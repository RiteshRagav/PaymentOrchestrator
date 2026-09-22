package com.ritesh.paymentorchestrator.router;

import com.ritesh.paymentorchestrator.dto.PaymentRequest;
import com.ritesh.paymentorchestrator.gateway.PaymentGateway;
import com.ritesh.paymentorchestrator.gateway.PaymentGatewayResponse;
import com.ritesh.paymentorchestrator.gateway.GatewayTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentRouter using mocked PaymentGateway interfaces.
 *
 * <p>All tests are deterministic because we use Mockito to control gateway behavior
 * exactly — no random failure rates, no latency. Each test verifies a specific
 * routing scenario independently.
 *
 * <p>Test scenarios:
 * 1. Primary gateway succeeds — no fallback needed.
 * 2. Primary fails, fallback (B) succeeds.
 * 3. All gateways fail — FAILED result.
 * 4. Gateway throws timeout exception — triggers fallback.
 * 5. Disabled gateway is skipped.
 * 6. Priority ordering is respected.
 * 7. No enabled gateways — immediate FAILED.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRouter Unit Tests")
class PaymentRouterTest {

    private PaymentRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new PaymentRequest("CUST-1001", new BigDecimal("1499.99"), "INR");
    }

    // ---- Helpers to create mock gateways ----

    private PaymentGateway mockGateway(String name, boolean enabled, int priority,
                                        boolean willSucceed) {
        PaymentGateway gateway = mock(PaymentGateway.class);
        lenient().when(gateway.getName()).thenReturn(name);
        lenient().when(gateway.isEnabled()).thenReturn(enabled);
        lenient().when(gateway.getPriority()).thenReturn(priority);
        if (enabled) {
            if (willSucceed) {
                lenient().when(gateway.processPayment(any())).thenReturn(
                        PaymentGatewayResponse.success(name, "TXN_123"));
            } else {
                lenient().when(gateway.processPayment(any())).thenReturn(
                        PaymentGatewayResponse.failure(name, name + " declined"));
            }
        }
        return gateway;
    }

    private PaymentGateway mockTimeoutGateway(String name, int priority) {
        PaymentGateway gateway = mock(PaymentGateway.class);
        when(gateway.getName()).thenReturn(name);
        when(gateway.isEnabled()).thenReturn(true);
        when(gateway.getPriority()).thenReturn(priority);
        when(gateway.processPayment(any())).thenThrow(new GatewayTimeoutException(name, 6000));
        return gateway;
    }

    /**
     * When the first gateway always succeeds, the router should return SUCCESS
     * on the first attempt without trying subsequent gateways.
     */
    @Test
    @DisplayName("1. Primary gateway succeeds — no fallback needed")
    void primaryGatewaySucceeds() {
        PaymentGateway gatewayA = mockGateway("MockGatewayA", true, 1, true);
        PaymentGateway gatewayB = mockGateway("MockGatewayB", true, 2, true);
        PaymentRouter router = new PaymentRouter(List.of(gatewayA, gatewayB));

        PaymentRoutingResult result = router.route(sampleRequest);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessfulGateway()).isEqualTo("MockGatewayA");
        assertThat(result.getAttemptedGateways()).containsExactly("MockGatewayA");
        verify(gatewayB, never()).processPayment(any()); // B never tried
    }

    /**
     * Gateway A fails, Gateway B succeeds. Router should fall back to B.
     */
    @Test
    @DisplayName("2. Primary gateway fails — fallback to secondary succeeds")
    void primaryFailsFallbackSucceeds() {
        PaymentGateway gatewayA = mockGateway("MockGatewayA", true, 1, false); // fails
        PaymentGateway gatewayB = mockGateway("MockGatewayB", true, 2, true);  // succeeds

        PaymentRouter router = new PaymentRouter(List.of(gatewayA, gatewayB));

        PaymentRoutingResult result = router.route(sampleRequest);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessfulGateway()).isEqualTo("MockGatewayB");
        assertThat(result.getAttemptedGateways()).containsExactly("MockGatewayA", "MockGatewayB");
    }

    /**
     * All three gateways fail. Router should return FAILED with all gateways in the audit trail.
     */
    @Test
    @DisplayName("3. All gateways fail — router returns FAILED")
    void allGatewaysFail() {
        PaymentGateway gatewayA = mockGateway("MockGatewayA", true, 1, false);
        PaymentGateway gatewayB = mockGateway("MockGatewayB", true, 2, false);
        PaymentGateway gatewayC = mockGateway("MockGatewayC", true, 3, false);
        PaymentRouter router = new PaymentRouter(List.of(gatewayA, gatewayB, gatewayC));

        PaymentRoutingResult result = router.route(sampleRequest);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).containsIgnoringCase("failed");
        assertThat(result.getAttemptedGateways())
                .containsExactly("MockGatewayA", "MockGatewayB", "MockGatewayC");
    }

    /**
     * Gateway A throws a timeout exception. Router should catch it, move to B, and succeed.
     */
    @Test
    @DisplayName("4. Gateway timeout exception triggers fallback")
    void gatewayTimeoutTriggersFallback() {
        PaymentGateway gatewayA = mockTimeoutGateway("MockGatewayA", 1); // times out
        PaymentGateway gatewayB = mockGateway("MockGatewayB", true, 2, true); // succeeds

        PaymentRouter router = new PaymentRouter(List.of(gatewayA, gatewayB));

        PaymentRoutingResult result = router.route(sampleRequest);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessfulGateway()).isEqualTo("MockGatewayB");
        assertThat(result.getAttemptedGateways()).containsExactly("MockGatewayA", "MockGatewayB");
    }

    /**
     * Disabled gateways are completely skipped. Only enabled gateways are tried.
     */
    @Test
    @DisplayName("5. Disabled gateway is skipped")
    void disabledGatewayIsSkipped() {
        PaymentGateway gatewayA = mockGateway("MockGatewayA", false, 1, true); // DISABLED
        PaymentGateway gatewayB = mockGateway("MockGatewayB", true, 2, true);  // enabled

        PaymentRouter router = new PaymentRouter(List.of(gatewayA, gatewayB));

        PaymentRoutingResult result = router.route(sampleRequest);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessfulGateway()).isEqualTo("MockGatewayB");
        assertThat(result.getAttemptedGateways()).doesNotContain("MockGatewayA");
        verify(gatewayA, never()).processPayment(any()); // A's processPayment never called
    }

    /**
     * Even if gateways are registered in reversed order, the router sorts by priority.
     * Priority 1 must be tried before priority 2 before priority 3.
     */
    @Test
    @DisplayName("6. Gateway priority order is respected")
    void priorityOrderRespected() {
        // Registered in reverse order — router must sort them
        PaymentGateway gatewayC = mockGateway("MockGatewayC", true, 3, false); // fails
        PaymentGateway gatewayB = mockGateway("MockGatewayB", true, 2, false); // fails
        PaymentGateway gatewayA = mockGateway("MockGatewayA", true, 1, true);  // succeeds

        PaymentRouter router = new PaymentRouter(List.of(gatewayC, gatewayB, gatewayA));

        PaymentRoutingResult result = router.route(sampleRequest);

        // A has priority 1, so it should be tried first and succeed
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessfulGateway()).isEqualTo("MockGatewayA");
        assertThat(result.getAttemptedGateways()).containsExactly("MockGatewayA");
    }

    /**
     * When no gateways are enabled, return FAILED immediately.
     */
    @Test
    @DisplayName("7. No enabled gateways — immediate FAILED result")
    void noEnabledGateways() {
        PaymentGateway gatewayA = mockGateway("MockGatewayA", false, 1, true);
        PaymentGateway gatewayB = mockGateway("MockGatewayB", false, 2, true);
        PaymentRouter router = new PaymentRouter(List.of(gatewayA, gatewayB));

        PaymentRoutingResult result = router.route(sampleRequest);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getAttemptedGateways()).isEmpty();
    }
}

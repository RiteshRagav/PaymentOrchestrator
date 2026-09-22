package com.ritesh.paymentorchestrator.service;

import com.ritesh.paymentorchestrator.dto.PaymentRequest;
import com.ritesh.paymentorchestrator.dto.PaymentResponse;
import com.ritesh.paymentorchestrator.dto.PaymentStatsResponse;
import com.ritesh.paymentorchestrator.entity.Payment;
import com.ritesh.paymentorchestrator.entity.PaymentStatus;
import com.ritesh.paymentorchestrator.exception.PaymentNotFoundException;
import com.ritesh.paymentorchestrator.repository.PaymentRepository;
import com.ritesh.paymentorchestrator.router.PaymentRouterPort;
import com.ritesh.paymentorchestrator.router.PaymentRoutingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService using Mockito.
 * Uses PaymentRouterPort interface instead of concrete PaymentRouter to avoid
 * Mockito byte-buddy issues with Java 26.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentRouterPort paymentRouter;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new PaymentRequest("CUST-1001", new BigDecimal("1499.99"), "INR");
    }

    @Test
    @DisplayName("1. Payment creation — successful routing persists SUCCESS status")
    void processPayment_successfulRouting() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> mockSavedPayment(inv.getArgument(0)));
        when(paymentRouter.route(any())).thenReturn(PaymentRoutingResult.builder()
                .success(true)
                .successfulGateway("MockGatewayA")
                .message("Payment processed successfully by MockGatewayA")
                .attemptedGateways(List.of("MockGatewayA"))
                .build());

        PaymentResponse response = paymentService.processPayment(sampleRequest, null);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getGateway()).isEqualTo("MockGatewayA");
        assertThat(response.getAmount()).isEqualByComparingTo("1499.99");
        verify(paymentRepository, atLeast(2)).save(any(Payment.class));
        verify(paymentRouter).route(sampleRequest);
    }

    @Test
    @DisplayName("2. Payment creation — all gateways fail persists FAILED status")
    void processPayment_allGatewaysFail() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> mockSavedPayment(inv.getArgument(0)));
        when(paymentRouter.route(any())).thenReturn(PaymentRoutingResult.builder()
                .success(false)
                .message("All configured payment gateways failed")
                .attemptedGateways(List.of("MockGatewayA", "MockGatewayB", "MockGatewayC"))
                .build());

        PaymentResponse response = paymentService.processPayment(sampleRequest, null);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getGateway()).isNull();
        assertThat(response.getMessage()).containsIgnoringCase("failed");
    }

    @Test
    @DisplayName("3. Payment retrieval — existing payment returned correctly")
    void getPayment_existingPayment() {
        Payment payment = buildSuccessPayment("PAY-ABC123");
        when(paymentRepository.findByPaymentId("PAY-ABC123")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPayment("PAY-ABC123");

        assertThat(response.getPaymentId()).isEqualTo("PAY-ABC123");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getGateway()).isEqualTo("MockGatewayA");
    }

    @Test
    @DisplayName("4. Payment retrieval — unknown payment throws PaymentNotFoundException")
    void getPayment_notFound_throwsException() {
        when(paymentRepository.findByPaymentId("PAY-UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment("PAY-UNKNOWN"))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("PAY-UNKNOWN");
    }

    @Test
    @DisplayName("5. Idempotency — same key returns existing payment without re-processing")
    void processPayment_idempotencyKeyReturnsExisting() {
        Payment existing = buildSuccessPayment("PAY-IDEMPOTENT");
        when(paymentRepository.findByIdempotencyKey("ORDER-9999")).thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.processPayment(sampleRequest, "ORDER-9999");

        assertThat(response.getPaymentId()).isEqualTo("PAY-IDEMPOTENT");
        verifyNoInteractions(paymentRouter);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("6. Get all payments — returns paginated results")
    void getAllPayments_returnsPaginatedList() {
        Payment p1 = buildSuccessPayment("PAY-001");
        Payment p2 = buildSuccessPayment("PAY-002");
        Page<Payment> pagedResult = new PageImpl<>(List.of(p1, p2));
        when(paymentRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(pagedResult);

        Page<PaymentResponse> result = paymentService.getAllPayments(0, 20);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getPaymentId()).isEqualTo("PAY-001");
    }

    // ---- Helpers ----

    private Payment buildSuccessPayment(String paymentId) {
        return Payment.builder()
                .id(1L)
                .paymentId(paymentId)
                .customerId("CUST-1001")
                .amount(new BigDecimal("1499.99"))
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .gateway("MockGatewayA")
                .attemptedGateways("MockGatewayA")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Payment mockSavedPayment(Payment p) {
        return Payment.builder()
                .id(1L)
                .paymentId(p.getPaymentId() != null ? p.getPaymentId() : "PAY-TEST")
                .customerId(p.getCustomerId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .gateway(p.getGateway())
                .failureReason(p.getFailureReason())
                .attemptedGateways(p.getAttemptedGateways())
                .idempotencyKey(p.getIdempotencyKey())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

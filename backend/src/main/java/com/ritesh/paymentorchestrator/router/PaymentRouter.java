package com.ritesh.paymentorchestrator.router;

import com.ritesh.paymentorchestrator.dto.PaymentRequest;
import com.ritesh.paymentorchestrator.gateway.GatewayTimeoutException;
import com.ritesh.paymentorchestrator.gateway.PaymentGateway;
import com.ritesh.paymentorchestrator.gateway.PaymentGatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Orchestrates payment processing across multiple gateways with fallback support.
 *
 * <p>The router implements a sequential fallback strategy:
 * <ol>
 *   <li>Collect all registered {@link PaymentGateway} beans from the Spring context.</li>
 *   <li>Filter out disabled gateways.</li>
 *   <li>Sort remaining gateways by priority (ascending — lower number = higher priority).</li>
 *   <li>Attempt payment through the first gateway.</li>
 *   <li>If it fails or times out, try the next gateway in line.</li>
 *   <li>Continue until one succeeds or all gateways are exhausted.</li>
 *   <li>Return a {@link PaymentRoutingResult} recording the outcome and the audit trail.</li>
 * </ol>
 *
 * <p>Design decisions:
 * <ul>
 *   <li>This class depends only on the {@code PaymentGateway} interface, not on any
 *       specific implementation. New gateways are discovered automatically via Spring DI.</li>
 *   <li>Routing logic is isolated here so the service layer remains clean.</li>
 *   <li>Maximum attempts are configurable per environment.</li>
 * </ul>
 */
@Component
public class PaymentRouter implements PaymentRouterPort {

    private static final Logger log = LoggerFactory.getLogger(PaymentRouter.class);

    private final List<PaymentGateway> gateways;

    @Value("${payment.router.max-attempts:3}")
    private int maxAttempts = 3;

    /**
     * All {@link PaymentGateway} implementations are injected automatically
     * by Spring because they are all annotated with @Component.
     */
    public PaymentRouter(List<PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    /**
     * Routes a payment request through available gateways with fallback.
     *
     * @param request the validated payment request
     * @return a routing result indicating success or exhaustion of all options
     */
    public PaymentRoutingResult route(PaymentRequest request) {
        List<PaymentGateway> eligibleGateways = getEligibleGateways();
        List<String> attemptedGateways = new ArrayList<>();

        if (eligibleGateways.isEmpty()) {
            log.error("No enabled payment gateways are available");
            return PaymentRoutingResult.builder()
                    .success(false)
                    .message("No payment gateways are currently available")
                    .attemptedGateways(attemptedGateways)
                    .build();
        }

        int attempts = 0;
        for (PaymentGateway gateway : eligibleGateways) {
            if (attempts >= maxAttempts) {
                log.warn("Max attempts ({}) reached, stopping routing", maxAttempts);
                break;
            }

            attempts++;
            log.info("Routing attempt {}/{} via gateway: {}", attempts, maxAttempts, gateway.getName());

            try {
                PaymentGatewayResponse response = gateway.processPayment(request);
                attemptedGateways.add(gateway.getName());

                if (response.isSuccess()) {
                    log.info("Payment succeeded via gateway: {}", gateway.getName());
                    return PaymentRoutingResult.builder()
                            .success(true)
                            .successfulGateway(gateway.getName())
                            .message("Payment processed successfully by " + gateway.getName())
                            .attemptedGateways(attemptedGateways)
                            .build();
                } else {
                    log.warn("Gateway {} returned failure: {}", gateway.getName(), response.getMessage());
                    // Continue to next gateway
                }

            } catch (GatewayTimeoutException e) {
                attemptedGateways.add(gateway.getName());
                log.warn("Gateway {} timed out: {}", gateway.getName(), e.getMessage());
                // Continue to next gateway

            } catch (Exception e) {
                attemptedGateways.add(gateway.getName());
                log.error("Unexpected error from gateway {}: {}", gateway.getName(), e.getMessage(), e);
                // Continue to next gateway
            }
        }

        log.error("All gateways failed after {} attempt(s). Attempted: {}", attempts, attemptedGateways);
        return PaymentRoutingResult.builder()
                .success(false)
                .message("All configured payment gateways failed")
                .attemptedGateways(attemptedGateways)
                .build();
    }

    /**
     * Returns enabled gateways sorted by priority (ascending).
     */
    private List<PaymentGateway> getEligibleGateways() {
        return gateways.stream()
                .filter(PaymentGateway::isEnabled)
                .sorted(Comparator.comparingInt(PaymentGateway::getPriority))
                .toList();
    }

    /**
     * Returns a read-only view of all configured gateways (enabled or not).
     * Used by the gateway status endpoint.
     */
    public List<PaymentGateway> getAllGateways() {
        return List.copyOf(gateways);
    }
}

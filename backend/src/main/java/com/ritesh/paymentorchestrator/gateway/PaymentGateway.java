package com.ritesh.paymentorchestrator.gateway;

import com.ritesh.paymentorchestrator.dto.PaymentRequest;

/**
 * Core abstraction for all payment gateways.
 *
 * <p>This interface defines the contract that every gateway implementation must fulfill.
 * The PaymentRouter depends only on this interface — it never knows about specific
 * gateway implementations. This is the Open/Closed Principle in practice: adding a new
 * gateway means adding a new implementation, not modifying the router.
 *
 * <p>In a real system, each implementation would call an external payment provider's API.
 * In this portfolio project, each implementation simulates success, failure, and latency
 * using configurable probabilities.
 */
public interface PaymentGateway {

    /**
     * Attempt to process a payment through this gateway.
     *
     * @param request the payment details
     * @return a result indicating success or failure with a descriptive message
     * @throws GatewayTimeoutException if the gateway exceeds its allowed processing time
     */
    PaymentGatewayResponse processPayment(PaymentRequest request);

    /**
     * Returns the display name of this gateway (e.g., "MockGatewayA").
     */
    String getName();

    /**
     * Returns whether this gateway is currently enabled for routing.
     * Disabled gateways are skipped entirely by the router.
     */
    boolean isEnabled();

    /**
     * Returns the routing priority. Lower numbers are tried first.
     * Example: priority 1 is tried before priority 2.
     */
    int getPriority();
}

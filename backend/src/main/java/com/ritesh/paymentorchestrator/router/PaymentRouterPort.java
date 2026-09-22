package com.ritesh.paymentorchestrator.router;

import com.ritesh.paymentorchestrator.dto.PaymentRequest;
import com.ritesh.paymentorchestrator.gateway.PaymentGateway;

import java.util.List;

/**
 * Port interface for the payment routing capability.
 * Extracted so that PaymentService can depend on this interface,
 * allowing Mockito to easily mock it in tests without byte-buddy issues.
 */
public interface PaymentRouterPort {

    /**
     * Routes a payment request through available gateways with fallback.
     */
    PaymentRoutingResult route(PaymentRequest request);

    /**
     * Returns all configured gateways (enabled or not).
     */
    List<PaymentGateway> getAllGateways();
}

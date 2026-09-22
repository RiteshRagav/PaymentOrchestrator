package com.ritesh.paymentorchestrator.exception;

/**
 * Thrown when a payment with the given ID cannot be found.
 */
public class PaymentNotFoundException extends RuntimeException {

    private final String paymentId;

    public PaymentNotFoundException(String paymentId) {
        super("Payment not found with ID: " + paymentId);
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }
}

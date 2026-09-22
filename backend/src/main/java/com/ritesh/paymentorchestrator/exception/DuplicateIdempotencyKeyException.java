package com.ritesh.paymentorchestrator.exception;

/**
 * Thrown when a payment request is submitted with an idempotency key
 * that has already been used for a different payment.
 *
 * <p>Note: Submitting the SAME key for the SAME payment is handled
 * silently by returning the existing payment. This exception is only
 * thrown in unexpected edge cases.
 */
public class DuplicateIdempotencyKeyException extends RuntimeException {

    private final String idempotencyKey;

    public DuplicateIdempotencyKeyException(String idempotencyKey) {
        super("Idempotency key already in use: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}

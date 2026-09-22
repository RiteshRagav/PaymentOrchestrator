package com.ritesh.paymentorchestrator.entity;

/**
 * Represents the lifecycle states of a payment transaction.
 */
public enum PaymentStatus {

    /** Payment record has been created but processing has not started. */
    PENDING,

    /** Payment is currently being processed by a gateway. */
    PROCESSING,

    /** Payment was successfully processed by a gateway. */
    SUCCESS,

    /** All gateway attempts failed and payment could not be completed. */
    FAILED
}

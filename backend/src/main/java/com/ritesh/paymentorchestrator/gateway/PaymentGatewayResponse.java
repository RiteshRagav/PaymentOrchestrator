package com.ritesh.paymentorchestrator.gateway;

import java.util.List;

/**
 * Immutable result returned by a gateway after processing a payment.
 */
public class PaymentGatewayResponse {

    private final boolean success;
    private final String gatewayName;
    private final String message;
    private final String transactionId;

    public PaymentGatewayResponse(boolean success, String gatewayName, String message, String transactionId) {
        this.success = success;
        this.gatewayName = gatewayName;
        this.message = message;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() { return success; }
    public String getGatewayName() { return gatewayName; }
    public String getMessage() { return message; }
    public String getTransactionId() { return transactionId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean success;
        private String gatewayName;
        private String message;
        private String transactionId;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder gatewayName(String gatewayName) { this.gatewayName = gatewayName; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public PaymentGatewayResponse build() {
            return new PaymentGatewayResponse(success, gatewayName, message, transactionId);
        }
    }

    public static PaymentGatewayResponse success(String gatewayName, String transactionId) {
        return new PaymentGatewayResponse(true, gatewayName,
                "Payment processed successfully by " + gatewayName, transactionId);
    }

    public static PaymentGatewayResponse failure(String gatewayName, String reason) {
        return new PaymentGatewayResponse(false, gatewayName, reason, null);
    }
}

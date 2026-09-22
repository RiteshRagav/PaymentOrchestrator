package com.ritesh.paymentorchestrator.router;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable result returned by the PaymentRouter after attempting all available gateways.
 */
public class PaymentRoutingResult {

    private final boolean success;
    private final String successfulGateway;
    private final String message;
    private final List<String> attemptedGateways;

    private PaymentRoutingResult(boolean success, String successfulGateway,
                                  String message, List<String> attemptedGateways) {
        this.success = success;
        this.successfulGateway = successfulGateway;
        this.message = message;
        this.attemptedGateways = attemptedGateways != null ? attemptedGateways : new ArrayList<>();
    }

    public boolean isSuccess() { return success; }
    public String getSuccessfulGateway() { return successfulGateway; }
    public String getMessage() { return message; }
    public List<String> getAttemptedGateways() { return attemptedGateways; }

    public String getAttemptedGatewaysAsString() {
        if (attemptedGateways == null || attemptedGateways.isEmpty()) return "";
        return String.join(", ", attemptedGateways);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean success;
        private String successfulGateway;
        private String message;
        private List<String> attemptedGateways;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder successfulGateway(String gateway) { this.successfulGateway = gateway; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder attemptedGateways(List<String> gateways) { this.attemptedGateways = gateways; return this; }
        public PaymentRoutingResult build() {
            return new PaymentRoutingResult(success, successfulGateway, message, attemptedGateways);
        }
    }
}

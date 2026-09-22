package com.ritesh.paymentorchestrator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Aggregated payment statistics for the dashboard.
 */
@Schema(description = "Payment system statistics")
public class PaymentStatsResponse {

    @Schema(description = "Total number of payment attempts", example = "150")
    private long totalPayments;

    @Schema(description = "Number of successful payments", example = "120")
    private long successfulPayments;

    @Schema(description = "Number of failed payments", example = "30")
    private long failedPayments;

    @Schema(description = "Number of pending/processing payments", example = "0")
    private long pendingPayments;

    @Schema(description = "Total amount successfully processed", example = "179988.00")
    private BigDecimal totalProcessedAmount;

    @Schema(description = "Success rate as a percentage", example = "80.0")
    private double successRate;

    public PaymentStatsResponse() {}

    public long getTotalPayments() { return totalPayments; }
    public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }

    public long getSuccessfulPayments() { return successfulPayments; }
    public void setSuccessfulPayments(long successfulPayments) { this.successfulPayments = successfulPayments; }

    public long getFailedPayments() { return failedPayments; }
    public void setFailedPayments(long failedPayments) { this.failedPayments = failedPayments; }

    public long getPendingPayments() { return pendingPayments; }
    public void setPendingPayments(long pendingPayments) { this.pendingPayments = pendingPayments; }

    public BigDecimal getTotalProcessedAmount() { return totalProcessedAmount; }
    public void setTotalProcessedAmount(BigDecimal totalProcessedAmount) { this.totalProcessedAmount = totalProcessedAmount; }

    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PaymentStatsResponse r = new PaymentStatsResponse();

        public Builder totalPayments(long v) { r.totalPayments = v; return this; }
        public Builder successfulPayments(long v) { r.successfulPayments = v; return this; }
        public Builder failedPayments(long v) { r.failedPayments = v; return this; }
        public Builder pendingPayments(long v) { r.pendingPayments = v; return this; }
        public Builder totalProcessedAmount(BigDecimal v) { r.totalProcessedAmount = v; return this; }
        public Builder successRate(double v) { r.successRate = v; return this; }
        public PaymentStatsResponse build() { return r; }
    }
}

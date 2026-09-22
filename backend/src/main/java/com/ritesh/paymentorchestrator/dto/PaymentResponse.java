package com.ritesh.paymentorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ritesh.paymentorchestrator.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO returned to clients after payment processing.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Payment processing result")
public class PaymentResponse {

    @Schema(description = "Unique payment identifier", example = "PAY-A1B2C3D4")
    private String paymentId;

    @Schema(description = "Final payment status")
    private PaymentStatus status;

    @Schema(description = "Gateway that processed the payment", example = "MockGatewayB")
    private String gateway;

    @Schema(description = "Payment amount", example = "1499.99")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "INR")
    private String currency;

    @Schema(description = "Human-readable result message", example = "Payment processed successfully")
    private String message;

    @Schema(description = "All gateways that were attempted")
    private String attemptedGateways;

    @Schema(description = "Timestamp when payment was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update")
    private LocalDateTime updatedAt;

    public PaymentResponse() {}

    // Getters & Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAttemptedGateways() { return attemptedGateways; }
    public void setAttemptedGateways(String attemptedGateways) { this.attemptedGateways = attemptedGateways; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ---- Builder ----
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PaymentResponse r = new PaymentResponse();

        public Builder paymentId(String v) { r.paymentId = v; return this; }
        public Builder status(PaymentStatus v) { r.status = v; return this; }
        public Builder gateway(String v) { r.gateway = v; return this; }
        public Builder amount(BigDecimal v) { r.amount = v; return this; }
        public Builder currency(String v) { r.currency = v; return this; }
        public Builder message(String v) { r.message = v; return this; }
        public Builder attemptedGateways(String v) { r.attemptedGateways = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { r.updatedAt = v; return this; }
        public PaymentResponse build() { return r; }
    }

    // ---- Convenience factory methods ----
    public static PaymentResponse success(String paymentId, String gateway, BigDecimal amount,
                                          String currency, String attempted, LocalDateTime createdAt,
                                          LocalDateTime updatedAt) {
        return builder()
                .paymentId(paymentId)
                .status(PaymentStatus.SUCCESS)
                .gateway(gateway)
                .amount(amount)
                .currency(currency)
                .message("Payment processed successfully")
                .attemptedGateways(attempted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static PaymentResponse failure(String paymentId, BigDecimal amount, String currency,
                                          String reason, String attempted, LocalDateTime createdAt,
                                          LocalDateTime updatedAt) {
        return builder()
                .paymentId(paymentId)
                .status(PaymentStatus.FAILED)
                .amount(amount)
                .currency(currency)
                .message(reason)
                .attemptedGateways(attempted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}

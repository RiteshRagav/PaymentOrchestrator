package com.ritesh.paymentorchestrator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO representing an incoming payment request from the client.
 */
@Schema(description = "Payment request payload")
public class PaymentRequest {

    @NotBlank(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    @Schema(description = "Unique customer identifier", example = "CUST-1001")
    private String customerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Amount format is invalid")
    @Schema(description = "Payment amount (must be > 0)", example = "1499.99")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g. INR, USD)")
    @Schema(description = "ISO 4217 currency code", example = "INR")
    private String currency;

    public PaymentRequest() {}

    public PaymentRequest(String customerId, BigDecimal amount, String currency) {
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String customerId;
        private BigDecimal amount;
        private String currency;

        public Builder customerId(String customerId) { this.customerId = customerId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public PaymentRequest build() { return new PaymentRequest(customerId, amount, currency); }
    }
}

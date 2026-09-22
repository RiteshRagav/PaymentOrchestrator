package com.ritesh.paymentorchestrator.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a payment transaction.
 * Uses BigDecimal for monetary amounts to avoid floating-point precision issues.
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payment_customer_id", columnList = "customer_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_created_at", columnList = "created_at")
    }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", unique = true, nullable = false, length = 50)
    private String paymentId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "gateway", length = 100)
    private String gateway;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "attempted_gateways", length = 500)
    private String attemptedGateways;

    @Column(name = "idempotency_key", unique = true, length = 200)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getAttemptedGateways() { return attemptedGateways; }
    public void setAttemptedGateways(String attemptedGateways) { this.attemptedGateways = attemptedGateways; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ---- Builder ----

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Payment payment = new Payment();

        public Builder id(Long id) { payment.id = id; return this; }
        public Builder paymentId(String paymentId) { payment.paymentId = paymentId; return this; }
        public Builder customerId(String customerId) { payment.customerId = customerId; return this; }
        public Builder amount(BigDecimal amount) { payment.amount = amount; return this; }
        public Builder currency(String currency) { payment.currency = currency; return this; }
        public Builder status(PaymentStatus status) { payment.status = status; return this; }
        public Builder gateway(String gateway) { payment.gateway = gateway; return this; }
        public Builder failureReason(String failureReason) { payment.failureReason = failureReason; return this; }
        public Builder attemptedGateways(String attemptedGateways) { payment.attemptedGateways = attemptedGateways; return this; }
        public Builder idempotencyKey(String idempotencyKey) { payment.idempotencyKey = idempotencyKey; return this; }
        public Builder createdAt(LocalDateTime createdAt) { payment.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { payment.updatedAt = updatedAt; return this; }
        public Payment build() { return payment; }
    }
}

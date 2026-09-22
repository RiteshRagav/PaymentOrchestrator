package com.ritesh.paymentorchestrator.repository;

import com.ritesh.paymentorchestrator.entity.Payment;
import com.ritesh.paymentorchestrator.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Spring Data JPA repository for Payment entities.
 * All query methods are auto-implemented by Spring Data.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find a payment by its application-generated payment ID (e.g., "PAY-A1B2").
     */
    Optional<Payment> findByPaymentId(String paymentId);

    /**
     * Find all payments for a specific customer, ordered by creation date descending.
     */
    Page<Payment> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    /**
     * Find all payments with a specific status, ordered by creation date descending.
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    /**
     * Check whether an idempotency key has already been used.
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Count payments by status.
     */
    long countByStatus(PaymentStatus status);

    /**
     * Sum of amount for all successful payments.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentAmounts();

    /**
     * Find all payments ordered by creation date descending.
     */
    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

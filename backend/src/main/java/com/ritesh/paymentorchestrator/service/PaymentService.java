package com.ritesh.paymentorchestrator.service;

import com.ritesh.paymentorchestrator.dto.PaymentRequest;
import com.ritesh.paymentorchestrator.dto.PaymentResponse;
import com.ritesh.paymentorchestrator.dto.PaymentStatsResponse;
import com.ritesh.paymentorchestrator.entity.Payment;
import com.ritesh.paymentorchestrator.entity.PaymentStatus;
import com.ritesh.paymentorchestrator.exception.PaymentNotFoundException;
import com.ritesh.paymentorchestrator.repository.PaymentRepository;
import com.ritesh.paymentorchestrator.router.PaymentRouterPort;
import com.ritesh.paymentorchestrator.router.PaymentRoutingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment business logic service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Idempotency key checking — if the same key is submitted twice, return the original result.</li>
 *   <li>Payment record lifecycle — PENDING → PROCESSING → SUCCESS/FAILED.</li>
 *   <li>Delegating routing to {@link PaymentRouter}.</li>
 *   <li>Persisting the final state to PostgreSQL via {@link PaymentRepository}.</li>
 *   <li>Providing retrieval, listing, and statistics operations.</li>
 * </ul>
 *
 * <p>The service intentionally contains no gateway-specific logic.
 * The service does not know which gateways exist — it only calls the router.
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentRouterPort paymentRouter;

    public PaymentService(PaymentRepository paymentRepository, PaymentRouterPort paymentRouter) {
        this.paymentRepository = paymentRepository;
        this.paymentRouter = paymentRouter;
    }

    /**
     * Processes a payment request.
     *
     * <p>Idempotency: if an {@code idempotencyKey} is provided and a payment already
     * exists with that key, the existing payment's result is returned without creating
     * a duplicate or re-processing.
     *
     * @param request        the payment request
     * @param idempotencyKey optional client-provided uniqueness key
     * @return the payment response
     */
    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey) {
        // Idempotency check
        if (StringUtils.hasText(idempotencyKey)) {
            Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotency hit for key '{}', returning existing payment {}",
                        idempotencyKey, existing.get().getPaymentId());
                return toResponse(existing.get());
            }
        }

        // Create payment in PENDING state
        String paymentId = generatePaymentId();
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .status(PaymentStatus.PENDING)
                .idempotencyKey(StringUtils.hasText(idempotencyKey) ? idempotencyKey : null)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Created payment {} for customer={}", paymentId, request.getCustomerId());

        // Mark as PROCESSING
        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Delegate to router
        PaymentRoutingResult result = paymentRouter.route(request);

        // Update final state
        if (result.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGateway(result.getSuccessfulGateway());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.getMessage());
        }
        payment.setAttemptedGateways(result.getAttemptedGatewaysAsString());
        payment = paymentRepository.save(payment);

        log.info("Payment {} final status: {} via gateway: {}",
                paymentId, payment.getStatus(), payment.getGateway());

        return toResponse(payment);
    }

    /**
     * Retrieves a single payment by its ID.
     *
     * @throws PaymentNotFoundException if the payment does not exist
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return toResponse(payment);
    }

    /**
     * Returns a paginated list of all payments, most recent first.
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return paymentRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    /**
     * Returns a paginated list of payments filtered by status.
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(this::toResponse);
    }

    /**
     * Returns aggregated statistics for the dashboard.
     */
    @Transactional(readOnly = true)
    public PaymentStatsResponse getStats() {
        long total = paymentRepository.count();
        long successful = paymentRepository.countByStatus(PaymentStatus.SUCCESS);
        long failed = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long pending = paymentRepository.countByStatus(PaymentStatus.PENDING)
                + paymentRepository.countByStatus(PaymentStatus.PROCESSING);
        BigDecimal totalAmount = paymentRepository.sumSuccessfulPaymentAmounts();

        double successRate = total > 0 ? (double) successful / total * 100 : 0.0;

        return PaymentStatsResponse.builder()
                .totalPayments(total)
                .successfulPayments(successful)
                .failedPayments(failed)
                .pendingPayments(pending)
                .totalProcessedAmount(totalAmount)
                .successRate(Math.round(successRate * 10.0) / 10.0)
                .build();
    }

    // ---- Private helpers ----

    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private PaymentResponse toResponse(Payment payment) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return PaymentResponse.success(
                    payment.getPaymentId(),
                    payment.getGateway(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getAttemptedGateways(),
                    payment.getCreatedAt(),
                    payment.getUpdatedAt()
            );
        } else {
            return PaymentResponse.failure(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getFailureReason() != null ? payment.getFailureReason()
                            : payment.getStatus().name(),
                    payment.getAttemptedGateways(),
                    payment.getCreatedAt(),
                    payment.getUpdatedAt()
            );
        }
    }
}

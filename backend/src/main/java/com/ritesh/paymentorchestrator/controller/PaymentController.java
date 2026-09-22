package com.ritesh.paymentorchestrator.controller;

import com.ritesh.paymentorchestrator.dto.PaymentRequest;
import com.ritesh.paymentorchestrator.dto.PaymentResponse;
import com.ritesh.paymentorchestrator.dto.PaymentStatsResponse;
import com.ritesh.paymentorchestrator.entity.PaymentStatus;
import com.ritesh.paymentorchestrator.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment operations.
 *
 * <p>This controller has NO business logic — it only:
 * <ul>
 *   <li>Validates incoming requests (via @Valid)</li>
 *   <li>Extracts HTTP-specific concerns (headers, path variables)</li>
 *   <li>Delegates to {@link PaymentService}</li>
 *   <li>Returns appropriate HTTP responses</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment processing and history operations")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
        summary = "Process a new payment",
        description = "Validates and routes the payment through available gateways with fallback support. " +
                      "Provide an 'Idempotency-Key' header to safely retry without creating duplicates."
    )
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false)
            @Parameter(description = "Optional unique key to prevent duplicate payments")
            String idempotencyKey) {

        PaymentResponse response = paymentService.processPayment(request, idempotencyKey);
        HttpStatus status = response.getStatus() == PaymentStatus.SUCCESS
                ? HttpStatus.CREATED
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "Get payment by ID")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable @Parameter(description = "Payment ID (e.g. PAY-A1B2C3D4)") String paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @Operation(summary = "Get all payments (paginated)")
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paymentService.getAllPayments(page, size));
    }

    @Operation(summary = "Get payments filtered by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByStatus(
            @PathVariable @Parameter(description = "Payment status: PENDING, PROCESSING, SUCCESS, FAILED")
            PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status, page, size));
    }

    @Operation(summary = "Get payment statistics")
    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsResponse> getStats() {
        return ResponseEntity.ok(paymentService.getStats());
    }
}

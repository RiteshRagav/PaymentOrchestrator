package com.ritesh.paymentorchestrator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritesh.paymentorchestrator.dto.PaymentRequest;
import com.ritesh.paymentorchestrator.dto.PaymentResponse;
import com.ritesh.paymentorchestrator.dto.PaymentStatsResponse;
import com.ritesh.paymentorchestrator.entity.PaymentStatus;
import com.ritesh.paymentorchestrator.exception.GlobalExceptionHandler;
import com.ritesh.paymentorchestrator.exception.PaymentNotFoundException;
import com.ritesh.paymentorchestrator.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller layer tests using @WebMvcTest (loads only the web layer).
 * PaymentService is mocked. GlobalExceptionHandler is imported for full error handling.
 */
@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("PaymentController Integration Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("POST /api/payments — valid request returns 201")
    void processPayment_validRequest_returns201() throws Exception {
        PaymentRequest request = new PaymentRequest("CUST-1001", new BigDecimal("1499.99"), "INR");
        PaymentResponse response = buildSuccessResponse("PAY-TEST01");
        when(paymentService.processPayment(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("PAY-TEST01"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.gateway").value("MockGatewayA"));
    }

    @Test
    @DisplayName("POST /api/payments — missing customerId returns 400")
    void processPayment_missingCustomerId_returns400() throws Exception {
        PaymentRequest request = new PaymentRequest(null, new BigDecimal("100.00"), "INR");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/payments — amount <= 0 returns 400")
    void processPayment_zeroAmount_returns400() throws Exception {
        PaymentRequest request = new PaymentRequest("CUST-1001", BigDecimal.ZERO, "INR");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/payments — invalid currency returns 400")
    void processPayment_invalidCurrency_returns400() throws Exception {
        PaymentRequest request = new PaymentRequest("CUST-1001", new BigDecimal("100.00"), "INVALID");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/payments/{paymentId} — existing payment returns 200")
    void getPayment_exists_returns200() throws Exception {
        when(paymentService.getPayment("PAY-TEST01")).thenReturn(buildSuccessResponse("PAY-TEST01"));

        mockMvc.perform(get("/api/payments/PAY-TEST01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-TEST01"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /api/payments/{paymentId} — not found returns 404")
    void getPayment_notFound_returns404() throws Exception {
        when(paymentService.getPayment("PAY-UNKNOWN"))
                .thenThrow(new PaymentNotFoundException("PAY-UNKNOWN"));

        mockMvc.perform(get("/api/payments/PAY-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PAYMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/payments — paginated list returns 200")
    void getAllPayments_returns200() throws Exception {
        Page<PaymentResponse> page = new PageImpl<>(List.of(buildSuccessResponse("PAY-001")));
        when(paymentService.getAllPayments(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].paymentId").value("PAY-001"));
    }

    @Test
    @DisplayName("GET /api/payments/stats — returns statistics")
    void getStats_returns200() throws Exception {
        PaymentStatsResponse stats = PaymentStatsResponse.builder()
                .totalPayments(100)
                .successfulPayments(85)
                .failedPayments(15)
                .totalProcessedAmount(new BigDecimal("127498.50"))
                .successRate(85.0)
                .build();
        when(paymentService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/payments/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayments").value(100))
                .andExpect(jsonPath("$.successfulPayments").value(85))
                .andExpect(jsonPath("$.successRate").value(85.0));
    }

    @Test
    @DisplayName("POST /api/payments — with Idempotency-Key header")
    void processPayment_withIdempotencyKey() throws Exception {
        PaymentRequest request = new PaymentRequest("CUST-1001", new BigDecimal("1499.99"), "INR");
        when(paymentService.processPayment(any(), eq("ORDER-5555"))).thenReturn(buildSuccessResponse("PAY-IDEM"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "ORDER-5555")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("PAY-IDEM"));
    }

    // ---- Helper ----
    private PaymentResponse buildSuccessResponse(String paymentId) {
        return PaymentResponse.builder()
                .paymentId(paymentId)
                .status(PaymentStatus.SUCCESS)
                .gateway("MockGatewayA")
                .amount(new BigDecimal("1499.99"))
                .currency("INR")
                .message("Payment processed successfully")
                .attemptedGateways("MockGatewayA")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

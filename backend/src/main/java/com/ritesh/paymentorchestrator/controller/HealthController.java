package com.ritesh.paymentorchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Simple health check endpoint for deployment platforms and monitoring.
 *
 * <p>This endpoint is intentionally lightweight — it does not check the database
 * or external dependencies. It signals that the application has started and is
 * accepting connections. Deployment platforms (Render, Railway, etc.) use this
 * to determine when the container is ready to serve traffic.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Application health and status")
public class HealthController {

    @Operation(summary = "Health check", description = "Returns UP when the application is running")
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "payment-orchestrator",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}

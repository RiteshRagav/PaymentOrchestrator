package com.ritesh.paymentorchestrator.controller;

import com.ritesh.paymentorchestrator.dto.GatewayStatusResponse;
import com.ritesh.paymentorchestrator.gateway.PaymentGateway;
import com.ritesh.paymentorchestrator.config.GatewayProperties;
import com.ritesh.paymentorchestrator.router.PaymentRouterPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing gateway configuration and status information.
 */
@RestController
@RequestMapping("/api/gateways")
@Tag(name = "Gateways", description = "Payment gateway configuration and status")
public class GatewayController {

    private final PaymentRouterPort paymentRouter;
    private final GatewayProperties gatewayProperties;

    public GatewayController(PaymentRouterPort paymentRouter, GatewayProperties gatewayProperties) {
        this.paymentRouter = paymentRouter;
        this.gatewayProperties = gatewayProperties;
    }

    @Operation(
        summary = "Get gateway status",
        description = "Returns configuration and current status for all configured payment gateways"
    )
    @GetMapping
    public ResponseEntity<List<GatewayStatusResponse>> getGatewayStatus() {
        List<GatewayStatusResponse> statuses = paymentRouter.getAllGateways().stream()
                .map(this::toStatusResponse)
                .sorted(java.util.Comparator.comparingInt(GatewayStatusResponse::getPriority))
                .toList();
        return ResponseEntity.ok(statuses);
    }

    private GatewayStatusResponse toStatusResponse(PaymentGateway gateway) {
        GatewayProperties.GatewayConfig config = getConfigForGateway(gateway.getName());
        return GatewayStatusResponse.builder()
                .name(gateway.getName())
                .enabled(gateway.isEnabled())
                .priority(gateway.getPriority())
                .failureRate(config.getFailureRate())
                .latencyMs(config.getLatencyMs())
                .status(gateway.isEnabled() ? "ACTIVE" : "DISABLED")
                .build();
    }

    private GatewayProperties.GatewayConfig getConfigForGateway(String name) {
        return switch (name) {
            case "MockGatewayA" -> gatewayProperties.getA();
            case "MockGatewayB" -> gatewayProperties.getB();
            case "MockGatewayC" -> gatewayProperties.getC();
            default -> new GatewayProperties.GatewayConfig();
        };
    }
}

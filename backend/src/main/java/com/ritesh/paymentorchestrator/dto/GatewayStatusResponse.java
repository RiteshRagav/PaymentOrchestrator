package com.ritesh.paymentorchestrator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing the current status and configuration of a payment gateway.
 */
@Schema(description = "Gateway configuration and status")
public class GatewayStatusResponse {

    @Schema(description = "Gateway identifier", example = "MockGatewayA")
    private String name;

    @Schema(description = "Whether the gateway is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "Routing priority (lower = higher priority)", example = "1")
    private int priority;

    @Schema(description = "Configured failure rate (0.0 - 1.0)", example = "0.20")
    private double failureRate;

    @Schema(description = "Simulated processing latency in milliseconds", example = "500")
    private long latencyMs;

    @Schema(description = "Current operational status", example = "ACTIVE")
    private String status;

    public GatewayStatusResponse() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public double getFailureRate() { return failureRate; }
    public void setFailureRate(double failureRate) { this.failureRate = failureRate; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final GatewayStatusResponse r = new GatewayStatusResponse();

        public Builder name(String v) { r.name = v; return this; }
        public Builder enabled(boolean v) { r.enabled = v; return this; }
        public Builder priority(int v) { r.priority = v; return this; }
        public Builder failureRate(double v) { r.failureRate = v; return this; }
        public Builder latencyMs(long v) { r.latencyMs = v; return this; }
        public Builder status(String v) { r.status = v; return this; }
        public GatewayStatusResponse build() { return r; }
    }
}

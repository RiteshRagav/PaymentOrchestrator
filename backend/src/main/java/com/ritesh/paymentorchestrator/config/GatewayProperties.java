package com.ritesh.paymentorchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed configuration properties for all payment gateways.
 * Bound from application.properties with prefix "gateway".
 *
 * <p>Example application.properties entries:
 * <pre>
 * gateway.a.enabled=true
 * gateway.a.failure-rate=0.20
 * gateway.a.latency-ms=500
 * gateway.a.priority=1
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private GatewayConfig a = new GatewayConfig();
    private GatewayConfig b = new GatewayConfig();
    private GatewayConfig c = new GatewayConfig();

    public GatewayConfig getA() { return a; }
    public void setA(GatewayConfig a) { this.a = a; }

    public GatewayConfig getB() { return b; }
    public void setB(GatewayConfig b) { this.b = b; }

    public GatewayConfig getC() { return c; }
    public void setC(GatewayConfig c) { this.c = c; }

    /**
     * Per-gateway configuration values. Uses explicit getters/setters
     * instead of Lombok @Data for Java 26 annotation processing compatibility.
     */
    public static class GatewayConfig {
        /** Whether this gateway participates in routing. */
        private boolean enabled = true;
        /** Simulated failure probability (0.0 = never, 1.0 = always). */
        private double failureRate = 0.1;
        /** Simulated processing delay in milliseconds. */
        private long latencyMs = 100;
        /** Routing priority — lower number = tried first. */
        private int priority = 1;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public double getFailureRate() { return failureRate; }
        public void setFailureRate(double failureRate) { this.failureRate = failureRate; }

        public long getLatencyMs() { return latencyMs; }
        public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }
}

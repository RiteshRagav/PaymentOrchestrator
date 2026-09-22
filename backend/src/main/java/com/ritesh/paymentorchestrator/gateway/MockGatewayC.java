package com.ritesh.paymentorchestrator.gateway;

import com.ritesh.paymentorchestrator.config.GatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Mock Gateway C — lowest priority, lowest failure rate.
 *
 * <p>Configuration (application.properties):
 * <pre>
 * gateway.c.enabled=true
 * gateway.c.failure-rate=0.05
 * gateway.c.latency-ms=300
 * gateway.c.priority=3
 * </pre>
 */
@Component
public class MockGatewayC extends AbstractMockGateway {

    private final GatewayProperties.GatewayConfig config;

    @Autowired
    public MockGatewayC(GatewayProperties properties) {
        super();
        this.config = properties.getC();
    }

    /** Test constructor allowing injected Random for deterministic behavior. */
    public MockGatewayC(GatewayProperties.GatewayConfig config, Random random) {
        super(random);
        this.config = config;
    }

    @Override
    public String getName() {
        return "MockGatewayC";
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }

    @Override
    public int getPriority() {
        return config.getPriority();
    }

    @Override
    protected double getFailureRate() {
        return config.getFailureRate();
    }

    @Override
    protected long getLatencyMs() {
        return config.getLatencyMs();
    }
}

package com.ritesh.paymentorchestrator.gateway;

import com.ritesh.paymentorchestrator.config.GatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Mock Gateway A — highest priority, moderate failure rate.
 *
 * <p>Configuration (application.properties):
 * <pre>
 * gateway.a.enabled=true
 * gateway.a.failure-rate=0.20
 * gateway.a.latency-ms=500
 * gateway.a.priority=1
 * </pre>
 */
@Component
public class MockGatewayA extends AbstractMockGateway {

    private final GatewayProperties.GatewayConfig config;

    @Autowired
    public MockGatewayA(GatewayProperties properties) {
        super();
        this.config = properties.getA();
    }

    /** Test constructor allowing injected Random for deterministic behavior. */
    public MockGatewayA(GatewayProperties.GatewayConfig config, Random random) {
        super(random);
        this.config = config;
    }

    @Override
    public String getName() {
        return "MockGatewayA";
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

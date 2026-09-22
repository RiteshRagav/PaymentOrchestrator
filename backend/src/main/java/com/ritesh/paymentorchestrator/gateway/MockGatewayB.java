package com.ritesh.paymentorchestrator.gateway;

import com.ritesh.paymentorchestrator.config.GatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Mock Gateway B — medium priority, higher failure rate.
 *
 * <p>Configuration (application.properties):
 * <pre>
 * gateway.b.enabled=true
 * gateway.b.failure-rate=0.50
 * gateway.b.latency-ms=800
 * gateway.b.priority=2
 * </pre>
 */
@Component
public class MockGatewayB extends AbstractMockGateway {

    private final GatewayProperties.GatewayConfig config;

    @Autowired
    public MockGatewayB(GatewayProperties properties) {
        super();
        this.config = properties.getB();
    }

    /** Test constructor allowing injected Random for deterministic behavior. */
    public MockGatewayB(GatewayProperties.GatewayConfig config, Random random) {
        super(random);
        this.config = config;
    }

    @Override
    public String getName() {
        return "MockGatewayB";
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

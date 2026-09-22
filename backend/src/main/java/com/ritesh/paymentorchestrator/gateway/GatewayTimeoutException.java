package com.ritesh.paymentorchestrator.gateway;

/**
 * Thrown when a gateway exceeds its configured latency threshold
 * and the router decides to abort and try the next gateway.
 */
public class GatewayTimeoutException extends RuntimeException {

    private final String gatewayName;

    public GatewayTimeoutException(String gatewayName, long latencyMs) {
        super(String.format("Gateway '%s' timed out after %dms", gatewayName, latencyMs));
        this.gatewayName = gatewayName;
    }

    public String getGatewayName() {
        return gatewayName;
    }
}

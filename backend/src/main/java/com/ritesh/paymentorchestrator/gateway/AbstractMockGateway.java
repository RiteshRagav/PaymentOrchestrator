package com.ritesh.paymentorchestrator.gateway;

import com.ritesh.paymentorchestrator.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

/**
 * Abstract base class for all mock gateway implementations.
 *
 * <p>Centralizes the shared simulation logic:
 * <ul>
 *   <li>Latency simulation via Thread.sleep</li>
 *   <li>Timeout detection (latency > threshold)</li>
 *   <li>Configurable failure rate using Random</li>
 *   <li>Success transaction ID generation</li>
 * </ul>
 *
 * <p>Subclasses only need to provide their configuration values.
 * In unit tests, a fixed seed Random or a fixed failure flag can be passed
 * to make behavior deterministic.
 */
public abstract class AbstractMockGateway implements PaymentGateway {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Latency above this threshold is treated as a timeout (ms). */
    private static final long TIMEOUT_THRESHOLD_MS = 5_000;

    private final Random random;

    protected AbstractMockGateway() {
        this.random = new Random();
    }

    /** Constructor allowing test injection of seeded Random for determinism. */
    protected AbstractMockGateway(Random random) {
        this.random = random;
    }

    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        log.info("[{}] Attempting payment for customer={} amount={} {}",
                getName(), request.getCustomerId(), request.getAmount(), request.getCurrency());

        simulateLatency();

        if (shouldFail()) {
            log.warn("[{}] Payment failed for customer={}", getName(), request.getCustomerId());
            return PaymentGatewayResponse.failure(getName(),
                    getName() + " declined the transaction");
        }

        String txId = generateTransactionId();
        log.info("[{}] Payment succeeded. txId={}", getName(), txId);
        return PaymentGatewayResponse.success(getName(), txId);
    }

    private void simulateLatency() {
        long latency = getLatencyMs();
        if (latency <= 0) return;

        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (latency > TIMEOUT_THRESHOLD_MS) {
            throw new GatewayTimeoutException(getName(), latency);
        }
    }

    private boolean shouldFail() {
        return random.nextDouble() < getFailureRate();
    }

    private String generateTransactionId() {
        return getName().toUpperCase().replace(" ", "_") + "_TXN_" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /** Configured failure rate (0.0 = never fails, 1.0 = always fails). */
    protected abstract double getFailureRate();

    /** Simulated processing time in milliseconds. */
    protected abstract long getLatencyMs();
}

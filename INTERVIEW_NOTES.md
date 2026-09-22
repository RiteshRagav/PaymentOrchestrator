# Technical Interview Guide & System Design Architecture Notes

This document provides deep technical talking points, design rationales, and architecture trade-offs for discussing this **Payment Orchestration Router** in senior software engineering and system design interviews.

---

## 1. Executive Summary & Pitch (30-Second Elevator Pitch)

> *"In real-world payment operations, relying on a single processor creates severe business risk: downtime causes revenue loss, while sub-optimal routing increases decline rates and interchange fees. I built a Payment Orchestration Router that abstracts multiple payment processors behind an intelligent waterfall routing engine. It features dynamic priority-based failover, strict idempotency enforcement to prevent double-charging, comprehensive transaction audit logging, and real-time operational telemetry."*

---

## 2. Core Architectural Pillars

### Pillar A: The Open-Closed Gateway Abstraction (SOLID)
- **Problem**: In naive payment architectures, service code directly calls vendor SDKs (e.g., `stripeClient.charge()`, `paypalClient.pay()`), leading to vendor lock-in and spaghetti branching.
- **Solution**:
  - We designed a generic `PaymentGateway` interface:
    ```java
    public interface PaymentGateway {
        String getName();
        boolean isEnabled();
        int getPriority();
        PaymentGatewayResponse processPayment(PaymentRequest request);
    }
    ```
  - `PaymentRouter` takes `List<PaymentGateway>` injected automatically by Spring's DI container.
  - Adding a new gateway (`MockGatewayD`) requires creating a single class with `@Component`. The router auto-discovers it, sorts it into the priority waterfall, and requires **zero modifications** to existing routing or service classes.

### Pillar B: Two-Phase Idempotency Protocol
- **Problem**: When a client or mobile app encounters a network timeout while sending a payment request, it retransmits the request. If unhandled, this results in **double charges**.
- **Solution**:
  - The client transmits an `Idempotency-Key` (UUIDv4) in the HTTP request headers.
  - We store `idempotency_key` with a **unique constraint** in the PostgreSQL `payments` table.
  - **Read phase**: When a request arrives, `PaymentService` queries `paymentRepository.findByIdempotencyKey(key)`. If found, the existing record is returned with an `HTTP 200` without re-engaging the gateway pipeline.
  - **Write phase**: If a concurrent duplicate slips past the initial read check, the database's unique constraint triggers a `DataIntegrityViolationException`, which our exception handler intercepts and safely resolves.

### Pillar C: Waterfall Fallback & Latency Isolation
- **Cascade Sequence**:
  1. Filter gateways where `isEnabled() == true`.
  2. Order gateways by `getPriority()` ascending (e.g., Priority 1 first).
  3. Attempt transaction with configured timeout protection.
  4. If the processor declines or times out, catch the error, record the attempted gateway in the transaction's audit trail, and immediately cascade to Priority 2 (`MockGatewayB`), then Priority 3 (`MockGatewayC`).
  5. The client receives a clean consolidated response explaining which gateways were evaluated and which one ultimately settled the charge.

---

## 3. Architecture Trade-offs & Deep Dive Questions

### Q1: Why synchronous HTTP routing instead of an asynchronous message queue (e.g., Kafka / RabbitMQ)?
- **Trade-off Answer**:
  - For standard e-commerce and POS checkout flows, the user is waiting in front of a screen. A synchronous request-response flow with quick fallback (under 2 seconds total) provides an immediate confirmation.
  - *When to evolve to asynchronous*: For high-volume batch payments, subscription renewals, or payouts, synchronous HTTP would exhaust server thread pools under spikes. In that scenario, we would decouple ingestion from processing:
    1. Accept payment request, publish `PaymentRequestedEvent` to Kafka, and return `202 Accepted` with a payment ID.
    2. A pool of distributed routing workers consumes from Kafka, handles gateway failover, and publishes `PaymentCompletedEvent`.
    3. The client receives the result via WebSockets / Server-Sent Events (SSE) or webhook callback.

### Q2: Why PostgreSQL for Idempotency instead of Redis distributed locks?
- **Trade-off Answer**:
  - In our architecture, the payment state and idempotency key are stored in the same relational transaction boundary in PostgreSQL. This ensures **strict ACID consistency** without the risk of distributed cache-database drift.
  - *Trade-off*: Under extreme throughput (e.g., >20,000 requests/sec), hitting PostgreSQL for every idempotency check increases database lock contention.
  - *Evolution at scale*: Combine Redis and PostgreSQL using a two-tier approach:
    1. Use Redis `SET key value NX PX 30000` to acquire an atomic distributed lock for in-flight requests.
    2. Store permanent historical keys in PostgreSQL or DynamoDB.

### Q3: How do you prevent "Cascading Failures" if an external gateway suffers high latency?
- **Trade-off Answer**:
  - Without timeouts and circuit breakers, slow gateways exhaust the application server's worker threads (e.g., Tomcat thread pool exhaustion).
  - *Protection implemented*: Each gateway call is bounded by a timeout (`GatewayTimeoutException`).
  - *Next step (Resilience4j Circuit Breaker)*: If `MockGatewayA` failure rate exceeds 50% over a 10-call sliding window, the circuit breaker opens for 30 seconds. The router skips Gateway A entirely and routes straight to Gateway B, saving latency and shielding Gateway A from further load.

---

## 4. Scaling the Architecture to 10,000+ Transactions Per Second (TPS)

In an interview, explain how you would scale this design to enterprise volume:

```
[ Load Balancer (AWS ALB / Cloudflare) ]
                  │
        ┌─────────┴─────────┐
        ▼                   ▼
 [ API Gateway / Auth ] [ API Gateway / Auth ]
        │                   │
        └─────────┬─────────┘
                  ▼
         [ Kafka Event Stream ]
         (Topic: payment-requests, 32 Partitions)
                  │
        ┌─────────┴─────────┐
        ▼                   ▼
 [ Routing Worker Pool ] [ Routing Worker Pool ]
   - Circuit Breakers      - Circuit Breakers
   - Health Monitor        - Health Monitor
        │                   │
        ├───────────────────┼────────────────────┐
        ▼                   ▼                    ▼
 [ Redis Cluster ]   [ PostgreSQL Cluster ] [ Webhook Dispatcher ]
 (Locks & Cache)     (Primary + Read Replicas)  (Outbox Pattern)
```

1. **Database Sharding & Read Replicas**:
   - Write operations (INSERT payments) go to PostgreSQL Primary.
   - Read operations (Dashboard stats, historical queries) query Read Replicas.
   - For multi-tenant global scale, shard the payment table by `tenant_id` or `customer_id`.
2. **Transactional Outbox Pattern**:
   - Instead of sending notifications or external webhooks inside the database transaction, write an `outbox_events` record within the same DB transaction. A CDC worker (e.g., Debezium) streams changes to Kafka, guaranteeing **at-least-once delivery** without distributed 2PC transactions.
3. **PCI-DSS Compliance Isolation**:
   - Cardholder Data Environment (CDE) should be segregated. In production, raw PAN (card numbers) should never touch this orchestration router. Instead, credit cards are tokenized on the frontend via PCI-compliant iframes, and the router handles tokenized payloads only.

---

## 5. Key Metrics to Monitor in Production (Telemetry)

1. **Routing Waterfall Fallback Rate**:
   - Percentage of payments requiring secondary or tertiary gateway fallback. A sudden spike in fallbacks indicates an upstream provider degradation.
2. **P99 Latency per Gateway**:
   - Time spent waiting for external gateway response. Used for dynamic latency-based routing.
3. **Idempotency Collision Rate**:
   - Frequency of duplicate submissions from clients. High collision rates may indicate aggressive client-side retry policies.
4. **Circuit Breaker State Transitions**:
   - Alerts whenever a gateway enters `OPEN` (tripped) state.

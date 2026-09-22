# Payment Orchestration Router

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-19-cyan.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-6-purple.svg)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

> **Educational & Portfolio Project**: A production-grade payment orchestration and smart routing layer designed to maximize transaction success rates, minimize processing latency, and provide automatic multi-gateway fallback resilience. **Strictly educational — does not process real financial assets or connect to real card networks.**

---

## 1. System Overview & Architecture

Modern payment operations cannot afford single points of failure. In enterprise fintech, relying on a single payment processor leads to downtime during gateway outages, geographic routing inefficiencies, and elevated decline rates.

The **Payment Orchestration Router** acts as an abstraction barrier between merchants/clients and underlying payment processors:
- **Intelligent Waterfall Cascade**: Attempts transactions across prioritized gateway processors.
- **Failover & Timeout Handling**: Automatically catches upstream network errors, timeouts, or processor declines and reroutes the charge to secondary/tertiary gateways without client intervention.
- **Idempotency Guarantee**: Protects against double-charging via client-supplied idempotency keys stored in PostgreSQL with unique database constraints.
- **Comprehensive Audit Trail**: Records every gateway attempted, execution timestamps, error messages, and final resolution.
- **Real-Time Observability**: Telemetry dashboard providing volume metrics, success rates, and gateway health.

### Architecture Diagram

```
[ Client / Web Browser / POS ]
              │
              ▼ HTTP / REST (Idempotency-Key)
    ┌───────────────────┐
    │  Spring Boot REST │
    │   Controllers     │
    └─────────┬─────────┘
              │
              ▼
    ┌───────────────────┐
    │  PaymentService   │ ◄───► [ PostgreSQL 16 DB ]
    │ (Idempotency Chk) │       (Payments Table & Keys)
    └─────────┬─────────┘
              │
              ▼
    ┌───────────────────────────────────────────────┐
    │          PaymentRouter Engine                 │
    │  (Dynamic Priority Sort & Cascade Waterfall)  │
    └───────┬──────────────┬──────────────┬─────────┘
            │ Attempt 1    │ Attempt 2    │ Attempt 3
            ▼              ▼              ▼
     ┌─────────────┐┌─────────────┐┌─────────────┐
     │MockGatewayA ││MockGatewayB ││MockGatewayC │
     │ (Priority 1)││ (Priority 2)││ (Priority 3)│
     │  Fast/20% F ││ Medium/50% F││ Reliable/5% │
     └─────────────┘└─────────────┘└─────────────┘
```

---

## 2. Gateway Profiles & Routing Policy

Gateways implement a clean `PaymentGateway` strategy interface. Each mock processor simulates realistic production conditions including artificial processing latency and randomized decline rates:

| Gateway | Priority | Latency | Failure Rate | Role |
| :--- | :---: | :---: | :---: | :--- |
| **MockGatewayA** | 1 (Primary) | ~500ms | 20% | High-volume primary channel; fastest path. |
| **MockGatewayB** | 2 (Secondary) | ~800ms | 50% | Secondary backup provider for transient outages. |
| **MockGatewayC** | 3 (Tertiary) | ~300ms | 5% | Ultra-reliable terminal fallback to salvage declining transactions. |

### Routing Waterfall Logic
1. Discovery of all Spring beans implementing `PaymentGateway`.
2. Gateways filtered by `isEnabled() == true`.
3. Eligible gateways sorted in ascending order of `getPriority()`.
4. Router attempts each gateway up to `payment.router.max-attempts` (default: 3).
5. If a gateway returns failure or throws `GatewayTimeoutException`, the audit trail appends the attempt and immediately delegates to the subsequent gateway.
6. If any gateway succeeds, the cascade terminates and the payment is marked `SUCCESS`.
7. If all eligible gateways fail, the transaction is marked `FAILED` with full diagnostic history.

---

## 3. Technology Stack

- **Backend**:
  - Java 21 (Modern LTS)
  - Spring Boot 3.3.4 (Spring MVC, Spring Data JPA, Hibernate, Bean Validation)
  - PostgreSQL 16 (Primary persistence) & H2 (In-memory testing)
  - Springdoc OpenAPI 2.6.0 (Interactive Swagger UI documentation)
  - JUnit 5 & Mockito 5 (Subclass mock-maker for Java 21/26 support)
- **Frontend**:
  - React 19 + TypeScript + Vite 6
  - Tailwind CSS + Lucide Icons
  - Axios with centralized error interceptors
  - SPA Client Routing with React Router 7
- **Infrastructure & Containerization**:
  - Docker & Docker Compose
  - Multi-stage Docker builds
  - Nginx 1.25 Alpine reverse proxy

---

## 4. API Specification & Endpoints

### Swagger / OpenAPI Documentation
When running locally, interactive API documentation is available at:
`http://localhost:8080/swagger-ui/index.html`

### Primary Endpoints

#### 1. Process Payment
`POST /api/payments`
- **Headers**: `Idempotency-Key` *(optional)*
- **Body**:
```json
{
  "customerId": "CUST-1001",
  "amount": 1499.50,
  "currency": "INR"
}
```
- **Response (201 Created / 200 OK for Idempotent)**:
```json
{
  "paymentId": "PAY-8FA3B140",
  "customerId": "CUST-1001",
  "amount": 1499.50,
  "currency": "INR",
  "status": "SUCCESS",
  "gateway": "MockGatewayB",
  "message": "Payment processed successfully by MockGatewayB",
  "attemptedGateways": ["MockGatewayA", "MockGatewayB"],
  "createdAt": "2026-09-22T17:15:30.124Z"
}
```

#### 2. Get Payment by ID
`GET /api/payments/{paymentId}`

#### 3. List Payments (Paginated)
`GET /api/payments?page=0&size=20`

#### 4. Filter Payments by Status
`GET /api/payments/status/{status}?page=0&size=20` (Statuses: `PENDING`, `SUCCESS`, `FAILED`, `TIMEOUT`)

#### 5. Get Aggregate Statistics
`GET /api/payments/stats`
- Returns total volume, success count, failure count, and overall success rate percentage.

#### 6. Gateway Status & Telemetry
`GET /api/gateways`

#### 7. Health Check
`GET /api/health`

---

## 5. Quickstart & Running Locally

### Option A: Using Docker Compose (Recommended)
Prerequisites: Docker Engine and Docker Compose installed.

```bash
# 1. Clone repository
git clone https://github.com/ritesh/payment-orchestrator.git
cd payment-orchestrator

# 2. Start full environment (PostgreSQL + Backend + Frontend)
docker-compose up --build
```
- Frontend UI: `http://localhost:3000`
- Backend REST API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Option B: Running Without Docker

#### Backend
Prerequisites: JDK 21+ and Maven (or use included Maven Wrapper `mvnw`).
```bash
cd backend

# Run with dev profile (H2 in-memory DB — no PostgreSQL installation needed)
# In PowerShell:
$env:SPRING_PROFILES_ACTIVE = "dev"; .\mvnw.cmd spring-boot:run

# In Bash / Mac / Linux:
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# Or run with default PostgreSQL (requires PostgreSQL running on localhost:5432):
.\mvnw.cmd spring-boot:run
```

#### Frontend
Prerequisites: Node.js 18+ and npm.
```bash
cd frontend
npm install
npm run dev
```
Access the dashboard at `http://localhost:5173`.

---

## 6. Running Automated Tests

The backend includes a comprehensive suite of 23 unit and integration tests covering:
- Sequential fallback and priority ordering
- Gateway timeout trigger and recovery
- Idempotency deduplication and caching
- Repository persistence and query constraints
- REST API contract validations and error handlers

```bash
cd backend
./mvnw test
```

Expected output:
```
[INFO] Results:
[INFO] 
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

## 7. Engineering & Architectural Decisions

1. **Idempotency Strategy**:
   - Client provides an `Idempotency-Key` header.
   - The database enforces a `UNIQUE` constraint on `idempotency_key`.
   - Incoming keys are queried before processing; if found, the prior response is returned immediately, preventing double debits during network retries.
2. **Decoupled Gateway Interface (`PaymentGateway`)**:
   - `PaymentRouter` depends solely on the `PaymentGateway` interface, adhering strictly to the Open/Closed Principle (SOLID). Adding new gateways requires zero modification to the router engine.
3. **Resilience & Fault Isolation**:
   - Individual gateway failures or timeouts are caught at the router boundary and do not crash the request lifecycle.
4. **Audit Trail Transparency**:
   - Every transaction maintains an `attemptedGateways` array, providing operators with full visibility into which vendors were invoked and why fallback was triggered.

---

## 8. License & Educational Disclaimer
This repository is an educational software design showcase developed for system design and portfolio demonstration. No real financial credentials or transactions are supported.

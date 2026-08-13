# Architecture

## Scope note

This is an **academic / portfolio implementation**, not an enterprise
deployment. It runs entirely on a single machine at **$0 infrastructure
cost**: Java, PostgreSQL, Python, and Node.js, all installed locally — no
Docker, no cloud subscription, no paid services. Azure Data Factory and
Azure Blob Storage are represented as the **target production
architecture** (see [`azure-adf/`](../azure-adf)) but are not required to
build, run, or demo the project.

## Conceptual architecture

```
5 Upstream Feeds                    Retry + Per-Feed Isolation
(Patients, Encounters,      →       (Azure Data Factory design /   →
 Diagnoses, Procedures,              local Java ingestion engine,
 Labs)                               actually running)
                                              │
                                              ▼
                                     Raw Landing Zone
                                     (local filesystem ⇄
                                      Azure Blob Storage)
                                              │
                                              ▼
                                     Validation & Transform
                                              │
                                              ▼
                                     PostgreSQL Analytics
                                     (partitioned + indexed)
                                              │
                                              ▼
                                     Spring Boot REST API
                                              │
                                              ▼
                                     React Dashboard
```

**Key property: a failure in any single feed pipeline (e.g. Encounters)
does not block, delay, or fail any other feed's pipeline.** This is
proven by an automated test — see [`docs/fault-tolerance.md`](fault-tolerance.md).

## Why a single Spring Boot application, not microservices

Five feeds, one small database, one small dashboard — splitting this into
separate deployable services would add network calls, service discovery,
and inter-service failure modes without adding any teaching value. The
distributed-systems property this project demonstrates (independent
units of work, isolated failure, retry) is expressed at the *feed*
level inside one application (`IngestionOrchestrator` runs 5 independent
pipelines, each wrapped in its own try/catch), not at the *service*
level. A single deployable JAR is also what keeps this a $0, no-Docker,
"clone and run" project.

## Package layout (backend)

```
com.healthcare.platform
├── controller/    REST endpoints, request/response mapping only
├── service/       business logic (patient/clinical-data/feed-status/metrics/ingestion)
├── repository/    Spring Data JPA repositories
├── entity/        JPA entities
├── dto/           API request/response contracts
├── mapper/        entity <-> DTO mapping
├── config/        Spring configuration (CORS, correlation IDs, properties)
├── exception/     centralized exception handling
├── audit/         feed_execution tracking
├── ingestion/      per-feed ingestion engine: retry, idempotency, isolation, validation, storage
└── analytics/     analytics query service (native SQL rollups)
```

## Data flow, step by step

1. **Data generator** (Python) produces 5 CSV files under `data/generated/`
   — `patients.csv`, `encounters.csv`, `diagnoses.csv`, `procedures.csv`,
   `labs.csv`.
2. **`IngestionOrchestrator`** runs each feed independently
   (`FeedIngestionService.ingest(feed)`), wrapped by a **retry policy**
   (`RetryExecutor`: 3 attempts, exponential backoff 2s/4s/8s by default).
3. Each attempt is recorded in **`audit.feed_execution`** before and after
   execution (`RUNNING` → `SUCCESS`/`FAILED`).
4. On each attempt, the raw file is copied into the **raw landing zone**
   (`StorageGateway` → `LocalFileSystemStorageGateway`), partitioned by
   `feed/year/month/day/batch_id` — the same path shape the Azure Blob
   Storage implementation would use in production.
5. The file is parsed and validated (`FeedValidator`): required fields,
   date parsing, duplicate natural keys within the batch. A content hash
   is computed for **idempotency**.
6. Valid rows are **upserted** into PostgreSQL `staging` tables
   (`ON CONFLICT` on natural key — replays are no-ops).
7. A transform step (`AnalyticsTransformRepository`) promotes `staging`
   rows into the **`analytics`** schema — `encounters` (partitioned,
   indexed), plus `patients`, `diagnoses`, `procedures`, `labs`.
8. **REST APIs** (`controller/`) expose the analytics layer via
   paginated, DTO-shaped endpoints.
9. The **React dashboard** polls these APIs for Overview, Pipeline
   Monitoring, Analytics, Patient Search, and the Failure Simulation panel.

## Per-feed failure isolation, concretely

Each feed's ingestion runs as an **independent unit of work** with its
own retry loop, database transaction, `feed_execution` audit row, and
exception boundary. The orchestrator iterates the 5 feeds and invokes
each through:

```java
try {
    results.add(feedIngestionService.ingest(feed));
} catch (Exception e) {
    // isolated: recorded as FAILED, loop continues to the next feed
}
```

so one exception cannot abort the batch. See
`backend/src/test/java/.../ingestion/FeedIsolationTest.java` (pure unit
test) and `IngestionIntegrationTest` (same property, proven against a
real PostgreSQL database) for the automated proof.

## What's design-only vs. what's actually running

| | Locally executable now (what this repo demonstrates) | Azure-ready design (not deployed) |
|---|---|---|
| Orchestration | `IngestionOrchestrator` + `RetryExecutor` | Azure Data Factory pipelines, [`azure-adf/`](../azure-adf) |
| Raw storage | `LocalFileSystemStorageGateway` | `AzureBlobStorageGateway` (real, compiling code — see [`azure-adf/documentation/README.md`](../azure-adf/documentation/README.md)) |
| Database | PostgreSQL (local install) | Azure Database for PostgreSQL |
| Everything else (API, dashboard, tests, benchmark) | Runs identically either way | — |
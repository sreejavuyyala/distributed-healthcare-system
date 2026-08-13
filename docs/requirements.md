# Requirements

## Scope

Academic / portfolio project. Demonstrates multi-source ingestion, fault
tolerance, retry, per-feed failure isolation, raw data landing, a
partitioned/indexed PostgreSQL analytics layer, and measurable query
performance improvement — at **$0 required infrastructure cost**, no
Docker.

> **No real PHI is used anywhere in this project.** All patient,
> encounter, diagnosis, procedure, and lab data is synthetically
> generated (see [`data-generator/`](../data-generator)). This is a
> demonstration platform, not a clinical system.

## Functional requirements

| ID | Requirement |
|----|-------------|
| F1 | Ingest 5 independent healthcare feeds: patients, encounters, diagnoses, procedures, labs |
| F2 | Each feed ingests, retries, and fails independently of every other feed (per-feed isolation) |
| F3 | Failed ingestion attempts retry with exponential backoff up to a configured maximum |
| F4 | Raw feed files are persisted immutably in a landing zone before transformation (local filesystem, standing in for Azure Blob Storage) |
| F5 | Raw data is validated and transformed into a normalized PostgreSQL analytics schema |
| F6 | Duplicate/replayed feed files do not create duplicate analytics records (idempotent ingestion) |
| F7 | Every ingestion attempt is recorded in a `feed_execution` audit table (status, counts, retries, error) |
| F8 | REST APIs expose patients, encounters, diagnoses, labs, analytics rollups, feed status, and platform metrics |
| F9 | A React dashboard visualizes pipeline health, clinical analytics, a patient explorer, and a live failure-isolation demo |
| F10 | An operator can trigger a simulated feed failure and observe isolation in real time |

## Non-functional requirements

| ID | Requirement | How it's addressed |
|----|-------------|---------------------|
| N1 | **Fault tolerance** | Each feed runs on its own execution path with its own retry/fail state (`ingestion/` package) |
| N2 | **Idempotency** | SHA-256 content hash + natural-key unique constraints |
| N3 | **Recoverability** | Raw landing zone is immutable and keyed by batch ID; re-running a batch reuses the same batch ID |
| N4 | **Low query latency** | Partitioning + indexing on the one large, time-queried table (`encounters`) — measured in [`docs/performance.md`](performance.md) |
| N5 | **Observability** | `feed_execution` audit trail, `/api/metrics` |
| N6 | **Maintainability** | Layered Spring Boot architecture, versioned SQL migrations |
| N7 | **Security** | No hardcoded secrets, env-var config, no PHI in logs, input validation, parameterized queries only |
| N8 | **Data quality** | Schema/null/date/duplicate validation rejects bad rows before they reach `analytics` |
| N9 | **Cost** | $0 required infrastructure — no Docker, no cloud subscription |

## Constraints

- Must run fully locally: Java 21, PostgreSQL, Python, Node.js — all free
  local installs, no Docker.
- Azure Data Factory / Azure Blob Storage are **design-only** artifacts
  (see [`azure-adf/`](../azure-adf)); not deployed, not required.
- All data is synthetic; the repository is safe to publish publicly.

## Reliability

- Maximum 3 retries per feed batch, exponential backoff (2s → 4s → 8s),
  then mark `FAILED` and continue with the other feeds.
- Every ingestion run writes a `feed_execution` row before it starts and
  updates it when it finishes.
- Partial batches (some valid rows, some invalid) process the valid rows
  and report the invalid count — invalid rows are not silently dropped
  without a trace.

## Data

- Generator produces referentially consistent patients → encounters →
  diagnoses/procedures/labs, with a deterministic seed for
  reproducibility (`data-generator/generate_data.py --seed 42`).
- Default volumes: 10,000 patients / 30,000 encounters / 50,000
  diagnoses / 20,000 procedures / 50,000 labs (all configurable via CLI
  flags).
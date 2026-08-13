# Data Pipeline

```
Source CSV → Ingestion → Retry → Raw Storage → Validation/Transform → Analytics DB → API → Dashboard
```

## 1. Source

Five independent CSV feeds under `data/generated/` (produced by
[`data-generator/generate_data.py`](../data-generator/generate_data.py)):
`patients.csv`, `encounters.csv`, `diagnoses.csv`, `procedures.csv`,
`labs.csv`. Each row's shape and validation rules are declared once, in
`FeedName.java`, and reused by both the validator and the staging upsert
(no per-feed duplication of column lists).

## 2. Ingestion + Retry

`FeedIngestionService.ingest(feed)` reads the file, computes a SHA-256
content hash, and hands the actual work (store raw → validate → upsert
staging → promote to analytics) to `RetryExecutor`:

```
attempt 1 → fails → wait 2s
attempt 2 → fails → wait 4s
attempt 3 → fails → give up, mark FAILED, retryCount = 2
```

Configurable via environment variables (`MAX_RETRIES`,
`INITIAL_BACKOFF_MS`, `BACKOFF_MULTIPLIER` — see `.env.example`).
Failures can be **simulated** two ways (`FailureSimulator`):

- `FAILURE_RATE` (0.0–1.0): every attempt of every feed has this
  probability of a randomly injected transient failure.
- Deterministic, via the dashboard's Failure Simulation panel or
  `POST /api/feeds/simulate`: forces a specific feed's next N attempts to
  fail — this is what makes the fault-tolerance demo reproducible on
  demand instead of left to chance.

## 3. Raw Storage

Every attempt writes the raw file bytes to the landing zone before any
parsing happens, at:

```
<root>/<feed>/year=YYYY/month=MM/day=DD/<batchId>/<filename>
```

Locally this is a directory on disk (`LocalFileSystemStorageGateway`);
in the target production architecture it's an Azure Blob Storage
container (`AzureBlobStorageGateway`, real code, not exercised in this
project's tests/demo — see [`azure-adf/documentation/README.md`](../azure-adf/documentation/README.md)).
Keeping the raw file immutable and separate from the transformed data is
what makes a failed transform **recoverable**: re-run against the same
raw bytes rather than needing the file re-delivered.

## 4. Validation

`FeedValidator` checks, per row:

- required fields are present (schema validation)
- date/timestamp fields parse
- the natural key isn't a duplicate within the same batch

A row that fails validation is counted in `records_failed` and excluded
from staging — **the whole batch is not rejected for one bad row.**

## 5. Idempotency

- `content_hash` = SHA-256 of the raw file bytes.
- `batch_id` = a UUID deterministically derived from `(feed name, content_hash)`
  (`UUID.nameUUIDFromBytes`), so the exact same file always resolves to
  the exact same batch ID.
- `audit.feed_execution` has `UNIQUE(feed_name, batch_id)` — if that
  batch already succeeded, ingestion is skipped (logged as an
  "idempotent skip") rather than reprocessed.
- Even if reprocessing does happen, `staging` tables use
  `INSERT ... ON CONFLICT (natural_key) DO UPDATE` — replays never
  create duplicate rows.

This means the same feed file delivered twice (a realistic scenario with
at-least-once upstream delivery) never corrupts analytics counts.

## 6. Transform → Analytics

`AnalyticsTransformRepository` promotes just-ingested `staging` rows into
the `analytics` schema with one `INSERT ... SELECT ... ON CONFLICT`
statement per feed, scoped to the current `batch_id`. The `encounters`
transform additionally computes `length_of_stay_hours` from
`admission_time`/`discharge_time`. See [`docs/database-design.md`](database-design.md).

## 7. API + Dashboard

The Spring Boot REST API reads only from the `analytics` schema (never
`staging`), through paginated, DTO-shaped endpoints — see
[`docs/api.md`](api.md). The React dashboard polls these endpoints (5s–30s
intervals depending on the panel) so the Pipeline Monitoring and Failure
Simulation views update live during a demo.
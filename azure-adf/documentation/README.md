# Azure Data Factory — Design Artifact (Not Deployed)

## Status: design-only, not a runtime dependency

This directory shows how the ingestion pattern implemented locally in
[`backend/src/main/java/.../ingestion/`](../../backend/src/main/java/com/healthcare/platform/ingestion)
maps onto Azure Data Factory as a **production target architecture**. It is
**not deployed**, and the project **does not require an Azure subscription
to build, run, or demo**. Every capability shown here — retry, per-feed
isolation, raw landing, error logging — already runs locally, for $0, via
the Spring Boot ingestion engine and PostgreSQL.

## What's here

- [`pipelines/PL_PATIENTS.json`](../pipelines/PL_PATIENTS.json) and
  [`pipelines/PL_ENCOUNTERS.json`](../pipelines/PL_ENCOUNTERS.json) —
  representative ADF pipeline definitions. The other three feeds
  (diagnoses, procedures, labs) would follow the identical shape — one
  pipeline per feed — and are omitted here to avoid repeating the same
  JSON five times.

Each pipeline shows the same 4 stages the local `FeedIngestionService`
implements in Java:

| Stage | ADF (design) | Local implementation (actually running) |
|---|---|---|
| Validate | `Validation` activity | `FeedValidator` (schema/null/date checks) |
| Copy with retry | `Copy` activity, `policy.retry: 3`, `retryIntervalInSeconds: 2` | `RetryExecutor` (exponential backoff 2s→4s→8s) |
| Raw landing | `AzureBlobStorageWriteSettings` sink | `StorageGateway` → `LocalFileSystemStorageGateway` (see below) |
| Success/failure logging | `WebActivity` → callback | `FeedExecutionService` → `audit.feed_execution` row |

## Per-feed failure isolation, at the orchestration layer

Each feed gets its **own pipeline** (`PL_PATIENTS`, `PL_ENCOUNTERS`, ...),
triggered independently. `PL_ENCOUNTERS` failing has no `dependsOn` edge to
any other pipeline — ADF simply has no mechanism by which one pipeline's
failure could cancel a sibling pipeline's run, which is exactly the
property `IngestionOrchestrator.runAllFeeds()` reproduces locally with a
per-feed try/catch. See [`docs/fault-tolerance.md`](../../docs/fault-tolerance.md).

## Local filesystem ⇄ Azure Blob Storage mapping

| Local (what actually runs) | Azure (production target) |
|---|---|
| `LocalFileSystemStorageGateway` writes to `./local-storage/raw/<feed>/year=YYYY/month=MM/day=DD/<batchId>/<file>` | `AzureBlobStorageGateway` (also implemented, in `backend/.../ingestion/storage/`) writes the identical path structure to a Blob container |
| `data/generated/*.csv` (produced by the Python generator) | Feed files landing in an Azure Storage source container / SFTP / Data Factory source dataset |
| Toggled via `platform.storage.provider=local` (default) | Toggled via `platform.storage.provider=azure` + `AZURE_STORAGE_CONNECTION_STRING` (never hardcoded; see `.env.example`) |

The `StorageGateway` Java interface is what makes this swap possible with
zero change to ingestion business logic — see
[`docs/data-pipeline.md`](../../docs/data-pipeline.md).

## What would be needed to actually deploy this

1. An Azure Storage Account with a container for the raw landing zone.
2. An Azure Data Factory instance with linked services pointing at the
   storage account and at the source feed location.
3. The 5 pipelines above (fully fleshed out from these 2 representative
   examples), each on its own trigger (schedule or event-based).
4. `AZURE_STORAGE_CONNECTION_STRING` supplied to the Spring Boot app via
   environment variable / CI secret, and `platform.storage.provider=azure`.

None of this has been provisioned for this project — it is documented here
so the design can be discussed and evaluated without incurring any cost.
# Database Design

Full DDL (canonical): [`backend/src/main/resources/db/migration/`](../backend/src/main/resources/db/migration)
(applied by Flyway on startup) — mirrored for browsability at
[`database/migrations/`](../database/migrations).

## Schemas

| Schema | Purpose |
|---|---|
| `staging` | Landing tables mirroring feed shape; idempotent upsert target (natural key + `content_hash` + `batch_id`) |
| `analytics` | Query-optimized tables the REST API reads — one of which (`encounters`) is partitioned |
| `audit` | `feed_execution` — one row per ingestion attempt, per feed, per batch |

## Tables

`patients`, `encounters`, `diagnoses`, `procedures`, `labs` — matching the
5 feeds. Provider and facility are **plain descriptive columns on
`encounters`** (`provider_name`, `specialty`, `facility_name`), not
separate feeds/tables — introducing two more normalized entities just to
hold a name and a specialty would add joins without adding a teaching
point at this project's scope.

## Partitioning — the one table that's partitioned

Per the design goal ("partition ONE meaningful table, not everything"):

| Table | Partition key | Why |
|---|---|---|
| `analytics.encounters` | `encounter_date` (DATE, yearly RANGE) | Largest, most time-filtered table — patient history, "encounters this year", and length-of-stay queries all filter by date. Yearly partitions let Postgres prune to a single partition instead of scanning the whole table. |
| Everything else | Not partitioned | `patients` is small and not time-queried. `diagnoses`/`procedures`/`labs` are queried primarily by `patient_id` or code, not by date range — partitioning them would add planner overhead with no pruning benefit. |

`analytics.encounters`'s true primary key is the composite
`(encounter_id, encounter_date)` — Postgres requires the partition key to
be part of any unique/primary key on a partitioned table. One consequence:
`diagnoses.encounter_id` / `procedures.encounter_id` are **not** declared
as foreign keys into `encounters` (a partitioned table's referenced
columns must be covered by a single unique index, which the composite key
doesn't satisfy in the shape this project needs); that referential
integrity is enforced at the application layer instead. `patient_id`
columns keep real foreign keys since `analytics.patients` isn't
partitioned.

## Indexing

Every index in `V7__analytics_indexes.sql` is tied to a specific query
the API or dashboard runs:

| Index | Query it serves |
|---|---|
| `encounters (patient_id, encounter_date DESC)` | `GET /api/patients/{id}/encounters` — most common single-patient query |
| `encounters (encounter_date)` | date-range queries independent of patient |
| `encounters (facility_name, encounter_date)` | "encounters by facility" analytics chart |
| `diagnoses (patient_id)`, `diagnoses (diagnosis_code)` | patient diagnosis lookup, diagnosis-frequency rollup |
| `procedures (patient_id)`, `procedures (procedure_code)` | mirrors diagnoses usage |
| `labs (patient_id, collected_at DESC)`, `labs (test_name)` | patient lab history, lab-trend rollup |

Measured impact of partitioning + indexing: [`docs/performance.md`](performance.md).

## Idempotency mechanism

See [`docs/data-pipeline.md`](data-pipeline.md#5-idempotency) for the full
explanation: SHA-256 content hash → deterministic `batch_id` →
`UNIQUE(feed_name, batch_id)` on `audit.feed_execution`, plus
`ON CONFLICT (natural_key)` upserts on every staging table.

## Why PostgreSQL instead of a dedicated warehouse

At this project's scale (160K rows across 5 tables) PostgreSQL serves
both the operational read path (paginated API queries) and the
analytical rollups (GROUP BY aggregations) well, with none of the
operational cost or complexity of standing up a separate OLAP system
(Redshift/BigQuery/Snowflake/Databricks) — all of which would also break
the $0-infrastructure-cost constraint. Partitioning + targeted indexing
get most of the benefit a warehouse would provide at this volume; a real
warehouse would earn its cost at a much larger scale than a portfolio
project needs to demonstrate the concept.
# Performance

## Methodology

Three representative queries were run against two versions of the same
data:

- **Baseline**: `benchmark.encounters_baseline` / `benchmark.diagnoses_baseline`
  — plain tables, no partitioning, no secondary indexes (PK only),
  loaded with the exact same rows as the real tables.
- **Optimized**: the real `analytics.encounters` (partitioned by
  `encounter_date`, indexed) and `analytics.diagnoses` (indexed) tables.

Each query ran with `EXPLAIN (ANALYZE, FORMAT JSON)`, 2 warm-up runs
followed by 7 measured runs, taking the **median** execution time
reported by Postgres itself (not wall-clock from the client, which would
include network/psql overhead). Full script:
[`reports/performance/run_benchmark.py`](../reports/performance/run_benchmark.py);
setup and query SQL: [`database/queries/benchmark_setup.sql`](../database/queries/benchmark_setup.sql),
[`database/queries/benchmark_queries.sql`](../database/queries/benchmark_queries.sql);
raw results: [`reports/performance/results.json`](../reports/performance/results.json).

Reproduce it yourself:

```bash
# after ingesting the full dataset (see README "Run the demo")
cd reports/performance
python3 run_benchmark.py --db healthcare --runs 7
```

## Results (measured, not assumed)

| Query | Baseline (median of 7) | Optimized (median of 7) | Improvement |
|---|---|---|---|
| Q1: Patient encounter history (indexed point lookup + sort) | 2.443 ms | 0.105 ms | **95.7%** |
| Q2: Encounters by facility, date-range filtered (partition pruning) | 4.151 ms | 2.835 ms | **31.7%** |
| Q3: Diagnoses by patient (indexed lookup) | 4.820 ms | 0.099 ms | **97.9%** |
| **Average** | | | **75.1%** |

Run on: Apple Silicon Mac, local PostgreSQL 16, 10,000 patients / 30,000
encounters / 50,000 diagnoses / 20,000 procedures / 50,000 labs (160,000
rows total). Raw samples: [`reports/performance/results.json`](../reports/performance/results.json).

**These numbers move between runs** — a repeat run on the same machine,
same data, minutes apart, previously measured 95.5% / 60.5% / 98.0%
(84.7% average) for the identical 3 queries. At sub-5ms baseline
latencies, OS-level disk/page-cache state and background load on the
machine running the benchmark dominate the variance more than anything
about the query plan itself. This is expected, and is exactly why the
methodology takes a **median of 7 runs** per query rather than a single
sample — and why this doc reports whatever the most recent run actually
produced rather than cherry-picking the best one. Reproduce it yourself
with the command above; don't expect to reproduce these exact digits.

## Honest interpretation

The original project brief targets "approximately 30%" improvement. The
measured average here (75.1%, and 84.7% on an earlier run) is **higher**
than that target — the brief's number was explicitly treated as a target
to attempt, not a number to force, and the actual measurement is reported
as-is per that instruction (never fabricate or adjust a result to match
an expected figure).

Interestingly, Q2 (the query that does real aggregation work — a
`GROUP BY` over a date-filtered range — rather than a single-row point
lookup) landed at **31.7%** on this run, almost exactly the brief's
target, while Q1 and Q3 (pure indexed point lookups) landed at 95–98%.
That split is the more informative result than the headline average:
**index-only point lookups see a dramatic, near-total improvement at
this data volume, while aggregation-style queries — which still have to
touch every matching row even with a good index — see a more modest,
brief-sized improvement.** A query mix weighted more toward aggregation
(as a real analytics workload would be) should be expected to land
closer to the 30% figure than a mix weighted toward point lookups.

At this project's data volume, even the *baseline* queries are fast in
absolute terms (2–5 ms) because Postgres's sequential scan over
~30K–50K rows still fits comfortably in memory — so relative percentages
swing more than they would at a larger scale, and run-to-run variance
(see above) is large relative to the absolute times involved. At larger
data volumes (millions of rows, data that doesn't fit in the buffer
cache), the absolute time saved would grow substantially, and these
percentages would likely stabilize.

## Query plan mechanism

- **Q1 & Q3** go from a `Seq Scan` (baseline, no index on `patient_id`) to
  an `Index Scan` using `idx_encounters_patient_date` /
  `idx_diagnoses_patient` (optimized) — this is the dominant effect for
  point lookups.
- **Q2** goes from scanning the entire baseline table to Postgres
  **pruning to a single yearly partition** (`analytics.encounters_2026`)
  before an `Index Scan` on `idx_encounters_facility` runs inside it —
  visible in the `EXPLAIN` plan as far fewer rows scanned even though the
  query still aggregates across all facilities.

## Other measured numbers (not benchmark-specific)

From a real run, captured via the platform's own `/api/metrics` endpoint
and `data-generator` timing (see respective docs):

- Synthetic data generation (160,000 records, default volumes): **~3.4s**
  on the same machine.
- Full 5-feed ingestion (160,000 records, cold start): **~2.3s average
  per feed** (`averageProcessingTimeMs` from `/api/metrics`).
- Live analytics query latency probe (`averageQueryLatencyMs` in
  `/api/metrics`, computed at request time from 3 representative
  queries): **~3–4 ms**.
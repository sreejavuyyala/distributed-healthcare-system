-- The 3 representative queries benchmarked (before vs after). Each pair
-- targets the exact same result set against the baseline table (no
-- partitioning, no secondary indexes) vs the optimized table (partitioned
-- and/or indexed) — see reports/performance/results.json for measured timings
-- and docs/performance.md for the analysis.

-- =========================================================================
-- Q1: Patient encounter history — most common single-patient query.
-- Optimized table benefits from idx_encounters_patient_date (patient_id, encounter_date DESC).
-- =========================================================================
-- BASELINE
EXPLAIN (ANALYZE, FORMAT JSON)
SELECT * FROM benchmark.encounters_baseline
WHERE patient_id = 'PAT-0005000'
ORDER BY encounter_date DESC
LIMIT 25;

-- OPTIMIZED
EXPLAIN (ANALYZE, FORMAT JSON)
SELECT * FROM analytics.encounters
WHERE patient_id = 'PAT-0005000'
ORDER BY encounter_date DESC
LIMIT 25;

-- =========================================================================
-- Q2: Encounters by facility within a date range — dashboard "encounters by
-- facility" chart. Optimized table benefits from partition pruning
-- (encounter_date) + idx_encounters_facility.
-- =========================================================================
-- BASELINE
EXPLAIN (ANALYZE, FORMAT JSON)
SELECT facility_name, COUNT(*) AS cnt
FROM benchmark.encounters_baseline
WHERE encounter_date BETWEEN '2026-01-01' AND '2026-12-31'
GROUP BY facility_name
ORDER BY cnt DESC;

-- OPTIMIZED
EXPLAIN (ANALYZE, FORMAT JSON)
SELECT facility_name, COUNT(*) AS cnt
FROM analytics.encounters
WHERE encounter_date BETWEEN '2026-01-01' AND '2026-12-31'
GROUP BY facility_name
ORDER BY cnt DESC;

-- =========================================================================
-- Q3: Diagnosis frequency lookup by patient — Patient Explorer panel.
-- Optimized table benefits from idx_diagnoses_patient.
-- =========================================================================
-- BASELINE
EXPLAIN (ANALYZE, FORMAT JSON)
SELECT * FROM benchmark.diagnoses_baseline
WHERE patient_id = 'PAT-0005000';

-- OPTIMIZED
EXPLAIN (ANALYZE, FORMAT JSON)
SELECT * FROM analytics.diagnoses
WHERE patient_id = 'PAT-0005000';
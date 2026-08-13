-- Benchmark baseline: plain, unpartitioned, minimally-indexed copies of the
-- two tables under test (encounters, diagnoses), loaded with the exact same
-- rows as the real (partitioned + indexed) analytics tables. Used to measure
-- the actual before/after effect of partitioning + indexing — see
-- docs/performance.md and reports/performance/results.json for the
-- real numbers this produced.

CREATE SCHEMA IF NOT EXISTS benchmark;

DROP TABLE IF EXISTS benchmark.encounters_baseline;
CREATE TABLE benchmark.encounters_baseline (
    encounter_id           VARCHAR(20) PRIMARY KEY,
    patient_id              VARCHAR(20) NOT NULL,
    provider_name             VARCHAR(150),
    specialty                   VARCHAR(100),
    facility_name                 VARCHAR(150),
    encounter_type                  VARCHAR(50),
    department                        VARCHAR(100),
    admission_time                      TIMESTAMPTZ NOT NULL,
    discharge_time                        TIMESTAMPTZ,
    length_of_stay_hours                    NUMERIC(10,2),
    encounter_date                            DATE NOT NULL
);
INSERT INTO benchmark.encounters_baseline
SELECT encounter_id, patient_id, provider_name, specialty, facility_name, encounter_type,
       department, admission_time, discharge_time, length_of_stay_hours, encounter_date
FROM analytics.encounters;

DROP TABLE IF EXISTS benchmark.diagnoses_baseline;
CREATE TABLE benchmark.diagnoses_baseline (
    diagnosis_id            VARCHAR(20) PRIMARY KEY,
    patient_id               VARCHAR(20) NOT NULL,
    encounter_id              VARCHAR(20) NOT NULL,
    diagnosis_code             VARCHAR(20) NOT NULL,
    diagnosis_description       VARCHAR(300)
);
INSERT INTO benchmark.diagnoses_baseline
SELECT diagnosis_id, patient_id, encounter_id, diagnosis_code, diagnosis_description
FROM analytics.diagnoses;

ANALYZE benchmark.encounters_baseline;
ANALYZE benchmark.diagnoses_baseline;
ANALYZE analytics.encounters;
ANALYZE analytics.diagnoses;
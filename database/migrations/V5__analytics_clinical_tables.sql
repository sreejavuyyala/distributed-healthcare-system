-- diagnoses/procedures/labs: normalized, NOT partitioned. At this project's
-- volumes (50K/20K/50K rows) a single indexed table serves both point lookups
-- and aggregate queries fine; these are queried primarily by patient_id or
-- code, not by date range, so partitioning would add planner overhead without
-- a pruning benefit (see docs/database-design.md).
--
-- NOTE: encounter_id below is NOT a foreign key to analytics.encounters.
-- Postgres requires a partitioned table's referenced columns to be covered by
-- a unique index equal to the FK, which conflicts with encounters' composite
-- partition key. Referential integrity to encounters is enforced at the
-- application layer instead; patient_id keeps a real FK since analytics.patients
-- is not partitioned.

CREATE TABLE analytics.diagnoses (
    diagnosis_id            VARCHAR(20) PRIMARY KEY,
    patient_id               VARCHAR(20) NOT NULL REFERENCES analytics.patients(patient_id),
    encounter_id              VARCHAR(20) NOT NULL,
    diagnosis_code             VARCHAR(20) NOT NULL,
    diagnosis_description       VARCHAR(300),
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE analytics.procedures (
    procedure_id              VARCHAR(20) PRIMARY KEY,
    patient_id                 VARCHAR(20) NOT NULL REFERENCES analytics.patients(patient_id),
    encounter_id                 VARCHAR(20) NOT NULL,
    procedure_code                 VARCHAR(20) NOT NULL,
    procedure_description            VARCHAR(300),
    procedure_date                     DATE NOT NULL,
    created_at                           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE analytics.labs (
    lab_id           VARCHAR(20) PRIMARY KEY,
    patient_id       VARCHAR(20) NOT NULL REFERENCES analytics.patients(patient_id),
    test_name        VARCHAR(150) NOT NULL,
    test_result      VARCHAR(100),
    reference_range  VARCHAR(100),
    collected_at     TIMESTAMPTZ NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
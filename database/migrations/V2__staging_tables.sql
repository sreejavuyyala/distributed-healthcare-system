-- Staging tables mirror the raw feed shape closely. Every row carries batch_id +
-- content_hash so a replayed/duplicate feed file is a safe no-op (idempotent upsert
-- on the natural key), not a duplicate row. See docs/data-pipeline.md.
--
-- Only 5 independent feeds in this academic scope: patients, encounters,
-- diagnoses, procedures, labs. Provider/facility are plain descriptive
-- columns on encounters rather than separate feeds/tables.

CREATE TABLE staging.patients (
    patient_id      VARCHAR(20) PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    date_of_birth   DATE NOT NULL,
    gender          VARCHAR(20),
    zip_code        VARCHAR(10),
    batch_id        UUID NOT NULL,
    content_hash    VARCHAR(64) NOT NULL,
    ingested_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE staging.encounters (
    encounter_id    VARCHAR(20) PRIMARY KEY,
    patient_id      VARCHAR(20) NOT NULL,
    provider_name   VARCHAR(150),
    specialty       VARCHAR(100),
    facility_name   VARCHAR(150),
    encounter_type  VARCHAR(50),
    department      VARCHAR(100),
    admission_time  TIMESTAMPTZ NOT NULL,
    discharge_time  TIMESTAMPTZ,
    encounter_date  DATE NOT NULL,
    batch_id        UUID NOT NULL,
    content_hash    VARCHAR(64) NOT NULL,
    ingested_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE staging.diagnoses (
    diagnosis_id           VARCHAR(20) PRIMARY KEY,
    patient_id              VARCHAR(20) NOT NULL,
    encounter_id             VARCHAR(20) NOT NULL,
    diagnosis_code            VARCHAR(20) NOT NULL,
    diagnosis_description      VARCHAR(300),
    batch_id                    UUID NOT NULL,
    content_hash                 VARCHAR(64) NOT NULL,
    ingested_at                   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE staging.procedures (
    procedure_id             VARCHAR(20) PRIMARY KEY,
    patient_id                 VARCHAR(20) NOT NULL,
    encounter_id                 VARCHAR(20) NOT NULL,
    procedure_code                 VARCHAR(20) NOT NULL,
    procedure_description            VARCHAR(300),
    procedure_date                     DATE NOT NULL,
    batch_id                             UUID NOT NULL,
    content_hash                          VARCHAR(64) NOT NULL,
    ingested_at                            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE staging.labs (
    lab_id           VARCHAR(20) PRIMARY KEY,
    patient_id       VARCHAR(20) NOT NULL,
    test_name        VARCHAR(150) NOT NULL,
    test_result      VARCHAR(100),
    reference_range  VARCHAR(100),
    collected_at     TIMESTAMPTZ NOT NULL,
    batch_id         UUID NOT NULL,
    content_hash     VARCHAR(64) NOT NULL,
    ingested_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
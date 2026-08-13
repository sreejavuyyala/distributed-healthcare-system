-- Small, not time-queried, not partitioned — see docs/database-design.md
-- "why only encounters is partitioned".
CREATE TABLE analytics.patients (
    patient_id      VARCHAR(20) PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    date_of_birth   DATE NOT NULL,
    gender          VARCHAR(20),
    zip_code        VARCHAR(10),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
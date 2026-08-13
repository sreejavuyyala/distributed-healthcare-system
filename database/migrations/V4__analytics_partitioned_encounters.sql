-- ENCOUNTERS is the one table partitioned in this project (per the academic
-- scope: "partition ONE meaningful large table, do not partition everything").
--
-- Partition key : encounter_date (DATE, RANGE, yearly)
-- Why           : encounters is the largest, most time-queried table (patient
--                 history, "encounters this year", length-of-stay trends all
--                 filter by date). Yearly partitions let Postgres prune to a
--                 single partition for "this year's encounters" style queries
--                 instead of scanning the whole table.
-- Query pattern : WHERE encounter_date BETWEEN :from AND :to (partition pruning)
--                 WHERE patient_id = :id ORDER BY encounter_date DESC (patient
--                 history — served by the composite index in V7, and still
--                 benefits from pruning whenever a date range is also given).
-- See docs/performance.md for measured before/after query plans.
--
-- Postgres requires the partition key to be part of any unique/primary key on
-- a partitioned table, hence the composite PK (encounter_id, encounter_date)
-- instead of encounter_id alone.

CREATE TABLE analytics.encounters (
    encounter_id           VARCHAR(20)  NOT NULL,
    patient_id              VARCHAR(20)  NOT NULL REFERENCES analytics.patients(patient_id),
    provider_name             VARCHAR(150),
    specialty                   VARCHAR(100),
    facility_name                 VARCHAR(150),
    encounter_type                  VARCHAR(50),
    department                        VARCHAR(100),
    admission_time                      TIMESTAMPTZ NOT NULL,
    discharge_time                        TIMESTAMPTZ,
    length_of_stay_hours                    NUMERIC(10,2),
    encounter_date                            DATE NOT NULL,
    created_at                                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (encounter_id, encounter_date)
) PARTITION BY RANGE (encounter_date);

-- Default partition catches any row outside the pre-created yearly ranges
-- (e.g. bad/late data) instead of failing the insert.
CREATE TABLE analytics.encounters_default PARTITION OF analytics.encounters DEFAULT;

-- Yearly partitions covering the synthetic data's date range plus headroom
-- for future demo runs, e.g. analytics.encounters_2025, analytics.encounters_2026.
DO $$
DECLARE
    start_year INT := 2023;
    end_year   INT := 2029;
    y          INT;
    part_name  TEXT;
BEGIN
    FOR y IN start_year..end_year LOOP
        part_name := 'encounters_' || y;
        EXECUTE format(
            'CREATE TABLE IF NOT EXISTS analytics.%I PARTITION OF analytics.encounters FOR VALUES FROM (%L) TO (%L);',
            part_name, make_date(y, 1, 1), make_date(y + 1, 1, 1)
        );
    END LOOP;
END $$;
-- Indexes chosen from the read path the API + dashboard actually exercise —
-- each one is tied to a specific query, not added speculatively (see
-- docs/performance.md for the measured EXPLAIN ANALYZE before/after these).
--
-- CREATE INDEX on a partitioned parent (encounters) automatically creates a
-- matching index on every existing and future partition.

-- Patient encounter history: GET /api/patients/{id}/encounters (most common
-- single-patient query, always ordered most-recent-first).
CREATE INDEX idx_encounters_patient_date ON analytics.encounters (patient_id, encounter_date DESC);

-- Date-range / "encounters this year" queries independent of patient.
CREATE INDEX idx_encounters_date ON analytics.encounters (encounter_date);

-- "Encounters by facility" analytics chart.
CREATE INDEX idx_encounters_facility ON analytics.encounters (facility_name, encounter_date);

-- Diagnosis frequency / lookup: GET /api/analytics/diagnoses.
CREATE INDEX idx_diagnoses_patient ON analytics.diagnoses (patient_id);
CREATE INDEX idx_diagnoses_code ON analytics.diagnoses (diagnosis_code);

-- Procedure lookups mirror diagnoses usage.
CREATE INDEX idx_procedures_patient ON analytics.procedures (patient_id);
CREATE INDEX idx_procedures_code ON analytics.procedures (procedure_code);

-- Lab trend queries and patient lab history: GET /api/analytics/labs.
CREATE INDEX idx_labs_patient_time ON analytics.labs (patient_id, collected_at DESC);
CREATE INDEX idx_labs_test_name ON analytics.labs (test_name);
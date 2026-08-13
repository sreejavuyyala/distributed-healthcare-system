package com.healthcare.platform.ingestion;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Promotes validated rows from staging.* to the query-optimized analytics.*
 * layer for a single batch. Each feed's transform is a plain
 * INSERT ... SELECT ... ON CONFLICT DO UPDATE, scoped to WHERE batch_id = :batchId
 * so only the rows just ingested are (re)promoted, not the entire staging
 * table. analytics.encounters additionally computes length_of_stay_hours at
 * promotion time — see docs/database-design.md.
 *
 * Feed ingestion order (FeedName enum order) ensures dependency direction:
 * patients are promoted before encounters/diagnoses/procedures/labs, which
 * all reference patient_id.
 */
@Repository
public class AnalyticsTransformRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final Map<FeedName, String> transforms = new EnumMap<>(FeedName.class);

    public AnalyticsTransformRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
        registerTransforms();
    }

    @Transactional
    public int promote(FeedName feed, UUID batchId) {
        String sql = transforms.get(feed);
        if (sql == null) {
            throw new IllegalStateException("No analytics transform registered for feed " + feed);
        }
        return jdbc.update(sql, new MapSqlParameterSource("batchId", batchId));
    }

    private void registerTransforms() {
        transforms.put(FeedName.PATIENTS, """
                INSERT INTO analytics.patients (patient_id, first_name, last_name, date_of_birth, gender, zip_code, updated_at)
                SELECT patient_id, first_name, last_name, date_of_birth, gender, zip_code, now()
                FROM staging.patients WHERE batch_id = :batchId
                ON CONFLICT (patient_id) DO UPDATE SET
                    first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name,
                    date_of_birth = EXCLUDED.date_of_birth, gender = EXCLUDED.gender,
                    zip_code = EXCLUDED.zip_code, updated_at = now()
                """);

        transforms.put(FeedName.ENCOUNTERS, """
                INSERT INTO analytics.encounters
                    (encounter_id, patient_id, provider_name, specialty, facility_name, encounter_type, department,
                     admission_time, discharge_time, length_of_stay_hours, encounter_date)
                SELECT
                    e.encounter_id, e.patient_id, e.provider_name, e.specialty, e.facility_name, e.encounter_type, e.department,
                    e.admission_time, e.discharge_time,
                    CASE WHEN e.discharge_time IS NOT NULL
                         THEN ROUND(EXTRACT(EPOCH FROM (e.discharge_time - e.admission_time)) / 3600.0, 2)
                         ELSE NULL END,
                    e.encounter_date
                FROM staging.encounters e
                WHERE e.batch_id = :batchId
                ON CONFLICT (encounter_id, encounter_date) DO UPDATE SET
                    patient_id = EXCLUDED.patient_id, provider_name = EXCLUDED.provider_name,
                    specialty = EXCLUDED.specialty, facility_name = EXCLUDED.facility_name,
                    encounter_type = EXCLUDED.encounter_type, department = EXCLUDED.department,
                    discharge_time = EXCLUDED.discharge_time, length_of_stay_hours = EXCLUDED.length_of_stay_hours
                """);

        transforms.put(FeedName.DIAGNOSES, """
                INSERT INTO analytics.diagnoses (diagnosis_id, patient_id, encounter_id, diagnosis_code, diagnosis_description)
                SELECT diagnosis_id, patient_id, encounter_id, diagnosis_code, diagnosis_description
                FROM staging.diagnoses WHERE batch_id = :batchId
                ON CONFLICT (diagnosis_id) DO UPDATE SET
                    diagnosis_code = EXCLUDED.diagnosis_code, diagnosis_description = EXCLUDED.diagnosis_description
                """);

        transforms.put(FeedName.PROCEDURES, """
                INSERT INTO analytics.procedures (procedure_id, patient_id, encounter_id, procedure_code, procedure_description, procedure_date)
                SELECT procedure_id, patient_id, encounter_id, procedure_code, procedure_description, procedure_date
                FROM staging.procedures WHERE batch_id = :batchId
                ON CONFLICT (procedure_id) DO UPDATE SET
                    procedure_code = EXCLUDED.procedure_code, procedure_description = EXCLUDED.procedure_description,
                    procedure_date = EXCLUDED.procedure_date
                """);

        transforms.put(FeedName.LABS, """
                INSERT INTO analytics.labs (lab_id, patient_id, test_name, test_result, reference_range, collected_at)
                SELECT lab_id, patient_id, test_name, test_result, reference_range, collected_at
                FROM staging.labs WHERE batch_id = :batchId
                ON CONFLICT (lab_id) DO UPDATE SET
                    test_result = EXCLUDED.test_result, reference_range = EXCLUDED.reference_range
                """);
    }
}
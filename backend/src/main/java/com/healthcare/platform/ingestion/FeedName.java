package com.healthcare.platform.ingestion;

import java.util.List;

import static com.healthcare.platform.ingestion.ColumnSpec.date;
import static com.healthcare.platform.ingestion.ColumnSpec.text;
import static com.healthcare.platform.ingestion.ColumnSpec.timestamp;

/**
 * The 5 independent upstream feeds this platform ingests. Declaration order
 * is also the ingestion dependency order used by {@link IngestionOrchestrator}
 * (patients before encounters/diagnoses/procedures/labs, which reference
 * patient_id) — but each feed still runs as an isolated unit of work: a
 * failure in one does not skip or abort the ones that follow.
 *
 * Provider/facility are carried as plain descriptive columns on the
 * encounters feed rather than their own feeds — this keeps the ingestion
 * model to 5 pipelines while still supporting "encounters by facility" /
 * "provider workload" analytics via a simple GROUP BY.
 */
public enum FeedName {

    PATIENTS(
            "patients.csv", "staging.patients", "patient_id",
            List.of(
                    text("patient_id", true),
                    text("first_name", true),
                    text("last_name", true),
                    date("date_of_birth", true),
                    text("gender", false),
                    text("zip_code", false)
            )
    ),
    ENCOUNTERS(
            "encounters.csv", "staging.encounters", "encounter_id",
            List.of(
                    text("encounter_id", true),
                    text("patient_id", true),
                    text("provider_name", false),
                    text("specialty", false),
                    text("facility_name", false),
                    text("encounter_type", false),
                    text("department", false),
                    timestamp("admission_time", true),
                    timestamp("discharge_time", false),
                    date("encounter_date", true)
            )
    ),
    DIAGNOSES(
            "diagnoses.csv", "staging.diagnoses", "diagnosis_id",
            List.of(
                    text("diagnosis_id", true),
                    text("patient_id", true),
                    text("encounter_id", true),
                    text("diagnosis_code", true),
                    text("diagnosis_description", false)
            )
    ),
    PROCEDURES(
            "procedures.csv", "staging.procedures", "procedure_id",
            List.of(
                    text("procedure_id", true),
                    text("patient_id", true),
                    text("encounter_id", true),
                    text("procedure_code", true),
                    text("procedure_description", false),
                    date("procedure_date", true)
            )
    ),
    LABS(
            "labs.csv", "staging.labs", "lab_id",
            List.of(
                    text("lab_id", true),
                    text("patient_id", true),
                    text("test_name", true),
                    text("test_result", false),
                    text("reference_range", false),
                    timestamp("collected_at", true)
            )
    );

    private final String csvFileName;
    private final String stagingTable;
    private final String naturalKeyColumn;
    private final List<ColumnSpec> columns;

    FeedName(String csvFileName, String stagingTable, String naturalKeyColumn, List<ColumnSpec> columns) {
        this.csvFileName = csvFileName;
        this.stagingTable = stagingTable;
        this.naturalKeyColumn = naturalKeyColumn;
        this.columns = columns;
    }

    public String csvFileName() {
        return csvFileName;
    }

    public String stagingTable() {
        return stagingTable;
    }

    public String naturalKeyColumn() {
        return naturalKeyColumn;
    }

    public List<ColumnSpec> columns() {
        return columns;
    }

    public String feedName() {
        return name().toLowerCase();
    }
}
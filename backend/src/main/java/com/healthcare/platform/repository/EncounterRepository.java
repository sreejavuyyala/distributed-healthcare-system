package com.healthcare.platform.repository;

import com.healthcare.platform.entity.Encounter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read-only repository over analytics.encounters (partitioned by encounter_date).
 * See the Javadoc on {@link Encounter} — writes never go through this repository.
 */
public interface EncounterRepository extends JpaRepository<Encounter, String> {

    // Uses idx_encounters_patient_date (patient_id, encounter_date DESC).
    Page<Encounter> findByPatientIdOrderByAdmissionTimeDesc(String patientId, Pageable pageable);

    Page<Encounter> findAllByOrderByAdmissionTimeDesc(Pageable pageable);
}
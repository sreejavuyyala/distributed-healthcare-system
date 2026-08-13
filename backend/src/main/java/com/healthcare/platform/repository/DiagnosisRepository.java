package com.healthcare.platform.repository;

import com.healthcare.platform.entity.Diagnosis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, String> {
    Page<Diagnosis> findByPatientIdOrderByCreatedAtDesc(String patientId, Pageable pageable);
    Page<Diagnosis> findAllByOrderByDiagnosisIdAsc(Pageable pageable);
}
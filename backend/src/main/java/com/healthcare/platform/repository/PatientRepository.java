package com.healthcare.platform.repository;

import com.healthcare.platform.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, String> {
    Page<Patient> findAllByOrderByPatientIdAsc(Pageable pageable);
}
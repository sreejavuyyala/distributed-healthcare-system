package com.healthcare.platform.repository;

import com.healthcare.platform.entity.Procedure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedureRepository extends JpaRepository<Procedure, String> {
    Page<Procedure> findByPatientIdOrderByProcedureDateDesc(String patientId, Pageable pageable);
}
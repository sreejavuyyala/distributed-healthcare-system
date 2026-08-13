package com.healthcare.platform.repository;

import com.healthcare.platform.entity.Lab;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Read-only repository over the partitioned analytics.labs table — see {@link Lab}. */
public interface LabRepository extends JpaRepository<Lab, String> {
    Page<Lab> findByPatientIdOrderByCollectedAtDesc(String patientId, Pageable pageable);
    Page<Lab> findAllByOrderByCollectedAtDesc(Pageable pageable);
}
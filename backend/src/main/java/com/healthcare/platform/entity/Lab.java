package com.healthcare.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Maps analytics.labs — a plain (not partitioned) table; see V5__analytics_clinical_tables.sql. */
@Entity
@Table(name = "labs", schema = "analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lab {

    @Id
    @Column(name = "lab_id")
    private String labId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "test_result")
    private String testResult;

    @Column(name = "reference_range")
    private String referenceRange;

    @Column(name = "collected_at", nullable = false)
    private OffsetDateTime collectedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
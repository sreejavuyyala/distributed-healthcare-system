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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Maps analytics.encounters, the one table in this project that is
 * PARTITIONED BY RANGE(encounter_date), whose true primary key is the
 * composite (encounter_id, encounter_date) — see
 * V4__analytics_partitioned_encounters.sql. This entity intentionally maps
 * only encounter_id as the JPA @Id and is used for READS ONLY (repository
 * query methods); bulk writes during ingestion go through
 * {@link com.healthcare.platform.ingestion.AnalyticsTransformRepository},
 * which issues raw SQL against the real composite conflict target. Do not
 * call save()/saveAll() on the JPA repository for this entity.
 *
 * Provider/facility are plain descriptive columns here (no separate
 * provider/facility feed or table in this academic scope).
 */
@Entity
@Table(name = "encounters", schema = "analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encounter {

    @Id
    @Column(name = "encounter_id")
    private String encounterId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "facility_name")
    private String facilityName;

    @Column(name = "encounter_type")
    private String encounterType;

    @Column(name = "department")
    private String department;

    @Column(name = "admission_time", nullable = false)
    private OffsetDateTime admissionTime;

    @Column(name = "discharge_time")
    private OffsetDateTime dischargeTime;

    @Column(name = "length_of_stay_hours")
    private BigDecimal lengthOfStayHours;

    @Column(name = "encounter_date", nullable = false)
    private LocalDate encounterDate;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
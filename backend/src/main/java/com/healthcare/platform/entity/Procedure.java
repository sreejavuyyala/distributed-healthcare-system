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

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "procedures", schema = "analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procedure {

    @Id
    @Column(name = "procedure_id")
    private String procedureId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "encounter_id", nullable = false)
    private String encounterId;

    @Column(name = "procedure_code", nullable = false)
    private String procedureCode;

    @Column(name = "procedure_description")
    private String procedureDescription;

    @Column(name = "procedure_date", nullable = false)
    private LocalDate procedureDate;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
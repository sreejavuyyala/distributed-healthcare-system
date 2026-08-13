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

/**
 * Entities in this codebase are intentionally flat (no JPA @ManyToOne/@OneToMany
 * associations). Reads that need a join (e.g. encounter + patient + provider)
 * use an explicit native/JPQL query in the relevant repository instead of
 * relying on lazy-loaded associations, which avoids N+1 query patterns by
 * construction rather than by remembering to add JOIN FETCH everywhere.
 */
@Entity
@Table(name = "patients", schema = "analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
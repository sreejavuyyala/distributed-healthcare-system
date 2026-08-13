package com.healthcare.platform.dto;

import java.time.LocalDate;

public record PatientDto(
        String patientId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String zipCode
) {
}
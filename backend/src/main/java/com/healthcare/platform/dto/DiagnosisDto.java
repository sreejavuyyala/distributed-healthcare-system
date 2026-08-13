package com.healthcare.platform.dto;

public record DiagnosisDto(
        String diagnosisId,
        String patientId,
        String encounterId,
        String diagnosisCode,
        String diagnosisDescription
) {
}
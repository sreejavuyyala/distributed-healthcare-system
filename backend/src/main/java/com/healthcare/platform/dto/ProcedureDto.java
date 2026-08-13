package com.healthcare.platform.dto;

import java.time.LocalDate;

public record ProcedureDto(
        String procedureId,
        String patientId,
        String encounterId,
        String procedureCode,
        String procedureDescription,
        LocalDate procedureDate
) {
}
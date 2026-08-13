package com.healthcare.platform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EncounterDto(
        String encounterId,
        String patientId,
        String providerName,
        String specialty,
        String facilityName,
        String encounterType,
        String department,
        OffsetDateTime admissionTime,
        OffsetDateTime dischargeTime,
        BigDecimal lengthOfStayHours,
        LocalDate encounterDate
) {
}
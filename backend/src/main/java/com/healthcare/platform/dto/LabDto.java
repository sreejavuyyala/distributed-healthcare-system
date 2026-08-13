package com.healthcare.platform.dto;

import java.time.OffsetDateTime;

public record LabDto(
        String labId,
        String patientId,
        String testName,
        String testResult,
        String referenceRange,
        OffsetDateTime collectedAt
) {
}
package com.healthcare.platform.dto;

public record OverviewCountsDto(
        long totalPatients,
        long totalEncounters,
        long totalDiagnoses,
        long totalProcedures,
        long totalLabs
) {
}
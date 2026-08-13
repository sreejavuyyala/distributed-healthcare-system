package com.healthcare.platform.dto;

/** Backs GET /api/metrics — every field is a real computed value, never hardcoded. */
public record MetricsDto(
        long recordsProcessed,
        long successfulFeeds,
        long failedFeeds,
        double averageProcessingTimeMs,
        double averageQueryLatencyMs
) {
}
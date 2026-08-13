package com.healthcare.platform.dto;

import java.time.OffsetDateTime;

/** Latest-execution summary per feed — powers the dashboard's Pipeline Health panel. */
public record FeedStatusDto(
        String feedName,
        String status,
        OffsetDateTime lastRun,
        int recordsProcessed,
        int recordsFailed,
        int retryCount,
        String errorMessage
) {
}
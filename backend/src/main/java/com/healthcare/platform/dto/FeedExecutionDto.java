package com.healthcare.platform.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedExecutionDto(
        Long executionId,
        String feedName,
        UUID batchId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String status,
        int recordsReceived,
        int recordsProcessed,
        int recordsFailed,
        int retryCount,
        String errorMessage
) {
}
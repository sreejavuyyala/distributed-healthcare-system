package com.healthcare.platform.ingestion;

import com.healthcare.platform.audit.FeedExecutionStatus;

import java.util.UUID;

public record IngestionResult(
        Long executionId,
        String feedName,
        UUID batchId,
        FeedExecutionStatus status,
        int recordsReceived,
        int recordsProcessed,
        int recordsFailed,
        int retryCount,
        String errorMessage
) {
}
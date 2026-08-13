package com.healthcare.platform.audit;

import com.healthcare.platform.entity.FeedExecution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Writes audit.feed_execution rows. Uses REQUIRES_NEW so the audit record for
 * a failed feed commits even though the feed's own data transaction rolls
 * back — otherwise a failure would erase the very evidence of the failure.
 */
@Service
public class FeedExecutionService {

    private final FeedExecutionRepository repository;

    public FeedExecutionService(FeedExecutionRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FeedExecution start(String feedName, UUID batchId, String rawFilePath, String contentHash) {
        FeedExecution existing = repository.findByFeedNameAndBatchId(feedName, batchId).orElse(null);
        if (existing != null) {
            if (FeedExecutionStatus.SUCCESS.name().equals(existing.getStatus())) {
                return existing;
            }
            // Re-running a batch that previously FAILED (or is stuck RUNNING from a
            // crash): reset it to a fresh RUNNING attempt rather than leaving stale
            // start_time/error_message from the prior run.
            existing.setStatus(FeedExecutionStatus.RUNNING.name());
            existing.setStartTime(OffsetDateTime.now());
            existing.setEndTime(null);
            existing.setErrorMessage(null);
            return repository.save(existing);
        }
        FeedExecution execution = FeedExecution.builder()
                .feedName(feedName)
                .batchId(batchId)
                .startTime(OffsetDateTime.now())
                .status(FeedExecutionStatus.RUNNING.name())
                .recordsReceived(0)
                .recordsProcessed(0)
                .recordsFailed(0)
                .retryCount(0)
                .rawFilePath(rawFilePath)
                .contentHash(contentHash)
                .build();
        return repository.save(execution);
    }

    /** Forces an execution row back to a fresh RUNNING attempt regardless of its current status — used when a demo-forced failure re-runs a batch that already succeeded (bypassing the idempotent-skip). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FeedExecution resetToRunning(Long executionId) {
        FeedExecution execution = repository.findById(executionId).orElseThrow();
        execution.setStatus(FeedExecutionStatus.RUNNING.name());
        execution.setStartTime(OffsetDateTime.now());
        execution.setEndTime(null);
        execution.setErrorMessage(null);
        return repository.save(execution);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(Long executionId, int recordsReceived, int recordsProcessed, int recordsFailed, int retryCount) {
        FeedExecution execution = repository.findById(executionId).orElseThrow();
        execution.setStatus(FeedExecutionStatus.SUCCESS.name());
        execution.setEndTime(OffsetDateTime.now());
        execution.setRecordsReceived(recordsReceived);
        execution.setRecordsProcessed(recordsProcessed);
        execution.setRecordsFailed(recordsFailed);
        execution.setRetryCount(retryCount);
        repository.save(execution);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long executionId, int recordsReceived, int retryCount, String errorMessage) {
        FeedExecution execution = repository.findById(executionId).orElseThrow();
        execution.setStatus(FeedExecutionStatus.FAILED.name());
        execution.setEndTime(OffsetDateTime.now());
        execution.setRecordsReceived(recordsReceived);
        execution.setRetryCount(retryCount);
        execution.setErrorMessage(errorMessage != null && errorMessage.length() > 2000
                ? errorMessage.substring(0, 2000) : errorMessage);
        repository.save(execution);
    }
}
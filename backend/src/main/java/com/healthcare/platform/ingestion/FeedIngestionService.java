package com.healthcare.platform.ingestion;

import com.healthcare.platform.audit.FeedExecutionService;
import com.healthcare.platform.audit.FeedExecutionStatus;
import com.healthcare.platform.config.PlatformProperties;
import com.healthcare.platform.entity.FeedExecution;
import com.healthcare.platform.ingestion.retry.RetryExecutor;
import com.healthcare.platform.ingestion.retry.RetryOutcome;
import com.healthcare.platform.ingestion.storage.StorageGateway;
import com.healthcare.platform.ingestion.validation.FeedValidator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Ingests exactly one feed, end to end: read -> validate -> land raw file ->
 * upsert staging -> promote to analytics -> record the outcome in
 * audit.feed_execution. Wrapped in exponential-backoff retry. This class
 * never throws for an ordinary ingestion failure — it always returns an
 * {@link IngestionResult}, which is what lets {@link IngestionOrchestrator}
 * isolate one feed's failure from the rest without special-casing exceptions.
 */
@Service
public class FeedIngestionService {

    private static final Logger log = LoggerFactory.getLogger(FeedIngestionService.class);

    private final PlatformProperties properties;
    private final RetryExecutor retryExecutor;
    private final FailureSimulator failureSimulator;
    private final IdempotencyService idempotencyService;
    private final StorageGateway storageGateway;
    private final FeedValidator validator;
    private final StagingRepository stagingRepository;
    private final AnalyticsTransformRepository transformRepository;
    private final FeedExecutionService feedExecutionService;

    public FeedIngestionService(PlatformProperties properties, RetryExecutor retryExecutor,
                                 FailureSimulator failureSimulator, IdempotencyService idempotencyService,
                                 StorageGateway storageGateway, FeedValidator validator,
                                 StagingRepository stagingRepository, AnalyticsTransformRepository transformRepository,
                                 FeedExecutionService feedExecutionService) {
        this.properties = properties;
        this.retryExecutor = retryExecutor;
        this.failureSimulator = failureSimulator;
        this.idempotencyService = idempotencyService;
        this.storageGateway = storageGateway;
        this.validator = validator;
        this.stagingRepository = stagingRepository;
        this.transformRepository = transformRepository;
        this.feedExecutionService = feedExecutionService;
    }

    public IngestionResult ingest(FeedName feed) {
        Path filePath = Path.of(properties.ingestion().feedsDir()).resolve(feed.csvFileName());
        byte[] content;
        try {
            content = Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Feed file not found for {}: {}", feed.feedName(), filePath, e);
            FeedExecution failedStart = feedExecutionService.start(feed.feedName(), UUID.randomUUID(), filePath.toString(), "unavailable");
            feedExecutionService.markFailed(failedStart.getExecutionId(), 0, 0, "Feed file not found: " + filePath);
            return new IngestionResult(failedStart.getExecutionId(), feed.feedName(), failedStart.getBatchId(),
                    FeedExecutionStatus.FAILED, 0, 0, 0, 0, "Feed file not found: " + filePath);
        }

        String contentHash = idempotencyService.sha256Hex(content);
        UUID batchId = idempotencyService.deriveBatchId(feed, contentHash);

        FeedExecution existing = null;
        try {
            existing = feedExecutionService.start(feed.feedName(), batchId, null, contentHash);
        } catch (Exception e) {
            log.warn("Could not check/create feed_execution row up-front, continuing: {}", e.getMessage());
        }
        boolean forcedFailurePending = failureSimulator.hasForcedFailure(feed);
        if (!forcedFailurePending && existing != null && FeedExecutionStatus.SUCCESS.name().equals(existing.getStatus())) {
            log.info("Idempotent skip: feed={} batchId={} already SUCCESS (same content)", feed.feedName(), batchId);
            return new IngestionResult(existing.getExecutionId(), feed.feedName(), batchId, FeedExecutionStatus.SUCCESS,
                    existing.getRecordsReceived(), existing.getRecordsProcessed(), existing.getRecordsFailed(),
                    existing.getRetryCount(), null);
        }
        if (forcedFailurePending && existing != null) {
            // Demo failure simulation must actually re-run even though this exact
            // content already ingested successfully — reset the execution row to a
            // fresh RUNNING attempt instead of taking the idempotent-skip path.
            existing = feedExecutionService.resetToRunning(existing.getExecutionId());
        }

        int maxAttempts = properties.ingestion().maxRetries();
        long initialBackoff = properties.ingestion().initialBackoffMs();
        double multiplier = properties.ingestion().backoffMultiplier();

        RetryOutcome<AttemptResult> outcome = retryExecutor.execute(feed.feedName(), maxAttempts, initialBackoff, multiplier,
                attemptNumber -> {
                    failureSimulator.maybeInjectFailure(feed, attemptNumber);
                    return runAttempt(feed, batchId, content, contentHash);
                });

        Long executionId = existing != null ? existing.getExecutionId() : null;
        if (executionId == null) {
            executionId = feedExecutionService.start(feed.feedName(), batchId, null, contentHash).getExecutionId();
        }

        if (!outcome.failed()) {
            AttemptResult result = outcome.result();
            feedExecutionService.markSuccess(executionId, result.recordsReceived(), result.recordsProcessed(),
                    result.recordsFailed(), outcome.retryCount());
            return new IngestionResult(executionId, feed.feedName(), batchId, FeedExecutionStatus.SUCCESS,
                    result.recordsReceived(), result.recordsProcessed(), result.recordsFailed(), outcome.retryCount(), null);
        } else {
            String message = outcome.error() != null ? outcome.error().getMessage() : "unknown error";
            feedExecutionService.markFailed(executionId, 0, outcome.retryCount(), message);
            return new IngestionResult(executionId, feed.feedName(), batchId, FeedExecutionStatus.FAILED,
                    0, 0, 0, outcome.retryCount(), message);
        }
    }

    private AttemptResult runAttempt(FeedName feed, UUID batchId, byte[] content, String contentHash) throws IOException {
        OffsetDateTime now = OffsetDateTime.now();
        storageGateway.store(feed, batchId, feed.csvFileName(), content, now);

        List<CSVRecord> records;
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new StringReader(new String(content, StandardCharsets.UTF_8)))) {
            records = parser.getRecords();
        }

        Set<String> seenKeys = new HashSet<>();
        List<CSVRecord> validRows = new ArrayList<>();
        int failedRows = 0;
        for (CSVRecord record : records) {
            List<String> errors = validator.validate(feed, record, seenKeys);
            if (errors.isEmpty()) {
                validRows.add(record);
            } else {
                failedRows++;
                log.debug("Row {} in feed {} rejected: {}", record.getRecordNumber(), feed.feedName(), errors);
            }
        }

        stagingRepository.upsertBatch(feed, validRows, batchId, contentHash);
        transformRepository.promote(feed, batchId);

        return new AttemptResult(records.size(), validRows.size(), failedRows);
    }

    private record AttemptResult(int recordsReceived, int recordsProcessed, int recordsFailed) {
    }
}
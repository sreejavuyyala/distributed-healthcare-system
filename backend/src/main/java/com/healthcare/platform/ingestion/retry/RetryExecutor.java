package com.healthcare.platform.ingestion.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Generic exponential-backoff retry executor.
 *
 * Sequence for maxAttempts=3, initialBackoffMs=2000, multiplier=2 (the
 * platform default): attempt 1 -> fail -> wait 2s -> attempt 2 -> fail ->
 * wait 4s -> attempt 3 -> fail -> give up, retryCount=2 recorded on the
 * feed_execution row. See docs/fault-tolerance.md.
 */
@Component
public class RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(RetryExecutor.class);

    private final Sleeper sleeper;

    public RetryExecutor(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    public <T> RetryOutcome<T> execute(String operationLabel, int maxAttempts, long initialBackoffMs,
                                        double backoffMultiplier, RetryableOperation<T> operation) {
        long backoff = initialBackoffMs;
        Exception lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T result = operation.attempt(attempt);
                return RetryOutcome.success(result, attempt - 1);
            } catch (Exception e) {
                lastError = e;
                log.warn("[{}] attempt {}/{} failed: {}", operationLabel, attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        sleeper.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return RetryOutcome.failure(attempt - 1, ie);
                    }
                    backoff = (long) (backoff * backoffMultiplier);
                }
            }
        }
        return RetryOutcome.failure(maxAttempts - 1, lastError);
    }
}
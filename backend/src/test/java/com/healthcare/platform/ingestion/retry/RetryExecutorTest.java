package com.healthcare.platform.ingestion.retry;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the exponential-backoff retry sequence described in
 * docs/fault-tolerance.md: attempt -> fail -> wait -> attempt -> fail -> wait
 * -> attempt -> give up. Uses a no-op Sleeper so the test runs instantly
 * instead of actually waiting seconds.
 */
class RetryExecutorTest {

    private final RetryExecutor retryExecutor = new RetryExecutor(millis -> { /* no-op: don't actually sleep in tests */ });

    @Test
    void succeedsOnFirstAttemptWithoutRetrying() {
        RetryOutcome<String> outcome = retryExecutor.execute("test", 3, 10, 2.0, attempt -> "ok");

        assertThat(outcome.failed()).isFalse();
        assertThat(outcome.result()).isEqualTo("ok");
        assertThat(outcome.retryCount()).isZero();
    }

    @Test
    void retriesUntilSuccessWithinMaxAttempts() {
        AtomicInteger calls = new AtomicInteger(0);

        RetryOutcome<String> outcome = retryExecutor.execute("test", 3, 10, 2.0, attempt -> {
            if (calls.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure on attempt " + attempt);
            }
            return "recovered";
        });

        assertThat(outcome.failed()).isFalse();
        assertThat(outcome.result()).isEqualTo("recovered");
        assertThat(outcome.retryCount()).isEqualTo(2); // 2 failed attempts before success
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void givesUpAfterMaxAttemptsAndReportsError() {
        AtomicInteger calls = new AtomicInteger(0);

        RetryOutcome<String> outcome = retryExecutor.execute("test", 3, 10, 2.0, attempt -> {
            calls.incrementAndGet();
            throw new RuntimeException("permanent failure, attempt " + attempt);
        });

        assertThat(outcome.failed()).isTrue();
        assertThat(outcome.error()).hasMessageContaining("attempt 3");
        assertThat(outcome.retryCount()).isEqualTo(2);
        assertThat(calls.get()).isEqualTo(3); // exactly maxAttempts, no more, no fewer
    }
}
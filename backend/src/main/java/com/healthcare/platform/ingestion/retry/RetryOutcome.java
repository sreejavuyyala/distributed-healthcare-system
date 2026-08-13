package com.healthcare.platform.ingestion.retry;

public record RetryOutcome<T>(T result, int retryCount, Exception error) {

    public boolean failed() {
        return error != null;
    }

    public static <T> RetryOutcome<T> success(T result, int retryCount) {
        return new RetryOutcome<>(result, retryCount, null);
    }

    public static <T> RetryOutcome<T> failure(int retryCount, Exception error) {
        return new RetryOutcome<>(null, retryCount, error);
    }
}
package com.healthcare.platform.ingestion.retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T attempt(int attemptNumber) throws Exception;
}
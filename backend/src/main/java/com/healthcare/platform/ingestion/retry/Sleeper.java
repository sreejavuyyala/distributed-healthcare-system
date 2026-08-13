package com.healthcare.platform.ingestion.retry;

@FunctionalInterface
public interface Sleeper {
    void sleep(long millis) throws InterruptedException;
}
package com.healthcare.platform.ingestion;

/** Thrown by {@link FailureSimulator} to simulate a transient upstream/feed failure. */
public class SimulatedTransientFailureException extends RuntimeException {
    public SimulatedTransientFailureException(String message) {
        super(message);
    }
}
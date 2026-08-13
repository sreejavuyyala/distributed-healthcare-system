package com.healthcare.platform.service;

import com.healthcare.platform.exception.InvalidFeedNameException;
import com.healthcare.platform.ingestion.FailureSimulator;
import com.healthcare.platform.ingestion.FeedName;
import com.healthcare.platform.ingestion.IngestionOrchestrator;
import com.healthcare.platform.ingestion.IngestionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final IngestionOrchestrator orchestrator;
    private final FailureSimulator failureSimulator;

    public IngestionService(IngestionOrchestrator orchestrator, FailureSimulator failureSimulator) {
        this.orchestrator = orchestrator;
        this.failureSimulator = failureSimulator;
    }

    /** Synchronous single-feed run — fast enough (worst case a few retries) to answer inline for the demo panel. */
    public IngestionResult runFeed(String feedName) {
        return orchestrator.runFeed(resolveFeed(feedName));
    }

    /** Runs all 5 feeds. Potentially slow if a feed is retrying with backoff, so this is fire-and-forget; poll GET /api/feeds/status for progress. */
    @Async
    public void runAllFeedsAsync() {
        List<IngestionResult> results = orchestrator.runAllFeeds();
        log.info("Batch ingestion run complete: {} feeds processed", results.size());
    }

    public void configureSimulatedFailure(String feedName, int attemptsToFail) {
        failureSimulator.forceFailure(resolveFeed(feedName), attemptsToFail);
    }

    public void clearSimulatedFailure(String feedName) {
        failureSimulator.clearForcedFailure(resolveFeed(feedName));
    }

    private FeedName resolveFeed(String feedName) {
        try {
            return FeedName.valueOf(feedName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidFeedNameException("Unknown feed name: " + feedName
                    + " (expected one of: " + java.util.Arrays.toString(FeedName.values()) + ")");
        }
    }
}
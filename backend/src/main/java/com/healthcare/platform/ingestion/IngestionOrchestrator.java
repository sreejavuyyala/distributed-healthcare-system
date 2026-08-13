package com.healthcare.platform.ingestion;

import com.healthcare.platform.audit.FeedExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs all 5 feeds and guarantees PER-FEED FAILURE ISOLATION: each feed is
 * wrapped in its own try/catch so an exception (or exhausted retries) in one
 * feed's pipeline can never abort, skip, or delay any other feed. This is the
 * single most important behavior in the whole platform — see
 * docs/fault-tolerance.md and
 * backend/src/test/java/.../ingestion/FeedIsolationTest.java, which asserts
 * this exact property.
 */
@Service
public class IngestionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(IngestionOrchestrator.class);

    private final FeedIngestionService feedIngestionService;

    public IngestionOrchestrator(FeedIngestionService feedIngestionService) {
        this.feedIngestionService = feedIngestionService;
    }

    public List<IngestionResult> runAllFeeds() {
        List<IngestionResult> results = new ArrayList<>();
        for (FeedName feed : FeedName.values()) {
            results.add(runFeedIsolated(feed));
        }
        return results;
    }

    public IngestionResult runFeed(FeedName feed) {
        return runFeedIsolated(feed);
    }

    private IngestionResult runFeedIsolated(FeedName feed) {
        try {
            IngestionResult result = feedIngestionService.ingest(feed);
            log.info("Feed {} finished with status {} ({} processed, {} failed, {} retries)",
                    feed.feedName(), result.status(), result.recordsProcessed(), result.recordsFailed(), result.retryCount());
            return result;
        } catch (Exception unexpected) {
            // Belt-and-suspenders: FeedIngestionService.ingest() is designed to
            // never throw for ordinary failures, but if something truly
            // unexpected happens (e.g. a bug), it still must not take down the
            // other feeds in this batch.
            log.error("Unexpected exception ingesting feed {} — isolated, other feeds continue", feed.feedName(), unexpected);
            return new IngestionResult(null, feed.feedName(), null, FeedExecutionStatus.FAILED,
                    0, 0, 0, 0, "Unexpected error: " + unexpected.getMessage());
        }
    }
}
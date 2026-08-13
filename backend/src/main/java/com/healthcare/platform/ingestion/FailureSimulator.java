package com.healthcare.platform.ingestion;

import com.healthcare.platform.config.PlatformProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Two independent failure-injection mechanisms, both used to demonstrate the
 * retry + per-feed-isolation behavior described in docs/fault-tolerance.md:
 *
 * 1. RANDOM: every ingestion attempt of every feed has a
 *    platform.ingestion.failure-rate (FAILURE_RATE env var) chance of being
 *    injected with a transient failure — models a flaky upstream feed.
 * 2. FORCED: the dashboard's "Pipeline Failure Simulation" panel calls
 *    POST /api/feeds/simulate to deterministically fail a specific feed's
 *    next N attempts (or all of them, exhausting retries), so the demo is
 *    reproducible rather than left to chance.
 */
@Component
public class FailureSimulator {

    private final double randomFailureRate;
    private final Map<FeedName, AtomicInteger> forcedFailureAttemptsRemaining = new ConcurrentHashMap<>();

    public FailureSimulator(PlatformProperties properties) {
        this.randomFailureRate = properties.ingestion().failureRate();
    }

    /** Forces the next {@code attemptsToFail} attempts of this feed to fail. Pass a large number to exhaust all retries. */
    public void forceFailure(FeedName feed, int attemptsToFail) {
        forcedFailureAttemptsRemaining.put(feed, new AtomicInteger(attemptsToFail));
    }

    public void clearForcedFailure(FeedName feed) {
        forcedFailureAttemptsRemaining.remove(feed);
    }

    public boolean hasForcedFailure(FeedName feed) {
        AtomicInteger remaining = forcedFailureAttemptsRemaining.get(feed);
        return remaining != null && remaining.get() > 0;
    }

    /** Called at the start of every ingestion attempt; throws if this attempt should simulate a failure. */
    public void maybeInjectFailure(FeedName feed, int attemptNumber) {
        AtomicInteger forced = forcedFailureAttemptsRemaining.get(feed);
        if (forced != null && forced.get() > 0) {
            forced.decrementAndGet();
            throw new SimulatedTransientFailureException(
                    "Forced failure (demo simulation) for feed=" + feed.feedName() + ", attempt " + attemptNumber);
        }
        if (randomFailureRate > 0 && ThreadLocalRandom.current().nextDouble() < randomFailureRate) {
            throw new SimulatedTransientFailureException(
                    "Randomly injected transient failure (FAILURE_RATE=" + randomFailureRate
                            + ") for feed=" + feed.feedName() + ", attempt " + attemptNumber);
        }
    }
}
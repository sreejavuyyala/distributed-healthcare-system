# Fault Tolerance

## The core property: per-feed failure isolation

**A failure in the Encounters feed must not stop Patients, Diagnoses,
Procedures, or Labs from processing.** This is the single most important
distributed-systems property this project demonstrates, and it is proven
three separate ways:

1. **Unit test** — `backend/src/test/java/.../ingestion/FeedIsolationTest.java`:
   mocks `FeedIngestionService` so Encounters returns `FAILED` and the
   other 4 feeds return `SUCCESS`, then asserts the orchestrator still
   ran all 5 and reports the correct status for each.
2. **Integration test** — `backend/src/test/java/.../IngestionIntegrationTest.java`:
   the same property, against a real local PostgreSQL database, with a
   real (short, test-configured) retry sequence.
3. **Live demo** — the dashboard's Failure Simulation panel
   (`POST /api/feeds/simulate`) forces a real failure against the real
   running system. Captured output from an actual run:

   ```
   Patients    SUCCESS   10,000 processed   0 retries
   Encounters  FAILED         0 processed   2 retries
   Diagnoses   SUCCESS   50,000 processed   0 retries
   Procedures  SUCCESS   20,000 processed   0 retries
   Labs        SUCCESS   50,000 processed   0 retries
   ```

## How isolation is implemented

`IngestionOrchestrator.runAllFeeds()`:

```java
for (FeedName feed : FeedName.values()) {
    try {
        results.add(feedIngestionService.ingest(feed));
    } catch (Exception unexpected) {
        // belt-and-suspenders: ingest() is designed to never throw for an
        // ordinary failure, but even an unexpected exception here is caught
        // and recorded, not allowed to abort the loop.
        results.add(failedResult(feed, unexpected));
    }
}
```

Three things make this actually independent, not just superficially
wrapped in a try/catch:

- **Separate retry state per feed.** Each feed's `RetryExecutor.execute(...)`
  call has its own attempt counter and backoff clock.
- **Separate database transaction per feed.** A rollback in the
  Encounters transform cannot roll back Patients' already-committed rows.
- **Separate audit row per feed, per batch**, written with
  `Propagation.REQUIRES_NEW` specifically so the audit record survives
  even when the surrounding ingestion transaction fails — otherwise the
  evidence of the failure would disappear along with the failed write.

## Retry strategy

Exponential backoff, defaults (`.env.example`): `MAX_RETRIES=3`,
`INITIAL_BACKOFF_MS=2000`, `BACKOFF_MULTIPLIER=2`.

```
attempt 1 → fail → wait 2s
attempt 2 → fail → wait 4s
attempt 3 → fail → give up → status=FAILED, retryCount=2
```

Implemented in `RetryExecutor` (generic, feed-agnostic) and unit-tested
in `RetryExecutorTest` with a no-op `Sleeper` so the test suite doesn't
actually wait — see [`docs/testing.md`](testing.md).

## Idempotency and recovery

Failures are also **recoverable**: because raw files are immutable and
`batch_id` is derived deterministically from content, re-running a failed
feed (after clearing a simulated failure, or once the real transient
issue is resolved) picks the same batch back up. `FeedExecutionService.start()`
resets a previously-`FAILED` (or stuck-`RUNNING`) execution row to a
fresh `RUNNING` attempt rather than leaving stale timestamps — verified
in the demo flow (clear the simulated failure, re-run, feed returns to
`SUCCESS` on the same batch ID). See [`docs/data-pipeline.md`](data-pipeline.md#5-idempotency).

## What this deliberately does not cover

This project demonstrates isolation and retry at the **feed/pipeline**
level, inside one Spring Boot process. It does not implement
process-level fault tolerance (e.g. surviving the JVM itself crashing
mid-batch) or distributed consensus — those are out of scope for the
academic objective here (see [`docs/performance.md`](performance.md) and
the README's Limitations section for what's intentionally left out).
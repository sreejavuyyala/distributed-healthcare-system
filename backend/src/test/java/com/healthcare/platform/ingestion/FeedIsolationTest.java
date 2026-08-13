package com.healthcare.platform.ingestion;

import com.healthcare.platform.audit.FeedExecutionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * THE core distributed-systems requirement of this project, proven as an
 * automated test: a failure in the Encounters feed must not stop Patients,
 * Diagnoses, Procedures, or Labs from processing. See docs/fault-tolerance.md.
 */
class FeedIsolationTest {

    @Test
    void encounterFeedFailureDoesNotStopOtherFeeds() {
        FeedIngestionService ingestionService = mock(FeedIngestionService.class);

        when(ingestionService.ingest(eq(FeedName.PATIENTS)))
                .thenReturn(success(FeedName.PATIENTS, 10_000));
        when(ingestionService.ingest(eq(FeedName.ENCOUNTERS)))
                .thenReturn(failure(FeedName.ENCOUNTERS, "simulated transient failure, retries exhausted"));
        when(ingestionService.ingest(eq(FeedName.DIAGNOSES)))
                .thenReturn(success(FeedName.DIAGNOSES, 50_000));
        when(ingestionService.ingest(eq(FeedName.PROCEDURES)))
                .thenReturn(success(FeedName.PROCEDURES, 20_000));
        when(ingestionService.ingest(eq(FeedName.LABS)))
                .thenReturn(success(FeedName.LABS, 50_000));

        IngestionOrchestrator orchestrator = new IngestionOrchestrator(ingestionService);

        List<IngestionResult> results = orchestrator.runAllFeeds();

        // All 5 feeds ran — the batch was not aborted after Encounters failed.
        assertThat(results).hasSize(5);

        assertThat(resultFor(results, FeedName.PATIENTS).status()).isEqualTo(FeedExecutionStatus.SUCCESS);
        assertThat(resultFor(results, FeedName.ENCOUNTERS).status()).isEqualTo(FeedExecutionStatus.FAILED);
        assertThat(resultFor(results, FeedName.DIAGNOSES).status()).isEqualTo(FeedExecutionStatus.SUCCESS);
        assertThat(resultFor(results, FeedName.PROCEDURES).status()).isEqualTo(FeedExecutionStatus.SUCCESS);
        assertThat(resultFor(results, FeedName.LABS).status()).isEqualTo(FeedExecutionStatus.SUCCESS);

        assertThat(resultFor(results, FeedName.PATIENTS).recordsProcessed()).isEqualTo(10_000);
        assertThat(resultFor(results, FeedName.DIAGNOSES).recordsProcessed()).isEqualTo(50_000);
    }

    @Test
    void unexpectedExceptionFromOneFeedIsIsolatedToo() {
        // Belt-and-suspenders: even if FeedIngestionService throws instead of
        // returning a FAILED result, the orchestrator must still isolate it.
        FeedIngestionService ingestionService = mock(FeedIngestionService.class);
        when(ingestionService.ingest(eq(FeedName.PATIENTS))).thenReturn(success(FeedName.PATIENTS, 1));
        when(ingestionService.ingest(eq(FeedName.ENCOUNTERS))).thenThrow(new RuntimeException("boom"));
        when(ingestionService.ingest(eq(FeedName.DIAGNOSES))).thenReturn(success(FeedName.DIAGNOSES, 1));
        when(ingestionService.ingest(eq(FeedName.PROCEDURES))).thenReturn(success(FeedName.PROCEDURES, 1));
        when(ingestionService.ingest(eq(FeedName.LABS))).thenReturn(success(FeedName.LABS, 1));

        IngestionOrchestrator orchestrator = new IngestionOrchestrator(ingestionService);

        List<IngestionResult> results = orchestrator.runAllFeeds();

        assertThat(results).hasSize(5);
        assertThat(resultFor(results, FeedName.ENCOUNTERS).status()).isEqualTo(FeedExecutionStatus.FAILED);
        assertThat(resultFor(results, FeedName.DIAGNOSES).status()).isEqualTo(FeedExecutionStatus.SUCCESS);
        assertThat(resultFor(results, FeedName.PROCEDURES).status()).isEqualTo(FeedExecutionStatus.SUCCESS);
        assertThat(resultFor(results, FeedName.LABS).status()).isEqualTo(FeedExecutionStatus.SUCCESS);
        assertThat(resultFor(results, FeedName.PATIENTS).status()).isEqualTo(FeedExecutionStatus.SUCCESS);
    }

    private IngestionResult resultFor(List<IngestionResult> results, FeedName feed) {
        return results.stream()
                .filter(r -> r.feedName().equals(feed.feedName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No result for feed " + feed));
    }

    private IngestionResult success(FeedName feed, int records) {
        return new IngestionResult(1L, feed.feedName(), java.util.UUID.randomUUID(), FeedExecutionStatus.SUCCESS,
                records, records, 0, 0, null);
    }

    private IngestionResult failure(FeedName feed, String message) {
        return new IngestionResult(2L, feed.feedName(), java.util.UUID.randomUUID(), FeedExecutionStatus.FAILED,
                0, 0, 0, 2, message);
    }
}
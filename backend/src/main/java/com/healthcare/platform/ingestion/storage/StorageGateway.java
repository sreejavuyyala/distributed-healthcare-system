package com.healthcare.platform.ingestion.storage;

import com.healthcare.platform.ingestion.FeedName;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Raw landing-zone abstraction. Every ingested feed file is written here,
 * immutably, before any validation/transformation happens — this is what
 * makes a failed transform recoverable (re-run against the same raw file)
 * rather than a permanent data loss.
 *
 * Two implementations back this interface: {@link LocalFileSystemStorageGateway}
 * (default, used for the local/Docker demo) and {@link AzureBlobStorageGateway}
 * (production target, selected via platform.storage.provider=azure). Swapping
 * providers requires zero changes to ingestion business logic.
 */
public interface StorageGateway {

    /**
     * Stores raw feed content under a hierarchical, date-partitioned path:
     * {@code <feed>/year=YYYY/month=MM/day=DD/<batchId>/<originalFileName>}.
     *
     * @return the storage-relative path the content was written to.
     */
    String store(FeedName feed, UUID batchId, String originalFileName, byte[] content, OffsetDateTime ingestedAt);

    default String buildPath(FeedName feed, UUID batchId, String originalFileName, OffsetDateTime ingestedAt) {
        return "%s/year=%04d/month=%02d/day=%02d/%s/%s".formatted(
                feed.feedName(),
                ingestedAt.getYear(), ingestedAt.getMonthValue(), ingestedAt.getDayOfMonth(),
                batchId, originalFileName
        );
    }
}
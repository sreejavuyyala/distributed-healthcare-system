package com.healthcare.platform.ingestion;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Proves the idempotency mechanism: same content -> same hash -> same batch ID, every time. */
class IdempotencyServiceTest {

    private final IdempotencyService service = new IdempotencyService();

    @Test
    void sameContentProducesSameHash() {
        byte[] content = "patient_id,first_name\nPAT-1,Jane\n".getBytes(StandardCharsets.UTF_8);

        String hash1 = service.sha256Hex(content);
        String hash2 = service.sha256Hex(content);

        assertThat(hash1).isEqualTo(hash2).hasSize(64);
    }

    @Test
    void differentContentProducesDifferentHash() {
        byte[] content1 = "a".getBytes(StandardCharsets.UTF_8);
        byte[] content2 = "b".getBytes(StandardCharsets.UTF_8);

        assertThat(service.sha256Hex(content1)).isNotEqualTo(service.sha256Hex(content2));
    }

    @Test
    void sameFeedAndHashAlwaysDeriveTheSameBatchId() {
        String hash = "abc123";

        UUID batchId1 = service.deriveBatchId(FeedName.ENCOUNTERS, hash);
        UUID batchId2 = service.deriveBatchId(FeedName.ENCOUNTERS, hash);

        assertThat(batchId1).isEqualTo(batchId2);
    }

    @Test
    void differentFeedsWithSameHashDeriveDifferentBatchIds() {
        String hash = "abc123";

        UUID encountersBatch = service.deriveBatchId(FeedName.ENCOUNTERS, hash);
        UUID labsBatch = service.deriveBatchId(FeedName.LABS, hash);

        assertThat(encountersBatch).isNotEqualTo(labsBatch);
    }
}
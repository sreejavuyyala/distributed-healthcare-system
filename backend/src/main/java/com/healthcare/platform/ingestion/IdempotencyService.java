package com.healthcare.platform.ingestion;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Idempotency strategy (see docs/ingestion.md):
 *   1. Hash the raw file content (SHA-256) -> content_hash.
 *   2. Derive a deterministic batch_id from (feed name + content_hash), so the
 *      *same bytes delivered twice always resolve to the same batch_id.
 *   3. audit.feed_execution has UNIQUE(feed_name, batch_id) — a caller can
 *      check for an existing SUCCESS execution for that batch_id and skip
 *      reprocessing entirely.
 *   4. Even if reprocessing does happen, staging tables upsert on natural key
 *      (ON CONFLICT ... DO UPDATE), so replays never create duplicate rows.
 */
@Component
public class IdempotencyService {

    public String sha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public UUID deriveBatchId(FeedName feed, String contentHash) {
        String seed = feed.feedName() + ":" + contentHash;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
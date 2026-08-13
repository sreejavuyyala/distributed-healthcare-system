package com.healthcare.platform.ingestion.storage;

import com.healthcare.platform.config.PlatformProperties;
import com.healthcare.platform.ingestion.FeedName;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Local-filesystem raw landing zone: production target is Azure Blob Storage
 * (see {@link AzureBlobStorageGateway}), but this local implementation makes
 * the exact same partitioned-path behavior runnable with zero cloud account.
 */
@Component
@ConditionalOnProperty(prefix = "platform.storage", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalFileSystemStorageGateway implements StorageGateway {

    private final Path rootDir;

    public LocalFileSystemStorageGateway(PlatformProperties properties) {
        this.rootDir = Path.of(properties.storage().local().rootDir());
    }

    @Override
    public String store(FeedName feed, UUID batchId, String originalFileName, byte[] content, OffsetDateTime ingestedAt) {
        String relativePath = buildPath(feed, batchId, originalFileName, ingestedAt);
        Path target = rootDir.resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write raw landing-zone file: " + target, e);
        }
        return relativePath;
    }
}
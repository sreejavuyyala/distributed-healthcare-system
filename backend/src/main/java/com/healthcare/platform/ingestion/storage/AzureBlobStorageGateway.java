package com.healthcare.platform.ingestion.storage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.healthcare.platform.config.PlatformProperties;
import com.healthcare.platform.ingestion.FeedName;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Production raw landing-zone implementation backed by Azure Blob Storage.
 *
 * This is real, compiling code against the Azure SDK — it is the intended
 * production implementation of {@link StorageGateway} — but it is NOT
 * exercised by this repository's tests or local demo: doing so would require
 * a live Azure Storage Account and connection string, which this portfolio
 * project does not provision. See docs/deployment.md for what deploying this
 * for real would involve. Activated only when platform.storage.provider=azure
 * (the "azure" Spring profile), which is not the default/tested path.
 */
@Component
@ConditionalOnProperty(prefix = "platform.storage", name = "provider", havingValue = "azure")
public class AzureBlobStorageGateway implements StorageGateway {

    private final PlatformProperties properties;
    private BlobContainerClient containerClient;

    public AzureBlobStorageGateway(PlatformProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        String connectionString = properties.storage().azure().connectionString();
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException(
                    "AZURE_STORAGE_CONNECTION_STRING is required when platform.storage.provider=azure");
        }
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.containerClient = serviceClient.getBlobContainerClient(properties.storage().azure().container());
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    @Override
    public String store(FeedName feed, UUID batchId, String originalFileName, byte[] content, OffsetDateTime ingestedAt) {
        String blobPath = buildPath(feed, batchId, originalFileName, ingestedAt);
        containerClient.getBlobClient(blobPath)
                .upload(new ByteArrayInputStream(content), content.length, true);
        return blobPath;
    }
}
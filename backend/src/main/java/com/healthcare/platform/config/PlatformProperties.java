package com.healthcare.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "platform")
public record PlatformProperties(Cors cors, Ingestion ingestion, Storage storage) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Ingestion(
            String feedsDir,
            double failureRate,
            int maxRetries,
            long initialBackoffMs,
            double backoffMultiplier
    ) {
    }

    public record Storage(String provider, Local local, Azure azure) {

        public record Local(String rootDir) {
        }

        public record Azure(String connectionString, String container) {
        }
    }
}
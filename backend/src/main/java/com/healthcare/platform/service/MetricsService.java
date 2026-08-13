package com.healthcare.platform.service;

import com.healthcare.platform.audit.FeedExecutionRepository;
import com.healthcare.platform.audit.FeedExecutionStatus;
import com.healthcare.platform.dto.MetricsDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * GET /api/metrics — every field below is computed from live data at request
 * time (Phase 20), never hardcoded. averageQueryLatencyMs is measured by
 * timing a small, representative sample of the platform's actual indexed
 * queries against the current database, right now, rather than being a
 * fixed/assumed number.
 */
@Service
public class MetricsService {

    private final FeedExecutionRepository feedExecutionRepository;
    private final JdbcTemplate jdbc;

    public MetricsService(FeedExecutionRepository feedExecutionRepository, JdbcTemplate jdbcTemplate) {
        this.feedExecutionRepository = feedExecutionRepository;
        this.jdbc = jdbcTemplate;
    }

    public MetricsDto currentMetrics() {
        long recordsProcessed = feedExecutionRepository.sumRecordsProcessed();
        long successfulFeeds = feedExecutionRepository.countByStatus(FeedExecutionStatus.SUCCESS.name());
        long failedFeeds = feedExecutionRepository.countByStatus(FeedExecutionStatus.FAILED.name());
        double avgProcessingTimeMs = feedExecutionRepository.averageProcessingTimeMs();
        double avgQueryLatencyMs = measureQueryLatencyMs();
        return new MetricsDto(recordsProcessed, successfulFeeds, failedFeeds, round2(avgProcessingTimeMs), round2(avgQueryLatencyMs));
    }

    private double measureQueryLatencyMs() {
        String[] probes = {
                "SELECT COUNT(*) FROM analytics.patients",
                "SELECT COUNT(*) FROM analytics.encounters WHERE encounter_date >= current_date - interval '30 days'",
                "SELECT diagnosis_code, COUNT(*) FROM analytics.diagnoses GROUP BY diagnosis_code ORDER BY COUNT(*) DESC LIMIT 10"
        };
        long totalNanos = 0;
        int samples = 0;
        for (String sql : probes) {
            long start = System.nanoTime();
            jdbc.queryForList(sql);
            totalNanos += (System.nanoTime() - start);
            samples++;
        }
        return samples == 0 ? 0 : (totalNanos / (double) samples) / 1_000_000.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
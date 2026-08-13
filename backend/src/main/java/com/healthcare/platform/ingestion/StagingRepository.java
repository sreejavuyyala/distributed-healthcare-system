package com.healthcare.platform.ingestion;

import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Generic idempotent upsert into a staging.* table, driven entirely by
 * {@link FeedName}'s column metadata rather than one hand-written class per
 * feed — staging tables are structurally identical (natural key + typed
 * columns + batch_id/content_hash/ingested_at), so the SQL is generated once
 * and reused for all 5 feeds. ON CONFLICT (natural_key) DO UPDATE is what
 * makes replaying the same file idempotent (see IdempotencyService).
 */
@Repository
public class StagingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StagingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Transactional
    public int upsertBatch(FeedName feed, List<CSVRecord> validRows, UUID batchId, String contentHash) {
        if (validRows.isEmpty()) {
            return 0;
        }
        String sql = buildUpsertSql(feed);
        MapSqlParameterSource[] batchParams = validRows.stream()
                .map(row -> toParams(feed, row, batchId, contentHash))
                .toArray(MapSqlParameterSource[]::new);
        int[] results = jdbc.batchUpdate(sql, batchParams);
        return results.length;
    }

    private String buildUpsertSql(FeedName feed) {
        List<String> columnNames = feed.columns().stream().map(ColumnSpec::name).toList();
        String insertColumns = String.join(", ", columnNames) + ", batch_id, content_hash, ingested_at";
        String insertValues = columnNames.stream().map(c -> ":" + c).collect(Collectors.joining(", "))
                + ", :batchId, :contentHash, now()";
        String updateAssignments = columnNames.stream()
                .filter(c -> !c.equals(feed.naturalKeyColumn()))
                .map(c -> c + " = EXCLUDED." + c)
                .collect(Collectors.joining(", "))
                + ", batch_id = EXCLUDED.batch_id, content_hash = EXCLUDED.content_hash, ingested_at = EXCLUDED.ingested_at";

        return "INSERT INTO " + feed.stagingTable() + " (" + insertColumns + ") VALUES (" + insertValues + ") "
                + "ON CONFLICT (" + feed.naturalKeyColumn() + ") DO UPDATE SET " + updateAssignments;
    }

    private MapSqlParameterSource toParams(FeedName feed, CSVRecord row, UUID batchId, String contentHash) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        for (ColumnSpec column : feed.columns()) {
            String raw = row.isSet(column.name()) ? row.get(column.name()) : null;
            boolean blank = raw == null || raw.isBlank();
            Object value;
            int sqlType;
            switch (column.type()) {
                case DATE -> {
                    value = blank ? null : LocalDate.parse(raw);
                    sqlType = Types.DATE;
                }
                case TIMESTAMPTZ -> {
                    value = blank ? null : parseTimestamp(raw);
                    sqlType = Types.TIMESTAMP_WITH_TIMEZONE;
                }
                default -> {
                    value = blank ? null : raw;
                    sqlType = Types.VARCHAR;
                }
            }
            params.addValue(column.name(), value, sqlType);
        }
        params.addValue("batchId", batchId);
        params.addValue("contentHash", contentHash);
        return params;
    }

    private OffsetDateTime parseTimestamp(String raw) {
        try {
            return OffsetDateTime.parse(raw);
        } catch (Exception e) {
            return LocalDateTime.parse(raw).atOffset(ZoneOffset.UTC);
        }
    }

    /** Row counts currently in staging for a feed, used by dashboard "Data Quality" panel. */
    public long countStagingRows(FeedName feed) {
        Long count = jdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM " + feed.stagingTable(), Long.class);
        return count == null ? 0 : count;
    }

    public Map<String, Object> lastIngestedRow(FeedName feed) {
        return jdbc.getJdbcTemplate().queryForMap(
                "SELECT * FROM " + feed.stagingTable() + " ORDER BY ingested_at DESC LIMIT 1");
    }
}
package com.healthcare.platform.ingestion.validation;

import com.healthcare.platform.ingestion.ColumnSpec;
import com.healthcare.platform.ingestion.FeedName;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Row-level schema + business-rule validation (Phase 27): required columns,
 * null handling, date parsing, and duplicate natural keys within a batch.
 * A row that fails validation is counted in records_failed and excluded from
 * staging — it does not fail the whole batch (see FeedIngestionService).
 */
@Component
public class FeedValidator {

    public List<String> validate(FeedName feed, CSVRecord record, Set<String> seenKeysInBatch) {
        List<String> errors = new ArrayList<>();

        for (ColumnSpec column : feed.columns()) {
            String raw = record.isSet(column.name()) ? record.get(column.name()) : null;
            boolean blank = raw == null || raw.isBlank();

            if (column.required() && blank) {
                errors.add("missing required field '" + column.name() + "'");
                continue;
            }
            if (blank) {
                continue;
            }
            switch (column.type()) {
                case DATE -> {
                    try {
                        LocalDate.parse(raw);
                    } catch (DateTimeParseException e) {
                        errors.add("invalid date in '" + column.name() + "': " + raw);
                    }
                }
                case TIMESTAMPTZ -> {
                    if (!tryParseTimestamp(raw)) {
                        errors.add("invalid timestamp in '" + column.name() + "': " + raw);
                    }
                }
                case STRING -> {
                    // no-op: presence already checked above
                }
            }
        }

        if (errors.isEmpty()) {
            String naturalKey = record.isSet(feed.naturalKeyColumn()) ? record.get(feed.naturalKeyColumn()) : null;
            if (naturalKey != null && !seenKeysInBatch.add(naturalKey)) {
                errors.add("duplicate natural key '" + naturalKey + "' within batch");
            }
        }

        return errors;
    }

    private boolean tryParseTimestamp(String raw) {
        try {
            OffsetDateTime.parse(raw);
            return true;
        } catch (DateTimeParseException e) {
            // fall through to try a bare local-datetime (our generator emits
            // ISO local timestamps without an offset, e.g. 2026-01-05T10:15:00)
        }
        try {
            java.time.LocalDateTime.parse(raw);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
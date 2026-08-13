package com.healthcare.platform.ingestion;

/**
 * Describes one CSV/staging column: its name, its JDBC-bindable type, and
 * whether it is required (drives both validation and prepared-statement
 * binding in {@link StagingRepository}).
 */
public record ColumnSpec(String name, ColumnType type, boolean required) {

    public static ColumnSpec text(String name, boolean required) {
        return new ColumnSpec(name, ColumnType.STRING, required);
    }

    public static ColumnSpec date(String name, boolean required) {
        return new ColumnSpec(name, ColumnType.DATE, required);
    }

    public static ColumnSpec timestamp(String name, boolean required) {
        return new ColumnSpec(name, ColumnType.TIMESTAMPTZ, required);
    }

    public enum ColumnType {
        STRING, DATE, TIMESTAMPTZ
    }
}
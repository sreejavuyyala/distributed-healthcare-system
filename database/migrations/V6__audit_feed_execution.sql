-- Central audit trail for every ingestion attempt of every feed. This table is
-- what makes per-feed failure isolation *observable*: one row per attempt, so
-- a failed Encounters run and three successful sibling runs in the same batch
-- window are plainly visible side by side (see docs/fault-tolerance.md).

CREATE TABLE audit.feed_execution (
    execution_id        BIGSERIAL PRIMARY KEY,
    feed_name             VARCHAR(50) NOT NULL,
    batch_id               UUID NOT NULL,
    start_time               TIMESTAMPTZ NOT NULL DEFAULT now(),
    end_time                  TIMESTAMPTZ,
    status                     VARCHAR(20) NOT NULL DEFAULT 'RUNNING'
                                CHECK (status IN ('RUNNING', 'SUCCESS', 'FAILED')),
    records_received            INTEGER NOT NULL DEFAULT 0,
    records_processed             INTEGER NOT NULL DEFAULT 0,
    records_failed                  INTEGER NOT NULL DEFAULT 0,
    retry_count                       INTEGER NOT NULL DEFAULT 0,
    error_message                      TEXT,
    content_hash                        VARCHAR(64),
    raw_file_path                        TEXT,
    UNIQUE (feed_name, batch_id)
);

CREATE INDEX idx_feed_execution_feed_time ON audit.feed_execution (feed_name, start_time DESC);
CREATE INDEX idx_feed_execution_status ON audit.feed_execution (status);
CREATE INDEX idx_feed_execution_batch ON audit.feed_execution (batch_id);
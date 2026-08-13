package com.healthcare.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "feed_execution", schema = "audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long executionId;

    @Column(name = "feed_name", nullable = false)
    private String feedName;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "records_received")
    private Integer recordsReceived;

    @Column(name = "records_processed")
    private Integer recordsProcessed;

    @Column(name = "records_failed")
    private Integer recordsFailed;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "content_hash")
    private String contentHash;

    @Column(name = "raw_file_path")
    private String rawFilePath;
}
package com.healthcare.platform.audit;

import com.healthcare.platform.entity.FeedExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedExecutionRepository extends JpaRepository<FeedExecution, Long> {

    Optional<FeedExecution> findByFeedNameAndBatchId(String feedName, UUID batchId);

    Page<FeedExecution> findAllByOrderByStartTimeDesc(Pageable pageable);

    List<FeedExecution> findByFeedNameOrderByStartTimeDesc(String feedName);

    @Query("""
            select fe from FeedExecution fe
            where fe.executionId in (
                select max(fe2.executionId) from FeedExecution fe2 group by fe2.feedName
            )
            order by fe.feedName
            """)
    List<FeedExecution> findLatestPerFeed();

    @Query("select count(fe) from FeedExecution fe where fe.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("select coalesce(sum(fe.recordsProcessed), 0) from FeedExecution fe where fe.status = 'SUCCESS'")
    long sumRecordsProcessed();

    @Query(value = "select coalesce(avg(extract(epoch from (end_time - start_time)) * 1000), 0) " +
            "from audit.feed_execution where end_time is not null", nativeQuery = true)
    double averageProcessingTimeMs();
}
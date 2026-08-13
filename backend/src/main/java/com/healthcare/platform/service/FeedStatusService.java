package com.healthcare.platform.service;

import com.healthcare.platform.audit.FeedExecutionRepository;
import com.healthcare.platform.dto.FeedExecutionDto;
import com.healthcare.platform.dto.FeedStatusDto;
import com.healthcare.platform.mapper.EntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/** Backs the dashboard's Pipeline Health panel: GET /api/feeds/status and GET /api/feeds/executions. */
@Service
public class FeedStatusService {

    private final FeedExecutionRepository repository;
    private final EntityMapper mapper;

    public FeedStatusService(FeedExecutionRepository repository, EntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<FeedStatusDto> latestStatusPerFeed() {
        return repository.findLatestPerFeed().stream()
                .map(fe -> new FeedStatusDto(fe.getFeedName(), fe.getStatus(), fe.getStartTime(),
                        nz(fe.getRecordsProcessed()), nz(fe.getRecordsFailed()), nz(fe.getRetryCount()), fe.getErrorMessage()))
                .toList();
    }

    public Page<FeedExecutionDto> executions(Pageable pageable) {
        return repository.findAllByOrderByStartTimeDesc(pageable).map(mapper::toDto);
    }

    public List<FeedExecutionDto> executionsForFeed(String feedName) {
        return repository.findByFeedNameOrderByStartTimeDesc(feedName).stream().map(mapper::toDto).toList();
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
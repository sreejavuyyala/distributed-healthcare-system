package com.healthcare.platform.controller;

import com.healthcare.platform.dto.FeedExecutionDto;
import com.healthcare.platform.dto.FeedStatusDto;
import com.healthcare.platform.service.FeedStatusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Powers the dashboard's Pipeline Health panel — the per-feed audit trail proving failure isolation. */
@RestController
@RequestMapping("/api/feeds")
public class FeedController {

    private final FeedStatusService feedStatusService;

    public FeedController(FeedStatusService feedStatusService) {
        this.feedStatusService = feedStatusService;
    }

    @GetMapping("/status")
    public List<FeedStatusDto> status() {
        return feedStatusService.latestStatusPerFeed();
    }

    @GetMapping("/executions")
    public Page<FeedExecutionDto> executions(@PageableDefault(size = 25) Pageable pageable) {
        return feedStatusService.executions(pageable);
    }

    @GetMapping("/executions/{feedName}")
    public List<FeedExecutionDto> executionsForFeed(@PathVariable String feedName) {
        return feedStatusService.executionsForFeed(feedName.toLowerCase());
    }
}
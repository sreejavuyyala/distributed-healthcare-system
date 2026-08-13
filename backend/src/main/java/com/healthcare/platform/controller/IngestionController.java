package com.healthcare.platform.controller;

import com.healthcare.platform.dto.SimulateFailureRequest;
import com.healthcare.platform.ingestion.IngestionResult;
import com.healthcare.platform.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Manual ingestion triggers used by the demo. In production this would be
 * called by Azure Data Factory pipeline activities / triggers rather than an
 * operator; see docs/ingestion.md.
 */
@RestController
@RequestMapping("/api")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingestion/run")
    public ResponseEntity<Map<String, String>> runAll() {
        ingestionService.runAllFeedsAsync();
        return ResponseEntity.accepted().body(Map.of(
                "status", "STARTED",
                "message", "Ingestion started for all feeds — poll GET /api/feeds/status for progress"
        ));
    }

    @PostMapping("/ingestion/run/{feedName}")
    public IngestionResult runFeed(@PathVariable String feedName) {
        return ingestionService.runFeed(feedName);
    }

    /**
     * Pipeline Failure Simulation panel: forces the named feed's next
     * {@code attemptsToFail} ingestion attempts to fail, then triggers a full
     * batch run so the dashboard can show that feed FAILED while every other
     * feed still reports SUCCESS.
     */
    @PostMapping("/feeds/simulate")
    public ResponseEntity<Map<String, String>> simulateFailure(@Valid @RequestBody SimulateFailureRequest request) {
        ingestionService.configureSimulatedFailure(request.feedName(), request.attemptsToFail());
        ingestionService.runAllFeedsAsync();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "STARTED",
                "message", "Forced failure configured for " + request.feedName()
                        + " — batch run started, poll GET /api/feeds/status for results"
        ));
    }

    @PostMapping("/feeds/simulate/{feedName}/clear")
    public ResponseEntity<Void> clearSimulatedFailure(@PathVariable String feedName) {
        ingestionService.clearSimulatedFailure(feedName);
        return ResponseEntity.noContent().build();
    }
}
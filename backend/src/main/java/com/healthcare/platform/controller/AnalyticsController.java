package com.healthcare.platform.controller;

import com.healthcare.platform.analytics.AnalyticsQueryService;
import com.healthcare.platform.dto.DiagnosisFrequencyDto;
import com.healthcare.platform.dto.EncounterTrendPointDto;
import com.healthcare.platform.dto.FacilityActivityDto;
import com.healthcare.platform.dto.LabTrendPointDto;
import com.healthcare.platform.dto.OverviewCountsDto;
import com.healthcare.platform.dto.ProviderWorkloadDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/overview")
    public OverviewCountsDto overview() {
        return analyticsQueryService.overviewCounts();
    }

    @GetMapping("/encounters")
    public List<EncounterTrendPointDto> encounterTrend(@RequestParam(defaultValue = "12") int months) {
        return analyticsQueryService.encounterTrend(months);
    }

    @GetMapping("/diagnoses")
    public List<DiagnosisFrequencyDto> diagnosisFrequency(@RequestParam(defaultValue = "15") int limit) {
        return analyticsQueryService.diagnosisFrequency(limit);
    }

    @GetMapping("/providers")
    public List<ProviderWorkloadDto> providerWorkload(@RequestParam(defaultValue = "15") int limit) {
        return analyticsQueryService.providerWorkload(limit);
    }

    @GetMapping("/facilities")
    public List<FacilityActivityDto> facilityActivity() {
        return analyticsQueryService.facilityActivity();
    }

    @GetMapping("/labs")
    public List<LabTrendPointDto> labTrend(@RequestParam(required = false) String testName,
                                            @RequestParam(defaultValue = "6") int months) {
        return analyticsQueryService.labTrend(testName, months);
    }

    @GetMapping("/length-of-stay")
    public Map<String, Object> averageLengthOfStayByDepartment() {
        return analyticsQueryService.averageLengthOfStayByDepartment();
    }
}
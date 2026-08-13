package com.healthcare.platform.controller;

import com.healthcare.platform.dto.DiagnosisDto;
import com.healthcare.platform.dto.EncounterDto;
import com.healthcare.platform.dto.LabDto;
import com.healthcare.platform.service.ClinicalDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ClinicalDataController {

    private final ClinicalDataService service;

    public ClinicalDataController(ClinicalDataService service) {
        this.service = service;
    }

    @GetMapping("/encounters")
    public Page<EncounterDto> encounters(@PageableDefault(size = 25) Pageable pageable) {
        return service.encounters(pageable);
    }

    @GetMapping("/diagnoses")
    public Page<DiagnosisDto> diagnoses(@PageableDefault(size = 25) Pageable pageable) {
        return service.diagnoses(pageable);
    }

    @GetMapping("/labs")
    public Page<LabDto> labs(@PageableDefault(size = 25) Pageable pageable) {
        return service.labs(pageable);
    }
}
package com.healthcare.platform.controller;

import com.healthcare.platform.dto.*;
import com.healthcare.platform.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Page<PatientDto> list(@PageableDefault(size = 25) Pageable pageable) {
        return patientService.list(pageable);
    }

    @GetMapping("/{patientId}")
    public PatientDto get(@PathVariable String patientId) {
        return patientService.get(patientId);
    }

    @GetMapping("/{patientId}/encounters")
    public Page<EncounterDto> encounters(@PathVariable String patientId, @PageableDefault(size = 25) Pageable pageable) {
        return patientService.encountersFor(patientId, pageable);
    }

    @GetMapping("/{patientId}/diagnoses")
    public Page<DiagnosisDto> diagnoses(@PathVariable String patientId, @PageableDefault(size = 25) Pageable pageable) {
        return patientService.diagnosesFor(patientId, pageable);
    }

    @GetMapping("/{patientId}/procedures")
    public Page<ProcedureDto> procedures(@PathVariable String patientId, @PageableDefault(size = 25) Pageable pageable) {
        return patientService.proceduresFor(patientId, pageable);
    }

    @GetMapping("/{patientId}/labs")
    public Page<LabDto> labs(@PathVariable String patientId, @PageableDefault(size = 25) Pageable pageable) {
        return patientService.labsFor(patientId, pageable);
    }
}
package com.healthcare.platform.service;

import com.healthcare.platform.dto.DiagnosisDto;
import com.healthcare.platform.dto.EncounterDto;
import com.healthcare.platform.dto.LabDto;
import com.healthcare.platform.mapper.EntityMapper;
import com.healthcare.platform.repository.DiagnosisRepository;
import com.healthcare.platform.repository.EncounterRepository;
import com.healthcare.platform.repository.LabRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Top-level (non-patient-scoped) listing endpoints: GET /api/encounters, /api/diagnoses, /api/labs. */
@Service
public class ClinicalDataService {

    private final EncounterRepository encounterRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final LabRepository labRepository;
    private final EntityMapper mapper;

    public ClinicalDataService(EncounterRepository encounterRepository, DiagnosisRepository diagnosisRepository,
                                LabRepository labRepository, EntityMapper mapper) {
        this.encounterRepository = encounterRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.labRepository = labRepository;
        this.mapper = mapper;
    }

    public Page<EncounterDto> encounters(Pageable pageable) {
        return encounterRepository.findAllByOrderByAdmissionTimeDesc(pageable).map(mapper::toDto);
    }

    public Page<DiagnosisDto> diagnoses(Pageable pageable) {
        return diagnosisRepository.findAllByOrderByDiagnosisIdAsc(pageable).map(mapper::toDto);
    }

    public Page<LabDto> labs(Pageable pageable) {
        return labRepository.findAllByOrderByCollectedAtDesc(pageable).map(mapper::toDto);
    }
}
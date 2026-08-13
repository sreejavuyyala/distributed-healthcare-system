package com.healthcare.platform.service;

import com.healthcare.platform.dto.*;
import com.healthcare.platform.entity.Patient;
import com.healthcare.platform.exception.ResourceNotFoundException;
import com.healthcare.platform.mapper.EntityMapper;
import com.healthcare.platform.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final EncounterRepository encounterRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final ProcedureRepository procedureRepository;
    private final LabRepository labRepository;
    private final EntityMapper mapper;

    public PatientService(PatientRepository patientRepository, EncounterRepository encounterRepository,
                           DiagnosisRepository diagnosisRepository, ProcedureRepository procedureRepository,
                           LabRepository labRepository, EntityMapper mapper) {
        this.patientRepository = patientRepository;
        this.encounterRepository = encounterRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.procedureRepository = procedureRepository;
        this.labRepository = labRepository;
        this.mapper = mapper;
    }

    public Page<PatientDto> list(Pageable pageable) {
        return patientRepository.findAllByOrderByPatientIdAsc(pageable).map(mapper::toDto);
    }

    public PatientDto get(String patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
        return mapper.toDto(patient);
    }

    public Page<EncounterDto> encountersFor(String patientId, Pageable pageable) {
        ensureExists(patientId);
        return encounterRepository.findByPatientIdOrderByAdmissionTimeDesc(patientId, pageable).map(mapper::toDto);
    }

    public Page<DiagnosisDto> diagnosesFor(String patientId, Pageable pageable) {
        ensureExists(patientId);
        return diagnosisRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable).map(mapper::toDto);
    }

    public Page<ProcedureDto> proceduresFor(String patientId, Pageable pageable) {
        ensureExists(patientId);
        return procedureRepository.findByPatientIdOrderByProcedureDateDesc(patientId, pageable).map(mapper::toDto);
    }

    public Page<LabDto> labsFor(String patientId, Pageable pageable) {
        ensureExists(patientId);
        return labRepository.findByPatientIdOrderByCollectedAtDesc(patientId, pageable).map(mapper::toDto);
    }

    private void ensureExists(String patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found: " + patientId);
        }
    }
}
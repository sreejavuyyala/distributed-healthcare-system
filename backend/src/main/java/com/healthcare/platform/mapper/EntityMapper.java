package com.healthcare.platform.mapper;

import com.healthcare.platform.dto.*;
import com.healthcare.platform.entity.*;
import org.springframework.stereotype.Component;

/** Plain, explicit entity <-> DTO mapping — no reflection-based mapping framework, so it's easy to trace in a debugger. */
@Component
public class EntityMapper {

    public PatientDto toDto(Patient p) {
        return new PatientDto(p.getPatientId(), p.getFirstName(), p.getLastName(), p.getDateOfBirth(), p.getGender(), p.getZipCode());
    }

    public EncounterDto toDto(Encounter e) {
        return new EncounterDto(e.getEncounterId(), e.getPatientId(), e.getProviderName(), e.getSpecialty(),
                e.getFacilityName(), e.getEncounterType(), e.getDepartment(), e.getAdmissionTime(),
                e.getDischargeTime(), e.getLengthOfStayHours(), e.getEncounterDate());
    }

    public DiagnosisDto toDto(Diagnosis d) {
        return new DiagnosisDto(d.getDiagnosisId(), d.getPatientId(), d.getEncounterId(), d.getDiagnosisCode(), d.getDiagnosisDescription());
    }

    public ProcedureDto toDto(Procedure p) {
        return new ProcedureDto(p.getProcedureId(), p.getPatientId(), p.getEncounterId(), p.getProcedureCode(),
                p.getProcedureDescription(), p.getProcedureDate());
    }

    public LabDto toDto(Lab l) {
        return new LabDto(l.getLabId(), l.getPatientId(), l.getTestName(), l.getTestResult(), l.getReferenceRange(), l.getCollectedAt());
    }

    public FeedExecutionDto toDto(FeedExecution fe) {
        return new FeedExecutionDto(fe.getExecutionId(), fe.getFeedName(), fe.getBatchId(), fe.getStartTime(), fe.getEndTime(),
                fe.getStatus(), nz(fe.getRecordsReceived()), nz(fe.getRecordsProcessed()), nz(fe.getRecordsFailed()),
                nz(fe.getRetryCount()), fe.getErrorMessage());
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
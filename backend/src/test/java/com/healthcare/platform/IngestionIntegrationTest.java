package com.healthcare.platform;

import com.healthcare.platform.audit.FeedExecutionStatus;
import com.healthcare.platform.ingestion.FailureSimulator;
import com.healthcare.platform.ingestion.FeedName;
import com.healthcare.platform.ingestion.IngestionOrchestrator;
import com.healthcare.platform.ingestion.IngestionResult;
import com.healthcare.platform.repository.DiagnosisRepository;
import com.healthcare.platform.repository.EncounterRepository;
import com.healthcare.platform.repository.LabRepository;
import com.healthcare.platform.repository.PatientRepository;
import com.healthcare.platform.repository.ProcedureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real end-to-end integration test against a local PostgreSQL database (no
 * Docker/Testcontainers — see docs/testing.md for the one-time
 * `createdb healthcare_test` setup). Ingests the fixture CSVs in
 * src/test/resources/test-feeds and verifies the data actually lands
 * correctly in the analytics schema, then proves failure isolation against a
 * real database and a real (near-instant, test-config) retry sequence.
 */
@SpringBootTest
@ActiveProfiles("test")
class IngestionIntegrationTest {

    @Autowired
    private IngestionOrchestrator orchestrator;
    @Autowired
    private FailureSimulator failureSimulator;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private EncounterRepository encounterRepository;
    @Autowired
    private DiagnosisRepository diagnosisRepository;
    @Autowired
    private ProcedureRepository procedureRepository;
    @Autowired
    private LabRepository labRepository;

    @Test
    void allFiveFeedsIngestSuccessfullyAndDataLandsInAnalyticsSchema() {
        List<IngestionResult> results = orchestrator.runAllFeeds();

        assertThat(results).hasSize(5);
        assertThat(results).allSatisfy(r -> assertThat(r.status()).isEqualTo(FeedExecutionStatus.SUCCESS));

        assertThat(patientRepository.findById("PAT-TEST-01")).isPresent();
        assertThat(patientRepository.findById("PAT-TEST-01").get().getFirstName()).isEqualTo("Jane");

        assertThat(encounterRepository
                .findByPatientIdOrderByAdmissionTimeDesc("PAT-TEST-01", PageRequest.of(0, 10))
                .getContent()).extracting("encounterId").contains("ENC-TEST-01");

        assertThat(diagnosisRepository
                .findByPatientIdOrderByCreatedAtDesc("PAT-TEST-01", PageRequest.of(0, 10))
                .getContent()).extracting("diagnosisCode").contains("I10");

        assertThat(procedureRepository
                .findByPatientIdOrderByProcedureDateDesc("PAT-TEST-01", PageRequest.of(0, 10))
                .getContent()).extracting("procedureCode").contains("93000");

        assertThat(labRepository
                .findByPatientIdOrderByCollectedAtDesc("PAT-TEST-01", PageRequest.of(0, 10))
                .getContent()).extracting("testName").contains("Hemoglobin A1c");
    }

    @Test
    void encounterFailureAgainstRealDatabaseDoesNotStopOtherFeeds() {
        failureSimulator.forceFailure(FeedName.ENCOUNTERS, 2); // exceeds test max-retries (2) -> exhausts and FAILS

        List<IngestionResult> results = orchestrator.runAllFeeds();

        IngestionResult encountersResult = results.stream()
                .filter(r -> r.feedName().equals("encounters")).findFirst().orElseThrow();
        assertThat(encountersResult.status()).isEqualTo(FeedExecutionStatus.FAILED);

        for (IngestionResult result : results) {
            if (!result.feedName().equals("encounters")) {
                assertThat(result.status())
                        .as("feed %s must succeed despite encounters failing", result.feedName())
                        .isEqualTo(FeedExecutionStatus.SUCCESS);
            }
        }

        failureSimulator.clearForcedFailure(FeedName.ENCOUNTERS);
    }
}
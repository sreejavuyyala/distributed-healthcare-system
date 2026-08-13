export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Patient {
  patientId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string | null;
  zipCode: string | null;
}

export interface Encounter {
  encounterId: string;
  patientId: string;
  providerName: string | null;
  specialty: string | null;
  facilityName: string | null;
  encounterType: string | null;
  department: string | null;
  admissionTime: string;
  dischargeTime: string | null;
  lengthOfStayHours: number | null;
  encounterDate: string;
}

export interface Diagnosis {
  diagnosisId: string;
  patientId: string;
  encounterId: string;
  diagnosisCode: string;
  diagnosisDescription: string | null;
}

export interface Procedure {
  procedureId: string;
  patientId: string;
  encounterId: string;
  procedureCode: string;
  procedureDescription: string | null;
  procedureDate: string;
}

export interface Lab {
  labId: string;
  patientId: string;
  testName: string;
  testResult: string | null;
  referenceRange: string | null;
  collectedAt: string;
}

export type FeedStatusValue = 'RUNNING' | 'SUCCESS' | 'FAILED';

export interface FeedStatus {
  feedName: string;
  status: FeedStatusValue;
  lastRun: string;
  recordsProcessed: number;
  recordsFailed: number;
  retryCount: number;
  errorMessage: string | null;
}

export interface FeedExecution {
  executionId: number;
  feedName: string;
  batchId: string;
  startTime: string;
  endTime: string | null;
  status: FeedStatusValue;
  recordsReceived: number;
  recordsProcessed: number;
  recordsFailed: number;
  retryCount: number;
  errorMessage: string | null;
}

export interface Metrics {
  recordsProcessed: number;
  successfulFeeds: number;
  failedFeeds: number;
  averageProcessingTimeMs: number;
  averageQueryLatencyMs: number;
}

export interface OverviewCounts {
  totalPatients: number;
  totalEncounters: number;
  totalDiagnoses: number;
  totalProcedures: number;
  totalLabs: number;
}

export interface EncounterTrendPoint {
  period: string;
  encounterCount: number;
}

export interface DiagnosisFrequency {
  diagnosisCode: string;
  diagnosisDescription: string | null;
  count: number;
}

export interface ProviderWorkload {
  providerName: string;
  specialty: string | null;
  encounterCount: number;
}

export interface FacilityActivity {
  facilityName: string;
  encounterCount: number;
}
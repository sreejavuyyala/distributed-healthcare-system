import type {
  Diagnosis,
  Encounter,
  EncounterTrendPoint,
  DiagnosisFrequency,
  FacilityActivity,
  FeedExecution,
  FeedStatus,
  Lab,
  Metrics,
  OverviewCounts,
  Page,
  Patient,
  Procedure,
  ProviderWorkload,
} from '../types';

const BASE = '/api';

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`);
  if (!res.ok) {
    const body = await res.text();
    throw new Error(`GET ${path} failed: ${res.status} ${body}`);
  }
  return res.json() as Promise<T>;
}

async function post<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`POST ${path} failed: ${res.status} ${text}`);
  }
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export const api = {
  health: () => get<{ status: string; notice: string }>('/health'),
  metrics: () => get<Metrics>('/metrics'),
  overview: () => get<OverviewCounts>('/analytics/overview'),

  patients: (page = 0, size = 25) => get<Page<Patient>>(`/patients?page=${page}&size=${size}`),
  patient: (id: string) => get<Patient>(`/patients/${id}`),
  patientEncounters: (id: string) => get<Page<Encounter>>(`/patients/${id}/encounters?size=50`),
  patientDiagnoses: (id: string) => get<Page<Diagnosis>>(`/patients/${id}/diagnoses?size=50`),
  patientProcedures: (id: string) => get<Page<Procedure>>(`/patients/${id}/procedures?size=50`),
  patientLabs: (id: string) => get<Page<Lab>>(`/patients/${id}/labs?size=50`),

  encounterTrend: (months = 12) => get<EncounterTrendPoint[]>(`/analytics/encounters?months=${months}`),
  diagnosisFrequency: (limit = 10) => get<DiagnosisFrequency[]>(`/analytics/diagnoses?limit=${limit}`),
  providerWorkload: (limit = 10) => get<ProviderWorkload[]>(`/analytics/providers?limit=${limit}`),
  facilityActivity: () => get<FacilityActivity[]>('/analytics/facilities'),

  feedStatus: () => get<FeedStatus[]>('/feeds/status'),
  feedExecutions: (page = 0, size = 25) => get<Page<FeedExecution>>(`/feeds/executions?page=${page}&size=${size}`),

  runAllFeeds: () => post<{ status: string; message: string }>('/ingestion/run'),
  runFeed: (feedName: string) => post<FeedExecution>(`/ingestion/run/${feedName}`),
  simulateFailure: (feedName: string, attemptsToFail: number) =>
    post<{ status: string; message: string }>('/feeds/simulate', { feedName, attemptsToFail }),
  clearSimulatedFailure: (feedName: string) => post<void>(`/feeds/simulate/${feedName}/clear`),
};
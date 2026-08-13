# API Reference

Base URL (local): `http://localhost:8080/api`

All list endpoints are paginated (Spring Data `Page<T>` — `content`,
`totalElements`, `totalPages`, `number`, `size`) and accept `?page=` /
`?size=` query params. All responses are DTOs, never raw JPA entities.
Every response carries an `X-Correlation-Id` header for tracing (see
`CorrelationIdFilter`); errors follow a consistent shape (see below).

## Health & Metrics

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Liveness + a reminder this is a synthetic-data demo |
| GET | `/metrics` | Real computed metrics: records processed, successful/failed feed counts, avg. processing time, live-probed avg. query latency |

## Patients

| Method | Path | Description |
|---|---|---|
| GET | `/patients` | Paginated patient list |
| GET | `/patients/{patientId}` | Single patient |
| GET | `/patients/{patientId}/encounters` | That patient's encounters, most recent first |
| GET | `/patients/{patientId}/diagnoses` | That patient's diagnoses |
| GET | `/patients/{patientId}/procedures` | That patient's procedures |
| GET | `/patients/{patientId}/labs` | That patient's lab results |

## Clinical data (non-patient-scoped)

| Method | Path | Description |
|---|---|---|
| GET | `/encounters` | All encounters, paginated |
| GET | `/diagnoses` | All diagnoses, paginated |
| GET | `/labs` | All labs, paginated |

## Analytics

| Method | Path | Description |
|---|---|---|
| GET | `/analytics/overview` | Total counts (patients/encounters/diagnoses/procedures/labs) |
| GET | `/analytics/encounters?months=12` | Encounter volume trend, monthly |
| GET | `/analytics/diagnoses?limit=10` | Diagnosis frequency, most common first |
| GET | `/analytics/providers?limit=10` | Provider workload (encounter count, grouped by provider_name) |
| GET | `/analytics/facilities` | Encounters by facility |
| GET | `/analytics/labs?testName=&months=6` | Lab test volume trend, optionally filtered by test |
| GET | `/analytics/length-of-stay` | Average length of stay by department |

## Pipeline / Ingestion

| Method | Path | Description |
|---|---|---|
| GET | `/feeds/status` | Latest execution status per feed (Pipeline Health panel) |
| GET | `/feeds/executions` | Full `feed_execution` audit trail, paginated |
| GET | `/feeds/executions/{feedName}` | Execution history for one feed |
| POST | `/ingestion/run` | Trigger all 5 feeds asynchronously (202 Accepted; poll `/feeds/status`) |
| POST | `/ingestion/run/{feedName}` | Trigger one feed synchronously, returns the result |
| POST | `/feeds/simulate` | Body: `{"feedName": "ENCOUNTERS", "attemptsToFail": 3}` — forces a feed to fail, then runs all feeds |
| POST | `/feeds/simulate/{feedName}/clear` | Clears a forced-failure configuration |

## Error shape

```json
{
  "timestamp": "2026-08-13T00:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found: PAT-9999999",
  "path": "/api/patients/PAT-9999999",
  "correlationId": "b3f1...",
  "details": null
}
```

Validation errors (`400`) populate `details` with one message per
invalid field. Database errors are caught centrally and never leak a
raw stack trace or SQL to the client (`GlobalExceptionHandler`).
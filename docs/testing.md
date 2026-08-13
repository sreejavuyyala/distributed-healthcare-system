# Testing

No Docker, no Testcontainers — consistent with this project's $0-cost,
no-Docker constraint, tests run against a **local PostgreSQL install**,
the same one used for `mvn spring-boot:run`.

## One-time setup

```bash
createdb healthcare_test
```

(Uses the same `healthcare` role created for the main `healthcare`
database — see the README setup section.)

## Running the tests

```bash
cd backend
mvn test
```

21 tests, all passing locally: 4 test classes of pure unit tests (no
database) + 1 integration test class (real database).

## Test breakdown

| Class | Type | What it proves |
|---|---|---|
| `FeedIsolationTest` | Unit (Mockito) | **The core requirement**: an Encounters failure does not stop Patients/Diagnoses/Procedures/Labs — both for a normal `FAILED` result and for an unexpected exception |
| `RetryExecutorTest` | Unit | Succeeds without retrying when the first attempt works; retries and eventually succeeds; gives up after exactly `maxAttempts` and reports the last error |
| `FailureSimulatorTest` | Unit | Random failure rate boundaries (0.0 never fails, 1.0 always fails); forced failure consumes exactly N attempts then stops; one feed's forced failure doesn't affect another |
| `FeedValidatorTest` | Unit | Missing required field, invalid date, duplicate natural key within a batch, and that optional blank fields are allowed |
| `IdempotencyServiceTest` | Unit | Same content → same hash → same batch ID, deterministically; different feeds with the same content hash get different batch IDs |
| `IngestionIntegrationTest` | Integration (`@SpringBootTest`, real Postgres) | All 5 fixture feeds ingest and land correctly in the `analytics` schema; **the failure-isolation property proven again, this time against a real database with a real (short) retry sequence** |

## Why the retry tests run instantly

`RetryExecutor` takes a `Sleeper` interface (`Thread.sleep` in
production). Tests inject a no-op `Sleeper`, so a test asserting a
3-attempt retry sequence with 2s/4s backoff finishes in milliseconds
instead of 6 seconds.

## Fixture data

`backend/src/test/resources/test-feeds/*.csv` — small (1–2 rows),
referentially consistent versions of the 5 feeds, used only by
`IngestionIntegrationTest`. `application-test.yml` points
`platform.ingestion.feeds-dir` at this directory and the datasource at
`healthcare_test`.

## Data-quality / validation testing

`FeedValidatorTest` covers Phase-equivalent requirements around schema
validation: required fields, null handling, invalid dates, and duplicate
identifiers within a batch. A row failing validation is counted and
excluded, not treated as a whole-batch failure — see
[`docs/data-pipeline.md`](data-pipeline.md#4-validation).

## CI

`.github/workflows/ci.yml` runs the same `mvn verify` in GitHub Actions,
using a Postgres **service container** (not a project Dockerfile — this
is CI plumbing, unrelated to the app's own no-Docker local-run story) so
the integration test has a real database to run against.
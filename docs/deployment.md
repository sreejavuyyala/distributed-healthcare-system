# Deployment

## Local (the only deployment this project actually does)

Everything runs on one machine at **$0**: Java 21, PostgreSQL, Python 3,
and Node.js, all local installs — no Docker, no cloud subscription. See
the README "Run the demo" section for exact commands.

Configuration profiles (`backend/src/main/resources/`):

- `application.yml` — shared defaults (env-var driven, no secrets committed)
- `application-local.yml` — local Postgres + local-filesystem raw storage (default)
- `application-azure.yml` — Azure profile (see below), **not exercised by
  this repo's CI, tests, or demo**

## Azure (design target — not deployed)

`application-azure.yml` and `AzureBlobStorageGateway` (real, compiling
code) exist to document what deploying this for real would involve. This
project has **not** provisioned any of the following, and none of it is
required to run or evaluate the project:

1. **Azure Database for PostgreSQL** — swap `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`
   to point at it; the same Flyway migrations apply unchanged.
2. **Azure Storage Account** — a Blob container for the raw landing zone;
   supply `AZURE_STORAGE_CONNECTION_STRING` (never hardcoded — see
   `.env.example`) and set `platform.storage.provider=azure`.
3. **Azure Data Factory** — the 5 pipelines described in
   [`azure-adf/`](../azure-adf) (2 shown in full as representative
   examples), each independently triggered, replacing the local
   `IngestionOrchestrator`'s role of *invoking* ingestion (the retry +
   isolation logic itself would move into ADF's native retry policy /
   per-pipeline execution model, mirrored by the JSON shown).
4. **App hosting** — Azure App Service (or a container) for the Spring
   Boot JAR; static hosting (Azure Static Web Apps, or this project's
   existing GitHub Pages setup) for the built React app.

Running with the `azure` profile: `SPRING_PROFILES_ACTIVE=azure` plus the
environment variables above. This has been written to be structurally
correct against the Azure SDK (it compiles and is part of the backend's
test-covered codebase in the sense that it's real production code, not a
stub) but has **not been executed against a live Azure account** as part
of this project — see [`docs/architecture.md`](architecture.md#whats-design-only-vs-what-actually-running)
for the explicit locally-executable vs. design-only split.

## Frontend

`frontend/` builds to static files (`npm run build` → `dist/`) that can
be served by any static host. Locally, `npm run dev` proxies `/api` to
the backend at `localhost:8080` (see `vite.config.ts`).

## CI/CD

`.github/workflows/ci.yml` builds and tests the backend (Maven, with a
Postgres service container for the one integration test), type-checks
and builds the frontend, and smoke-tests the data generator — all on
every push/PR. `.github/workflows/pages.yml` deploys
[`github-pages/`](../github-pages) to GitHub Pages on changes to that
directory. Neither workflow provisions or depends on Docker or Azure.
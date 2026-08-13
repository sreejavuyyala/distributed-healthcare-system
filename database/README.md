# database/

- **`migrations/`** — mirror of the canonical Flyway migrations that live at
  [`backend/src/main/resources/db/migration/`](../backend/src/main/resources/db/migration).
  Spring Boot applies the copy under `backend/` at startup; this copy exists so
  the schema is easy to browse without digging into the Java module. Keep them
  in sync (a copy step will be added to CI if this drifts).
- **`schema/`** — consolidated, human-readable snapshot of the full schema
  (`schema.sql`), generated from a running instance with `pg_dump --schema-only`.
- **`queries/`** — standalone analytics and benchmark queries runnable directly
  with `psql`, independent of the application. Includes the baseline-vs-optimized
  benchmark queries referenced in [`docs/performance.md`](../docs/performance.md).
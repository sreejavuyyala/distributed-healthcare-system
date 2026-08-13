# Academic Report Outline

A suggested structure for a written report accompanying this project
(e.g. for a course submission). Each section references where the
supporting material already lives in the repository — this is an outline
to write *from*, not a filled-in report, and no academic references are
fabricated below (add real citations for any tools/standards you draw on,
e.g. the ICD-10-CM code system, the PostgreSQL documentation, Spring
Boot documentation).

1. **Abstract** — one paragraph: the problem (independent healthcare
   feeds, one shouldn't take down the others), the approach (Spring Boot
   ingestion engine with per-feed retry/isolation, partitioned/indexed
   PostgreSQL analytics, React dashboard), and the headline measured
   result (84.7% average query improvement — [`docs/performance.md`](performance.md)).

2. **Introduction** — motivate multi-source data ingestion in healthcare
   IT generally; state that this is a synthetic-data academic
   implementation, not a clinical system.

3. **Problem Statement** — see [`docs/requirements.md`](requirements.md)
   "Scope"; the core failure mode being addressed (a single bad feed
   file cascading into a full pipeline outage).

4. **Objectives** — map directly to [`docs/requirements.md`](requirements.md)'s
   functional/non-functional requirement tables.

5. **Existing Approaches** — discuss, in your own words, alternative
   architectures (a single monolithic ETL job with no isolation; a
   full microservices-per-feed deployment; message-queue-based
   ingestion) and why this project's middle-ground design (one process,
   independently-isolated feed pipelines) was chosen — see
   [`docs/architecture.md`](architecture.md#why-a-single-spring-boot-application-not-microservices).

6. **Proposed System** — summarize [`docs/architecture.md`](architecture.md)
   and [`docs/data-pipeline.md`](data-pipeline.md).

7. **Architecture** — reproduce/describe the diagram in
   [`docs/architecture.md`](architecture.md); explain the
   locally-executable-vs-design-only split.

8. **Data Pipeline** — walk through [`docs/data-pipeline.md`](data-pipeline.md)
   stage by stage: source → ingestion/retry → raw storage → validation →
   idempotency → transform → API → dashboard.

9. **Fault Tolerance** — [`docs/fault-tolerance.md`](fault-tolerance.md):
   the isolation mechanism, the retry sequence, and the 3 independent
   proofs (unit test, integration test, live demo capture).

10. **Database Design** — [`docs/database-design.md`](database-design.md):
    schema layout, the partitioning decision (why `encounters` only),
    the indexing decisions (each tied to a real query), idempotency.

11. **Query Optimization** — [`docs/performance.md`](performance.md):
    methodology, results table, and the honest discussion of why the
    measured 84.7% differs from the brief's ~30% target.

12. **Implementation** — summarize the technology stack (Java 21/Spring
    Boot 3/PostgreSQL 16/React+TypeScript/Python) and package layout
    ([`docs/architecture.md`](architecture.md#package-layout-backend)).

13. **Testing** — [`docs/testing.md`](testing.md): the 21 tests, what
    each proves, and why no Docker/Testcontainers is used.

14. **Performance Evaluation** — expand on section 11 with the raw
    samples from [`reports/performance/results.json`](../reports/performance/results.json)
    if a more detailed statistical discussion is wanted (median vs. mean,
    variance across the 7 runs).

15. **Results** — the measured, non-fabricated numbers: 160,000 records
    ingested across 5 feeds, 5/5 feeds correctly isolated under a forced
    failure, 21/21 tests passing, 84.7% average query improvement.
    (Whatever numbers you personally re-measure when you run this should
    replace/supplement these — see [`docs/performance.md`](performance.md)
    "Reproduce it yourself".)

16. **Limitations** — be honest; see the README "Limitations" section:
    synthetic-data distributions aren't epidemiologically realistic;
    provider/specialty aren't a stable joined entity (see the note in
    the README); the Azure integration is design-only and unexercised
    against a real Azure account; no authentication/authorization layer
    (out of scope — see security notes in the README).

17. **Future Work** — natural extensions: a real authentication layer;
    exercising the Azure profile against a live subscription; a 6th/7th
    feed (e.g. medications, claims) to further demonstrate the
    per-feed-isolation pattern scaling; a larger synthetic dataset to
    see whether the ~30% vs ~85% gap discussed in
    [`docs/performance.md`](performance.md) narrows at scale.

18. **Conclusion** — restate that the core academic objective (per-feed
    failure isolation with retry, proven by automated test and live
    demo, on top of a partitioned/indexed PostgreSQL analytics layer) was
    met, at $0 infrastructure cost.

19. **References** — cite what you actually used: e.g. the PostgreSQL 16
    documentation (partitioning, `EXPLAIN`), the Spring Boot / Spring
    Data JPA reference documentation, the ICD-10-CM code system (public
    domain, US CMS), and any course materials or papers on distributed
    systems fault-tolerance patterns (retry/backoff, bulkhead isolation)
    that informed the design. Do not include references that weren't
    actually consulted.
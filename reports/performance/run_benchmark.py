#!/usr/bin/env python3
"""
Runs the before/after query benchmark against a real local PostgreSQL
database (the `healthcare` database populated by the data generator + backend
ingestion) and records ACTUAL measured EXPLAIN ANALYZE execution times.
No numbers here are invented — see docs/performance.md for how these are used.

Usage:
    python3 run_benchmark.py [--db healthcare] [--runs 5]

Requires: psql on PATH, pointed at a running local Postgres with the
`healthcare` database already ingested (see backend README / docs/testing.md).
"""
import argparse
import json
import statistics
import subprocess
import sys
from datetime import datetime, timezone

SETUP_SQL = "../../database/queries/benchmark_setup.sql"

QUERIES = [
    {
        "id": "Q1_patient_encounter_history",
        "description": "Patient encounter history (single patient, ordered, limited)",
        "baseline": """
            SELECT * FROM benchmark.encounters_baseline
            WHERE patient_id = 'PAT-0005000'
            ORDER BY encounter_date DESC
            LIMIT 25;
        """,
        "optimized": """
            SELECT * FROM analytics.encounters
            WHERE patient_id = 'PAT-0005000'
            ORDER BY encounter_date DESC
            LIMIT 25;
        """,
    },
    {
        "id": "Q2_encounters_by_facility_date_range",
        "description": "Encounters by facility within a date range (dashboard chart)",
        "baseline": """
            SELECT facility_name, COUNT(*) AS cnt
            FROM benchmark.encounters_baseline
            WHERE encounter_date BETWEEN '2026-01-01' AND '2026-12-31'
            GROUP BY facility_name
            ORDER BY cnt DESC;
        """,
        "optimized": """
            SELECT facility_name, COUNT(*) AS cnt
            FROM analytics.encounters
            WHERE encounter_date BETWEEN '2026-01-01' AND '2026-12-31'
            GROUP BY facility_name
            ORDER BY cnt DESC;
        """,
    },
    {
        "id": "Q3_diagnoses_by_patient",
        "description": "Diagnosis lookup by patient (Patient Explorer panel)",
        "baseline": """
            SELECT * FROM benchmark.diagnoses_baseline
            WHERE patient_id = 'PAT-0005000';
        """,
        "optimized": """
            SELECT * FROM analytics.diagnoses
            WHERE patient_id = 'PAT-0005000';
        """,
    },
]


def run_psql(db, sql):
    result = subprocess.run(
        ["psql", "-d", db, "-t", "-A", "-c", sql],
        capture_output=True, text=True, check=True,
    )
    return result.stdout.strip()


def explain_analyze_ms(db, query, warm_runs, measured_runs):
    # Warm the cache first (fair comparison — we're measuring query-plan
    # efficiency, not cold-cache disk I/O).
    for _ in range(warm_runs):
        run_psql(db, f"EXPLAIN (ANALYZE, FORMAT JSON) {query}")

    timings = []
    for _ in range(measured_runs):
        raw = run_psql(db, f"EXPLAIN (ANALYZE, FORMAT JSON) {query}")
        plan = json.loads(raw)
        timings.append(plan[0]["Execution Time"])
    return timings


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", default="healthcare")
    parser.add_argument("--runs", type=int, default=5)
    parser.add_argument("--warm-runs", type=int, default=2)
    args = parser.parse_args()

    print(f"Setting up benchmark baseline tables in database '{args.db}'...")
    subprocess.run(["psql", "-d", args.db, "-f", SETUP_SQL], check=True, capture_output=True, text=True)

    results = []
    for q in QUERIES:
        print(f"\n{q['id']}: {q['description']}")

        baseline_ms = explain_analyze_ms(args.db, q["baseline"], args.warm_runs, args.runs)
        optimized_ms = explain_analyze_ms(args.db, q["optimized"], args.warm_runs, args.runs)

        baseline_median = statistics.median(baseline_ms)
        optimized_median = statistics.median(optimized_ms)
        improvement_pct = (baseline_median - optimized_median) / baseline_median * 100

        print(f"  baseline (median of {args.runs}):  {baseline_median:.3f} ms  {baseline_ms}")
        print(f"  optimized (median of {args.runs}): {optimized_median:.3f} ms  {optimized_ms}")
        print(f"  improvement: {improvement_pct:.1f}%")

        results.append({
            "id": q["id"],
            "description": q["description"],
            "baseline_ms_samples": baseline_ms,
            "optimized_ms_samples": optimized_ms,
            "baseline_median_ms": round(baseline_median, 3),
            "optimized_median_ms": round(optimized_median, 3),
            "improvement_pct": round(improvement_pct, 1),
        })

    overall_improvement = statistics.mean(r["improvement_pct"] for r in results)

    output = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "database": args.db,
        "runs_per_query": args.runs,
        "queries": results,
        "overall_average_improvement_pct": round(overall_improvement, 1),
    }

    with open("results.json", "w") as f:
        json.dump(output, f, indent=2)

    print(f"\nOverall average improvement across {len(results)} queries: {overall_improvement:.1f}%")
    print("Results written to reports/performance/results.json")


if __name__ == "__main__":
    main()
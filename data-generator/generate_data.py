#!/usr/bin/env python3
"""
Synthetic healthcare data generator (academic-scope: 5 feeds).

Generates entirely fictitious, deterministic (seeded) healthcare records for
the Distributed Healthcare Data Platform. NO REAL PATIENT DATA IS USED OR
REPRESENTED ANYWHERE IN THIS PROJECT.

Feeds: patients, encounters, diagnoses, procedures, labs. Provider/facility
are carried as plain descriptive columns on encounters, not separate feeds —
this keeps the ingestion model to 5 independent pipelines as required.

Usage:
    python generate_data.py --patients 10000 --encounters 30000 \\
        --diagnoses 50000 --procedures 20000 --labs 50000 --seed 42

Output: CSV files written to --output-dir (default: ../data/generated/).
"""
import argparse
import csv
import os
import random
import sys
from datetime import datetime, timedelta

from faker import Faker

sys.path.insert(0, os.path.dirname(__file__))
from src import reference_data as ref  # noqa: E402

DATA_START = datetime(2023, 1, 1)
DATA_END = datetime(2026, 8, 12)  # generation run date; see README for how to bump this


def parse_args():
    p = argparse.ArgumentParser(description="Generate synthetic healthcare data (5-feed academic scope).")
    p.add_argument("--patients", type=int, default=10_000)
    p.add_argument("--encounters", type=int, default=30_000)
    p.add_argument("--diagnoses", type=int, default=50_000)
    p.add_argument("--procedures", type=int, default=20_000)
    p.add_argument("--labs", type=int, default=50_000)
    p.add_argument("--seed", type=int, default=42)
    p.add_argument(
        "--output-dir",
        default=os.path.join(os.path.dirname(__file__), "..", "data", "generated"),
    )
    return p.parse_args()


def rand_datetime(rng, start=DATA_START, end=DATA_END):
    delta = end - start
    seconds = rng.randint(0, int(delta.total_seconds()))
    return start + timedelta(seconds=seconds)


def write_csv(path, header, rows):
    with open(path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows(rows)
    print(f"  wrote {len(rows):>8,} rows -> {path}")


def gen_patients(fake, rng, n):
    rows = []
    ids = []
    for i in range(1, n + 1):
        pid = f"PAT-{i:07d}"
        ids.append(pid)
        first = fake.first_name()
        last = fake.last_name()
        dob = fake.date_of_birth(minimum_age=0, maximum_age=95)
        gender = rng.choice(ref.GENDERS)
        zip_code = fake.zipcode()
        rows.append([pid, first, last, dob.isoformat(), gender, zip_code])
    return ids, rows


def gen_encounters(fake, rng, n, patient_ids):
    rows = []
    ids = []
    encounter_patient = {}
    for i in range(1, n + 1):
        eid = f"ENC-{i:07d}"
        ids.append(eid)
        patient_id = rng.choice(patient_ids)
        provider_name = f"Dr. {fake.last_name()}"
        specialty = rng.choice(ref.SPECIALTIES)
        facility_name = rng.choice(ref.FACILITY_NAMES)
        encounter_type = rng.choice(ref.ENCOUNTER_TYPES)
        department = rng.choice(ref.DEPARTMENTS)
        admission = rand_datetime(rng)
        stay_hours = {
            "Inpatient": rng.randint(12, 240),
            "Emergency": rng.randint(1, 24),
            "Outpatient": rng.randint(1, 4),
            "Telehealth": rng.randint(0, 1),
            "Urgent Care": rng.randint(1, 6),
        }[encounter_type]
        discharge = admission + timedelta(hours=stay_hours)
        encounter_patient[eid] = (patient_id, admission, discharge)
        rows.append([
            eid, patient_id, provider_name, specialty, facility_name, encounter_type, department,
            admission.isoformat(), discharge.isoformat(), admission.date().isoformat(),
        ])
    return ids, rows, encounter_patient


def gen_diagnoses(rng, n, encounter_ids, encounter_patient):
    rows = []
    for i in range(1, n + 1):
        did = f"DX-{i:07d}"
        encounter_id = rng.choice(encounter_ids)
        patient_id = encounter_patient[encounter_id][0]
        code, desc = rng.choice(ref.DIAGNOSIS_CODES)
        rows.append([did, patient_id, encounter_id, code, desc])
    return rows


def gen_procedures(rng, n, encounter_ids, encounter_patient):
    rows = []
    for i in range(1, n + 1):
        prid = f"PRC-{i:07d}"
        encounter_id = rng.choice(encounter_ids)
        patient_id, admission, _ = encounter_patient[encounter_id]
        code, desc = rng.choice(ref.PROCEDURE_CODES)
        proc_date = admission.date()
        rows.append([prid, patient_id, encounter_id, code, desc, proc_date.isoformat()])
    return rows


def gen_labs(rng, n, patient_ids):
    rows = []
    for i in range(1, n + 1):
        lid = f"LAB-{i:07d}"
        patient_id = rng.choice(patient_ids)
        name, unit, ref_range, lo, hi = rng.choice(ref.LAB_TESTS)
        value = round(rng.uniform(lo, hi), 1)
        result = f"{value} {unit}"
        collected = rand_datetime(rng)
        rows.append([lid, patient_id, name, result, ref_range, collected.isoformat()])
    return rows


def main():
    args = parse_args()
    os.makedirs(args.output_dir, exist_ok=True)

    random.seed(args.seed)
    rng = random.Random(args.seed)
    fake = Faker()
    Faker.seed(args.seed)

    print(f"Generating synthetic healthcare data (seed={args.seed}) -> {args.output_dir}")
    print("NOTE: all data below is synthetic / fictitious. No real PHI is used.\n")

    print("patients:")
    patient_ids, patient_rows = gen_patients(fake, rng, args.patients)
    write_csv(os.path.join(args.output_dir, "patients.csv"),
              ["patient_id", "first_name", "last_name", "date_of_birth", "gender", "zip_code"],
              patient_rows)

    print("encounters:")
    encounter_ids, encounter_rows, encounter_patient = gen_encounters(
        fake, rng, args.encounters, patient_ids)
    write_csv(os.path.join(args.output_dir, "encounters.csv"),
              ["encounter_id", "patient_id", "provider_name", "specialty", "facility_name",
               "encounter_type", "department", "admission_time", "discharge_time", "encounter_date"],
              encounter_rows)

    print("diagnoses:")
    diagnosis_rows = gen_diagnoses(rng, args.diagnoses, encounter_ids, encounter_patient)
    write_csv(os.path.join(args.output_dir, "diagnoses.csv"),
              ["diagnosis_id", "patient_id", "encounter_id", "diagnosis_code", "diagnosis_description"],
              diagnosis_rows)

    print("procedures:")
    procedure_rows = gen_procedures(rng, args.procedures, encounter_ids, encounter_patient)
    write_csv(os.path.join(args.output_dir, "procedures.csv"),
              ["procedure_id", "patient_id", "encounter_id", "procedure_code",
               "procedure_description", "procedure_date"], procedure_rows)

    print("labs:")
    lab_rows = gen_labs(rng, args.labs, patient_ids)
    write_csv(os.path.join(args.output_dir, "labs.csv"),
              ["lab_id", "patient_id", "test_name", "test_result", "reference_range", "collected_at"],
              lab_rows)

    total = args.patients + args.encounters + args.diagnoses + args.procedures + args.labs
    print(f"\nDone. {total:,} total synthetic records written to {args.output_dir}")


if __name__ == "__main__":
    main()
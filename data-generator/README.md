# Synthetic Healthcare Data Generator

Generates fully synthetic patient, encounter, diagnosis, procedure, and
lab records for the platform demo. **No real patient data (PHI) is used
or represented anywhere.** Diagnosis and procedure codes are drawn from
the public ICD-10-CM / CPT code systems (code + description only, used
as realistic category values). Provider name/specialty and facility name
are carried as plain descriptive columns on the encounters feed rather
than separate feeds — see [`../docs/database-design.md`](../docs/database-design.md).

## Setup

```bash
cd data-generator
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Usage

```bash
python generate_data.py \
  --patients 10000 --encounters 30000 --diagnoses 50000 \
  --procedures 20000 --labs 50000 --seed 42
```

All counts and the seed are configurable flags; the seed makes generation
**deterministic and reproducible** — the same seed always produces the same
data, byte for byte.

Output: 5 CSV files (`patients.csv`, `encounters.csv`, `diagnoses.csv`,
`procedures.csv`, `labs.csv`) written to `../data/generated/` by default
(`--output-dir` to override). Referential integrity is enforced at
generation time — every `encounter.patient_id`, `diagnosis.encounter_id`,
etc. points at a row that actually exists in the corresponding file.

A small pre-generated sample (a few hundred rows per entity) is committed at
[`../data/sample/`](../data/sample) so the repo is browsable without running
the generator.

## Benchmark

Generating the default volumes (160,000 rows total) takes ~3.4s on
commodity hardware (measured on this machine, Apple Silicon, Python 3.14).
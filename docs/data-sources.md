# Dataset Strategy

## Decision: synthetic data, generated locally

This project uses a **custom synthetic data generator**
([`data-generator/`](../data-generator)) rather than redistributing a public
dataset, for two reasons documented here rather than left implicit:

1. **No public dataset lines up with the required schema.** This project
   needs referentially-linked patients → encounters → diagnoses/
   procedures/labs, generated at controllable volumes, with a
   deterministic seed. Public healthcare datasets (below) are each shaped
   for a specific purpose and don't compose into this exact shape without
   heavy reshaping.
2. **Redistribution safety.** Several strong public sources (e.g. CMS
   Synthetic Public Use Files, PhysioNet's MIMIC-derived sets) carry
   data-use agreements or credentialing requirements that make them
   unsuitable to bundle in a public GitHub repo, even when de-identified.

Generating synthetic data locally guarantees the repository stays 100% safe
to publish, with **zero real PHI risk**, while producing data that is
structurally realistic (real ICD-10-CM/CPT code values, real specialty and
department names, plausible lab reference ranges).

## Public sources reviewed (for reference / structural inspiration)

| Source | URL | License | Notes | Used here? |
|---|---|---|---|---|
| Synthea (MITRE) | https://synthetichealth.github.io/synthea/ | Apache 2.0 | Purpose-built synthetic patient generator, produces FHIR/CSV; the gold-standard alternative to a custom generator | Not used directly — informed the shape of our own generator (patient → encounter → clinical-event graph), reimplemented from scratch to hit this project's exact schema/volume requirements |
| CMS Synthetic Public Use Files (SynPUFs) | https://www.cms.gov/data-research/statistics-trends-reports/medicare-claims-synthetic-public-use-files | CMS Data Use Agreement | Real Medicare claims structure, statistically representative, synthetic beneficiaries | Not redistributed — DUA terms make bundling in a public repo inappropriate |
| CDC public health datasets | https://data.cdc.gov | Public domain (US Gov) | Population-level, not patient/encounter-level; wrong grain for this schema | Not used |
| UCI ML Repository (healthcare sets, e.g. Diabetes 130-US hospitals) | https://archive.ics.uci.edu/ | Varies by dataset (mostly CC BY 4.0) | Real-world-derived tabular sets, but fixed schema/columns don't match this project's entity model | Not used — schema mismatch |
| PhysioNet (MIMIC-III/IV) | https://physionet.org/ | PhysioNet Credentialed Health Data License | Real de-identified ICU data; requires credentialing + training even for de-identified data | Not used — access requirements make it unsuitable for a public portfolio repo |
| ICD-10-CM code system | https://www.cms.gov/medicare/coding-billing/icd-10-codes | Public domain (US Gov) | Diagnosis code + description pairs | Used as reference values in `data-generator/src/reference_data.py` (codes/descriptions only, no patient data) |
| CPT-like procedure descriptions | Public reference material | N/A | Procedure code + description pairs | Used as reference values (illustrative, not licensed AMA CPT codeset) |

## Limitations of the synthetic approach

- Value distributions (lab results, diagnosis frequency) are drawn from
  simple uniform/random distributions, not the true epidemiological
  distributions a tool like Synthea models — this is a demo dataset for
  system/pipeline behavior, not a clinical research dataset.
- Names, addresses, and zip codes are `Faker`-generated and not
  geographically/demographically correlated with the synthetic patient's
  other attributes.
"""
Reference value sets used to generate realistic-looking (but entirely
synthetic) clinical records. Diagnosis/procedure codes below are drawn from
the public ICD-10-CM and CPT code systems (code + short description only --
no patient data), used here purely as realistic category values, not as a
redistribution of any licensed dataset.
"""

GENDERS = ["Male", "Female", "Other", "Unknown"]

ENCOUNTER_TYPES = ["Inpatient", "Outpatient", "Emergency", "Telehealth", "Urgent Care"]

DEPARTMENTS = [
    "Cardiology", "Emergency Medicine", "General Surgery", "Internal Medicine",
    "Obstetrics & Gynecology", "Oncology", "Orthopedics", "Pediatrics",
    "Psychiatry", "Radiology", "Neurology", "Pulmonology", "Endocrinology",
    "Gastroenterology", "Dermatology", "Nephrology",
]

SPECIALTIES = [
    "Cardiology", "Family Medicine", "Internal Medicine", "General Surgery",
    "Obstetrics & Gynecology", "Oncology", "Orthopedic Surgery", "Pediatrics",
    "Psychiatry", "Radiology", "Neurology", "Pulmonology", "Endocrinology",
    "Gastroenterology", "Dermatology", "Nephrology", "Emergency Medicine",
]

# Small fixed set of fictional facility names -- encounters reference these as
# plain descriptive text (no separate facility feed/table in the academic scope).
FACILITY_NAMES = [
    "Riverside Medical Center", "Lakeview General Hospital", "Northside Community Clinic",
    "Cedar Grove Regional Hospital", "Sunrise Specialty Center", "Maple Street Outpatient Clinic",
    "Harborview Medical Center", "Pinecrest General Hospital", "Willow Creek Clinic",
    "Eastgate Regional Medical Center",
]

# (ICD-10-CM code, short description) — public code system reference values.
DIAGNOSIS_CODES = [
    ("E11.9", "Type 2 diabetes mellitus without complications"),
    ("I10", "Essential (primary) hypertension"),
    ("J45.909", "Unspecified asthma, uncomplicated"),
    ("M54.5", "Low back pain"),
    ("N39.0", "Urinary tract infection, site not specified"),
    ("K21.9", "Gastro-esophageal reflux disease without esophagitis"),
    ("F32.9", "Major depressive disorder, single episode, unspecified"),
    ("J06.9", "Acute upper respiratory infection, unspecified"),
    ("I25.10", "Atherosclerotic heart disease of native coronary artery"),
    ("E78.5", "Hyperlipidemia, unspecified"),
    ("R51.9", "Headache, unspecified"),
    ("M25.50", "Pain in unspecified joint"),
    ("J44.9", "Chronic obstructive pulmonary disease, unspecified"),
    ("N18.9", "Chronic kidney disease, unspecified"),
    ("E03.9", "Hypothyroidism, unspecified"),
    ("I48.91", "Unspecified atrial fibrillation"),
    ("F41.9", "Anxiety disorder, unspecified"),
    ("R10.9", "Unspecified abdominal pain"),
    ("Z00.00", "Encounter for general adult medical examination"),
    ("A09", "Infectious gastroenteritis and colitis, unspecified"),
    ("J18.9", "Pneumonia, unspecified organism"),
    ("R05.9", "Cough, unspecified"),
    ("G43.909", "Migraine, unspecified, not intractable, without status migrainosus"),
]

# (CPT-like code, short description) — public code system reference values.
PROCEDURE_CODES = [
    ("99213", "Office/outpatient visit, established patient, low complexity"),
    ("99283", "Emergency department visit, moderate severity"),
    ("93000", "Electrocardiogram, routine, with interpretation"),
    ("80053", "Comprehensive metabolic panel"),
    ("71046", "Chest X-ray, 2 views"),
    ("45378", "Colonoscopy, diagnostic"),
    ("29881", "Knee arthroscopy with meniscectomy"),
    ("47562", "Laparoscopic cholecystectomy"),
    ("36415", "Collection of venous blood by venipuncture"),
    ("99396", "Periodic preventive medicine exam, established patient, 40-64 years"),
    ("70450", "CT scan of head/brain without contrast"),
    ("12001", "Simple repair of superficial wound"),
    ("90471", "Immunization administration"),
    ("93306", "Echocardiogram, complete"),
    ("81001", "Urinalysis with microscopy"),
]

# (test name, unit, reference range, plausible value generation bounds)
LAB_TESTS = [
    ("Hemoglobin A1c", "%", "4.0-5.6", 4.0, 12.0),
    ("Fasting Glucose", "mg/dL", "70-99", 60, 260),
    ("Total Cholesterol", "mg/dL", "<200", 120, 300),
    ("LDL Cholesterol", "mg/dL", "<100", 50, 220),
    ("HDL Cholesterol", "mg/dL", "40-60", 20, 90),
    ("Creatinine", "mg/dL", "0.6-1.3", 0.4, 4.5),
    ("Sodium", "mmol/L", "135-145", 125, 155),
    ("Potassium", "mmol/L", "3.5-5.1", 2.8, 6.5),
    ("White Blood Cell Count", "10^3/uL", "4.5-11.0", 2.0, 20.0),
    ("Hemoglobin", "g/dL", "12.0-17.5", 7.0, 19.0),
    ("Platelet Count", "10^3/uL", "150-450", 50, 600),
    ("TSH", "uIU/mL", "0.4-4.0", 0.05, 12.0),
]
--
-- PostgreSQL database dump
--

\restrict vEcGSWhoCGSfIQVgLqQua3Xuvp5RSjH0A15LHD5b3lpmEkgbhYhx7XLXYzpFgw4

-- Dumped from database version 16.14 (Homebrew)
-- Dumped by pg_dump version 16.14 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: analytics; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA analytics;


--
-- Name: audit; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA audit;


--
-- Name: benchmark; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA benchmark;


--
-- Name: staging; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA staging;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: diagnoses; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.diagnoses (
    diagnosis_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    encounter_id character varying(20) NOT NULL,
    diagnosis_code character varying(20) NOT NULL,
    diagnosis_description character varying(300),
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
)
PARTITION BY RANGE (encounter_date);


--
-- Name: encounters_2023; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters_2023 (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters_2024; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters_2024 (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters_2025; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters_2025 (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters_2026; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters_2026 (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters_2027; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters_2027 (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters_2028; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters_2028 (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters_2029; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters_2029 (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters_default; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.encounters_default (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: labs; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.labs (
    lab_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    test_name character varying(150) NOT NULL,
    test_result character varying(100),
    reference_range character varying(100),
    collected_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: patients; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.patients (
    patient_id character varying(20) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    date_of_birth date NOT NULL,
    gender character varying(20),
    zip_code character varying(10),
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: procedures; Type: TABLE; Schema: analytics; Owner: -
--

CREATE TABLE analytics.procedures (
    procedure_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    encounter_id character varying(20) NOT NULL,
    procedure_code character varying(20) NOT NULL,
    procedure_description character varying(300),
    procedure_date date NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: feed_execution; Type: TABLE; Schema: audit; Owner: -
--

CREATE TABLE audit.feed_execution (
    execution_id bigint NOT NULL,
    feed_name character varying(50) NOT NULL,
    batch_id uuid NOT NULL,
    start_time timestamp with time zone DEFAULT now() NOT NULL,
    end_time timestamp with time zone,
    status character varying(20) DEFAULT 'RUNNING'::character varying NOT NULL,
    records_received integer DEFAULT 0 NOT NULL,
    records_processed integer DEFAULT 0 NOT NULL,
    records_failed integer DEFAULT 0 NOT NULL,
    retry_count integer DEFAULT 0 NOT NULL,
    error_message text,
    content_hash character varying(64),
    raw_file_path text,
    CONSTRAINT feed_execution_status_check CHECK (((status)::text = ANY ((ARRAY['RUNNING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying])::text[])))
);


--
-- Name: feed_execution_execution_id_seq; Type: SEQUENCE; Schema: audit; Owner: -
--

CREATE SEQUENCE audit.feed_execution_execution_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: feed_execution_execution_id_seq; Type: SEQUENCE OWNED BY; Schema: audit; Owner: -
--

ALTER SEQUENCE audit.feed_execution_execution_id_seq OWNED BY audit.feed_execution.execution_id;


--
-- Name: diagnoses_baseline; Type: TABLE; Schema: benchmark; Owner: -
--

CREATE TABLE benchmark.diagnoses_baseline (
    diagnosis_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    encounter_id character varying(20) NOT NULL,
    diagnosis_code character varying(20) NOT NULL,
    diagnosis_description character varying(300)
);


--
-- Name: encounters_baseline; Type: TABLE; Schema: benchmark; Owner: -
--

CREATE TABLE benchmark.encounters_baseline (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    length_of_stay_hours numeric(10,2),
    encounter_date date NOT NULL
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: diagnoses; Type: TABLE; Schema: staging; Owner: -
--

CREATE TABLE staging.diagnoses (
    diagnosis_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    encounter_id character varying(20) NOT NULL,
    diagnosis_code character varying(20) NOT NULL,
    diagnosis_description character varying(300),
    batch_id uuid NOT NULL,
    content_hash character varying(64) NOT NULL,
    ingested_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters; Type: TABLE; Schema: staging; Owner: -
--

CREATE TABLE staging.encounters (
    encounter_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    provider_name character varying(150),
    specialty character varying(100),
    facility_name character varying(150),
    encounter_type character varying(50),
    department character varying(100),
    admission_time timestamp with time zone NOT NULL,
    discharge_time timestamp with time zone,
    encounter_date date NOT NULL,
    batch_id uuid NOT NULL,
    content_hash character varying(64) NOT NULL,
    ingested_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: labs; Type: TABLE; Schema: staging; Owner: -
--

CREATE TABLE staging.labs (
    lab_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    test_name character varying(150) NOT NULL,
    test_result character varying(100),
    reference_range character varying(100),
    collected_at timestamp with time zone NOT NULL,
    batch_id uuid NOT NULL,
    content_hash character varying(64) NOT NULL,
    ingested_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: patients; Type: TABLE; Schema: staging; Owner: -
--

CREATE TABLE staging.patients (
    patient_id character varying(20) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    date_of_birth date NOT NULL,
    gender character varying(20),
    zip_code character varying(10),
    batch_id uuid NOT NULL,
    content_hash character varying(64) NOT NULL,
    ingested_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: procedures; Type: TABLE; Schema: staging; Owner: -
--

CREATE TABLE staging.procedures (
    procedure_id character varying(20) NOT NULL,
    patient_id character varying(20) NOT NULL,
    encounter_id character varying(20) NOT NULL,
    procedure_code character varying(20) NOT NULL,
    procedure_description character varying(300),
    procedure_date date NOT NULL,
    batch_id uuid NOT NULL,
    content_hash character varying(64) NOT NULL,
    ingested_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: encounters_2023; Type: TABLE ATTACH; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters ATTACH PARTITION analytics.encounters_2023 FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');


--
-- Name: encounters_2024; Type: TABLE ATTACH; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters ATTACH PARTITION analytics.encounters_2024 FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');


--
-- Name: encounters_2025; Type: TABLE ATTACH; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters ATTACH PARTITION analytics.encounters_2025 FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');


--
-- Name: encounters_2026; Type: TABLE ATTACH; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters ATTACH PARTITION analytics.encounters_2026 FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');


--
-- Name: encounters_2027; Type: TABLE ATTACH; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters ATTACH PARTITION analytics.encounters_2027 FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');


--
-- Name: encounters_2028; Type: TABLE ATTACH; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters ATTACH PARTITION analytics.encounters_2028 FOR VALUES FROM ('2028-01-01') TO ('2029-01-01');


--
-- Name: encounters_2029; Type: TABLE ATTACH; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters ATTACH PARTITION analytics.encounters_2029 FOR VALUES FROM ('2029-01-01') TO ('2030-01-01');


--
-- Name: encounters_default; Type: TABLE ATTACH; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters ATTACH PARTITION analytics.encounters_default DEFAULT;


--
-- Name: feed_execution execution_id; Type: DEFAULT; Schema: audit; Owner: -
--

ALTER TABLE ONLY audit.feed_execution ALTER COLUMN execution_id SET DEFAULT nextval('audit.feed_execution_execution_id_seq'::regclass);


--
-- Name: diagnoses diagnoses_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.diagnoses
    ADD CONSTRAINT diagnoses_pkey PRIMARY KEY (diagnosis_id);


--
-- Name: encounters encounters_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters
    ADD CONSTRAINT encounters_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: encounters_2023 encounters_2023_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters_2023
    ADD CONSTRAINT encounters_2023_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: encounters_2024 encounters_2024_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters_2024
    ADD CONSTRAINT encounters_2024_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: encounters_2025 encounters_2025_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters_2025
    ADD CONSTRAINT encounters_2025_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: encounters_2026 encounters_2026_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters_2026
    ADD CONSTRAINT encounters_2026_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: encounters_2027 encounters_2027_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters_2027
    ADD CONSTRAINT encounters_2027_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: encounters_2028 encounters_2028_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters_2028
    ADD CONSTRAINT encounters_2028_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: encounters_2029 encounters_2029_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters_2029
    ADD CONSTRAINT encounters_2029_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: encounters_default encounters_default_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.encounters_default
    ADD CONSTRAINT encounters_default_pkey PRIMARY KEY (encounter_id, encounter_date);


--
-- Name: labs labs_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.labs
    ADD CONSTRAINT labs_pkey PRIMARY KEY (lab_id);


--
-- Name: patients patients_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (patient_id);


--
-- Name: procedures procedures_pkey; Type: CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.procedures
    ADD CONSTRAINT procedures_pkey PRIMARY KEY (procedure_id);


--
-- Name: feed_execution feed_execution_feed_name_batch_id_key; Type: CONSTRAINT; Schema: audit; Owner: -
--

ALTER TABLE ONLY audit.feed_execution
    ADD CONSTRAINT feed_execution_feed_name_batch_id_key UNIQUE (feed_name, batch_id);


--
-- Name: feed_execution feed_execution_pkey; Type: CONSTRAINT; Schema: audit; Owner: -
--

ALTER TABLE ONLY audit.feed_execution
    ADD CONSTRAINT feed_execution_pkey PRIMARY KEY (execution_id);


--
-- Name: diagnoses_baseline diagnoses_baseline_pkey; Type: CONSTRAINT; Schema: benchmark; Owner: -
--

ALTER TABLE ONLY benchmark.diagnoses_baseline
    ADD CONSTRAINT diagnoses_baseline_pkey PRIMARY KEY (diagnosis_id);


--
-- Name: encounters_baseline encounters_baseline_pkey; Type: CONSTRAINT; Schema: benchmark; Owner: -
--

ALTER TABLE ONLY benchmark.encounters_baseline
    ADD CONSTRAINT encounters_baseline_pkey PRIMARY KEY (encounter_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: diagnoses diagnoses_pkey; Type: CONSTRAINT; Schema: staging; Owner: -
--

ALTER TABLE ONLY staging.diagnoses
    ADD CONSTRAINT diagnoses_pkey PRIMARY KEY (diagnosis_id);


--
-- Name: encounters encounters_pkey; Type: CONSTRAINT; Schema: staging; Owner: -
--

ALTER TABLE ONLY staging.encounters
    ADD CONSTRAINT encounters_pkey PRIMARY KEY (encounter_id);


--
-- Name: labs labs_pkey; Type: CONSTRAINT; Schema: staging; Owner: -
--

ALTER TABLE ONLY staging.labs
    ADD CONSTRAINT labs_pkey PRIMARY KEY (lab_id);


--
-- Name: patients patients_pkey; Type: CONSTRAINT; Schema: staging; Owner: -
--

ALTER TABLE ONLY staging.patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (patient_id);


--
-- Name: procedures procedures_pkey; Type: CONSTRAINT; Schema: staging; Owner: -
--

ALTER TABLE ONLY staging.procedures
    ADD CONSTRAINT procedures_pkey PRIMARY KEY (procedure_id);


--
-- Name: idx_encounters_date; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_encounters_date ON ONLY analytics.encounters USING btree (encounter_date);


--
-- Name: encounters_2023_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2023_encounter_date_idx ON analytics.encounters_2023 USING btree (encounter_date);


--
-- Name: idx_encounters_facility; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_encounters_facility ON ONLY analytics.encounters USING btree (facility_name, encounter_date);


--
-- Name: encounters_2023_facility_name_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2023_facility_name_encounter_date_idx ON analytics.encounters_2023 USING btree (facility_name, encounter_date);


--
-- Name: idx_encounters_patient_date; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_encounters_patient_date ON ONLY analytics.encounters USING btree (patient_id, encounter_date DESC);


--
-- Name: encounters_2023_patient_id_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2023_patient_id_encounter_date_idx ON analytics.encounters_2023 USING btree (patient_id, encounter_date DESC);


--
-- Name: encounters_2024_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2024_encounter_date_idx ON analytics.encounters_2024 USING btree (encounter_date);


--
-- Name: encounters_2024_facility_name_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2024_facility_name_encounter_date_idx ON analytics.encounters_2024 USING btree (facility_name, encounter_date);


--
-- Name: encounters_2024_patient_id_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2024_patient_id_encounter_date_idx ON analytics.encounters_2024 USING btree (patient_id, encounter_date DESC);


--
-- Name: encounters_2025_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2025_encounter_date_idx ON analytics.encounters_2025 USING btree (encounter_date);


--
-- Name: encounters_2025_facility_name_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2025_facility_name_encounter_date_idx ON analytics.encounters_2025 USING btree (facility_name, encounter_date);


--
-- Name: encounters_2025_patient_id_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2025_patient_id_encounter_date_idx ON analytics.encounters_2025 USING btree (patient_id, encounter_date DESC);


--
-- Name: encounters_2026_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2026_encounter_date_idx ON analytics.encounters_2026 USING btree (encounter_date);


--
-- Name: encounters_2026_facility_name_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2026_facility_name_encounter_date_idx ON analytics.encounters_2026 USING btree (facility_name, encounter_date);


--
-- Name: encounters_2026_patient_id_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2026_patient_id_encounter_date_idx ON analytics.encounters_2026 USING btree (patient_id, encounter_date DESC);


--
-- Name: encounters_2027_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2027_encounter_date_idx ON analytics.encounters_2027 USING btree (encounter_date);


--
-- Name: encounters_2027_facility_name_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2027_facility_name_encounter_date_idx ON analytics.encounters_2027 USING btree (facility_name, encounter_date);


--
-- Name: encounters_2027_patient_id_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2027_patient_id_encounter_date_idx ON analytics.encounters_2027 USING btree (patient_id, encounter_date DESC);


--
-- Name: encounters_2028_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2028_encounter_date_idx ON analytics.encounters_2028 USING btree (encounter_date);


--
-- Name: encounters_2028_facility_name_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2028_facility_name_encounter_date_idx ON analytics.encounters_2028 USING btree (facility_name, encounter_date);


--
-- Name: encounters_2028_patient_id_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2028_patient_id_encounter_date_idx ON analytics.encounters_2028 USING btree (patient_id, encounter_date DESC);


--
-- Name: encounters_2029_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2029_encounter_date_idx ON analytics.encounters_2029 USING btree (encounter_date);


--
-- Name: encounters_2029_facility_name_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2029_facility_name_encounter_date_idx ON analytics.encounters_2029 USING btree (facility_name, encounter_date);


--
-- Name: encounters_2029_patient_id_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_2029_patient_id_encounter_date_idx ON analytics.encounters_2029 USING btree (patient_id, encounter_date DESC);


--
-- Name: encounters_default_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_default_encounter_date_idx ON analytics.encounters_default USING btree (encounter_date);


--
-- Name: encounters_default_facility_name_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_default_facility_name_encounter_date_idx ON analytics.encounters_default USING btree (facility_name, encounter_date);


--
-- Name: encounters_default_patient_id_encounter_date_idx; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX encounters_default_patient_id_encounter_date_idx ON analytics.encounters_default USING btree (patient_id, encounter_date DESC);


--
-- Name: idx_diagnoses_code; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_diagnoses_code ON analytics.diagnoses USING btree (diagnosis_code);


--
-- Name: idx_diagnoses_patient; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_diagnoses_patient ON analytics.diagnoses USING btree (patient_id);


--
-- Name: idx_labs_patient_time; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_labs_patient_time ON analytics.labs USING btree (patient_id, collected_at DESC);


--
-- Name: idx_labs_test_name; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_labs_test_name ON analytics.labs USING btree (test_name);


--
-- Name: idx_procedures_code; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_procedures_code ON analytics.procedures USING btree (procedure_code);


--
-- Name: idx_procedures_patient; Type: INDEX; Schema: analytics; Owner: -
--

CREATE INDEX idx_procedures_patient ON analytics.procedures USING btree (patient_id);


--
-- Name: idx_feed_execution_batch; Type: INDEX; Schema: audit; Owner: -
--

CREATE INDEX idx_feed_execution_batch ON audit.feed_execution USING btree (batch_id);


--
-- Name: idx_feed_execution_feed_time; Type: INDEX; Schema: audit; Owner: -
--

CREATE INDEX idx_feed_execution_feed_time ON audit.feed_execution USING btree (feed_name, start_time DESC);


--
-- Name: idx_feed_execution_status; Type: INDEX; Schema: audit; Owner: -
--

CREATE INDEX idx_feed_execution_status ON audit.feed_execution USING btree (status);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: encounters_2023_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_date ATTACH PARTITION analytics.encounters_2023_encounter_date_idx;


--
-- Name: encounters_2023_facility_name_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_facility ATTACH PARTITION analytics.encounters_2023_facility_name_encounter_date_idx;


--
-- Name: encounters_2023_patient_id_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_patient_date ATTACH PARTITION analytics.encounters_2023_patient_id_encounter_date_idx;


--
-- Name: encounters_2023_pkey; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.encounters_pkey ATTACH PARTITION analytics.encounters_2023_pkey;


--
-- Name: encounters_2024_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_date ATTACH PARTITION analytics.encounters_2024_encounter_date_idx;


--
-- Name: encounters_2024_facility_name_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_facility ATTACH PARTITION analytics.encounters_2024_facility_name_encounter_date_idx;


--
-- Name: encounters_2024_patient_id_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_patient_date ATTACH PARTITION analytics.encounters_2024_patient_id_encounter_date_idx;


--
-- Name: encounters_2024_pkey; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.encounters_pkey ATTACH PARTITION analytics.encounters_2024_pkey;


--
-- Name: encounters_2025_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_date ATTACH PARTITION analytics.encounters_2025_encounter_date_idx;


--
-- Name: encounters_2025_facility_name_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_facility ATTACH PARTITION analytics.encounters_2025_facility_name_encounter_date_idx;


--
-- Name: encounters_2025_patient_id_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_patient_date ATTACH PARTITION analytics.encounters_2025_patient_id_encounter_date_idx;


--
-- Name: encounters_2025_pkey; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.encounters_pkey ATTACH PARTITION analytics.encounters_2025_pkey;


--
-- Name: encounters_2026_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_date ATTACH PARTITION analytics.encounters_2026_encounter_date_idx;


--
-- Name: encounters_2026_facility_name_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_facility ATTACH PARTITION analytics.encounters_2026_facility_name_encounter_date_idx;


--
-- Name: encounters_2026_patient_id_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_patient_date ATTACH PARTITION analytics.encounters_2026_patient_id_encounter_date_idx;


--
-- Name: encounters_2026_pkey; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.encounters_pkey ATTACH PARTITION analytics.encounters_2026_pkey;


--
-- Name: encounters_2027_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_date ATTACH PARTITION analytics.encounters_2027_encounter_date_idx;


--
-- Name: encounters_2027_facility_name_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_facility ATTACH PARTITION analytics.encounters_2027_facility_name_encounter_date_idx;


--
-- Name: encounters_2027_patient_id_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_patient_date ATTACH PARTITION analytics.encounters_2027_patient_id_encounter_date_idx;


--
-- Name: encounters_2027_pkey; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.encounters_pkey ATTACH PARTITION analytics.encounters_2027_pkey;


--
-- Name: encounters_2028_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_date ATTACH PARTITION analytics.encounters_2028_encounter_date_idx;


--
-- Name: encounters_2028_facility_name_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_facility ATTACH PARTITION analytics.encounters_2028_facility_name_encounter_date_idx;


--
-- Name: encounters_2028_patient_id_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_patient_date ATTACH PARTITION analytics.encounters_2028_patient_id_encounter_date_idx;


--
-- Name: encounters_2028_pkey; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.encounters_pkey ATTACH PARTITION analytics.encounters_2028_pkey;


--
-- Name: encounters_2029_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_date ATTACH PARTITION analytics.encounters_2029_encounter_date_idx;


--
-- Name: encounters_2029_facility_name_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_facility ATTACH PARTITION analytics.encounters_2029_facility_name_encounter_date_idx;


--
-- Name: encounters_2029_patient_id_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_patient_date ATTACH PARTITION analytics.encounters_2029_patient_id_encounter_date_idx;


--
-- Name: encounters_2029_pkey; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.encounters_pkey ATTACH PARTITION analytics.encounters_2029_pkey;


--
-- Name: encounters_default_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_date ATTACH PARTITION analytics.encounters_default_encounter_date_idx;


--
-- Name: encounters_default_facility_name_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_facility ATTACH PARTITION analytics.encounters_default_facility_name_encounter_date_idx;


--
-- Name: encounters_default_patient_id_encounter_date_idx; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.idx_encounters_patient_date ATTACH PARTITION analytics.encounters_default_patient_id_encounter_date_idx;


--
-- Name: encounters_default_pkey; Type: INDEX ATTACH; Schema: analytics; Owner: -
--

ALTER INDEX analytics.encounters_pkey ATTACH PARTITION analytics.encounters_default_pkey;


--
-- Name: diagnoses diagnoses_patient_id_fkey; Type: FK CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.diagnoses
    ADD CONSTRAINT diagnoses_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES analytics.patients(patient_id);


--
-- Name: encounters encounters_patient_id_fkey; Type: FK CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE analytics.encounters
    ADD CONSTRAINT encounters_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES analytics.patients(patient_id);


--
-- Name: labs labs_patient_id_fkey; Type: FK CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.labs
    ADD CONSTRAINT labs_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES analytics.patients(patient_id);


--
-- Name: procedures procedures_patient_id_fkey; Type: FK CONSTRAINT; Schema: analytics; Owner: -
--

ALTER TABLE ONLY analytics.procedures
    ADD CONSTRAINT procedures_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES analytics.patients(patient_id);


--
-- PostgreSQL database dump complete
--

\unrestrict vEcGSWhoCGSfIQVgLqQua3Xuvp5RSjH0A15LHD5b3lpmEkgbhYhx7XLXYzpFgw4


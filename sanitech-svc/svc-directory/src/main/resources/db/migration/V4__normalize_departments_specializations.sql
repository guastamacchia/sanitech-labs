-- V4__normalize_departments_specializations.sql
-- Normalizza reparti/specializzazioni su tabelle dedicate + relazioni many-to-many.

-- 1) Anagrafica reparti
CREATE TABLE IF NOT EXISTS departments (
    id    BIGSERIAL PRIMARY KEY,
    code  VARCHAR(80)  NOT NULL UNIQUE,
    name  VARCHAR(200) NOT NULL
);

-- 2) Anagrafica specializzazioni
CREATE TABLE IF NOT EXISTS specializations (
    id    BIGSERIAL PRIMARY KEY,
    code  VARCHAR(80)  NOT NULL UNIQUE,
    name  VARCHAR(200) NOT NULL
);

-- 3) Popola anagrafiche dai valori legacy (name = code come placeholder).
WITH legacy_departments AS (
    SELECT DISTINCT UPPER(TRIM(department)) AS code
    FROM doctors
    WHERE department IS NOT NULL
      AND TRIM(department) <> ''
)
INSERT INTO departments(code, name)
SELECT code, code FROM legacy_departments
ON CONFLICT (code) DO NOTHING;

WITH legacy_specializations AS (
    SELECT DISTINCT UPPER(TRIM(speciality)) AS code
    FROM doctors
    WHERE speciality IS NOT NULL
      AND TRIM(speciality) <> ''
)
INSERT INTO specializations(code, name)
SELECT code, code FROM legacy_specializations
ON CONFLICT (code) DO NOTHING;

-- 4) Tabelle di associazione
CREATE TABLE IF NOT EXISTS doctor_departments (
    doctor_id     BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    PRIMARY KEY (doctor_id, department_id),
    CONSTRAINT fk_doctor_departments_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    CONSTRAINT fk_doctor_departments_department
        FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS doctor_specializations (
    doctor_id         BIGINT NOT NULL,
    specialization_id BIGINT NOT NULL,
    PRIMARY KEY (doctor_id, specialization_id),
    CONSTRAINT fk_doctor_specializations_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    CONSTRAINT fk_doctor_specializations_specialization
        FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS patient_departments (
    patient_id    BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    PRIMARY KEY (patient_id, department_id),
    CONSTRAINT fk_patient_departments_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_patient_departments_department
        FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
);

-- 5) Migrazione dati doctors → associazioni many-to-many
INSERT INTO doctor_departments(doctor_id, department_id)
SELECT d.id, dep.id
FROM doctors d
JOIN departments dep ON dep.code = UPPER(TRIM(d.department))
ON CONFLICT DO NOTHING;

INSERT INTO doctor_specializations(doctor_id, specialization_id)
SELECT d.id, sp.id
FROM doctors d
JOIN specializations sp ON sp.code = UPPER(TRIM(d.speciality))
ON CONFLICT DO NOTHING;

-- 6) Rimozione colonne legacy (restano solo liste)
ALTER TABLE doctors DROP COLUMN IF EXISTS department;
ALTER TABLE doctors DROP COLUMN IF EXISTS speciality;

-- 7) Indici utili per filtri
CREATE INDEX IF NOT EXISTS idx_doctor_departments_department_id ON doctor_departments(department_id);
CREATE INDEX IF NOT EXISTS idx_doctor_specializations_specialization_id ON doctor_specializations(specialization_id);
CREATE INDEX IF NOT EXISTS idx_patient_departments_department_id ON patient_departments(department_id);

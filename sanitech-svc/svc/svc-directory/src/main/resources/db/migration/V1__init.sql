-- V1__init.sql
-- Schema base (legacy) per Directory; la normalizzazione arriva in V4.

CREATE TABLE IF NOT EXISTS doctors (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    speciality  VARCHAR(200) NOT NULL,
    department  VARCHAR(200) NOT NULL,
    email       VARCHAR(200) NOT NULL UNIQUE
);

-- Lookup rapido per cognome.
CREATE INDEX IF NOT EXISTS idx_doctors_last_name ON doctors(last_name);

CREATE TABLE IF NOT EXISTS patients (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(200) NOT NULL UNIQUE,
    phone       VARCHAR(50)
);

-- Lookup rapido per cognome.
CREATE INDEX IF NOT EXISTS idx_patients_last_name ON patients(last_name);

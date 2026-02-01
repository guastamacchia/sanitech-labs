-- V1__init.sql
-- Schema iniziale del microservizio Prescribing.

CREATE TABLE IF NOT EXISTS prescriptions (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    doctor_id       BIGINT NOT NULL,
    department_code VARCHAR(80) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    issued_at       TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON prescriptions(patient_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_doctor  ON prescriptions(doctor_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_status  ON prescriptions(status);

CREATE TABLE IF NOT EXISTS prescription_items (
    id              BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medication_code VARCHAR(64),
    medication_name VARCHAR(200) NOT NULL,
    dosage          VARCHAR(100) NOT NULL,
    frequency       VARCHAR(80) NOT NULL,
    duration_days   INTEGER,
    instructions    TEXT,
    sort_order      INTEGER
);

CREATE INDEX IF NOT EXISTS idx_prescription_items_prescription ON prescription_items(prescription_id);

-- =========================================================
-- V1__init.sql
-- Schema di base per il microservizio Sanitech Scheduling.
-- =========================================================

CREATE TABLE IF NOT EXISTS slots (
  id BIGSERIAL PRIMARY KEY,
  doctor_id BIGINT NOT NULL,
  department_code VARCHAR(80) NOT NULL,
  mode VARCHAR(32) NOT NULL,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Evita duplicazioni di slot “identici” per lo stesso medico.
CREATE UNIQUE INDEX IF NOT EXISTS ux_slots_doctor_start ON slots(doctor_id, start_at);

CREATE INDEX IF NOT EXISTS idx_slots_dept_start ON slots(department_code, start_at);
CREATE INDEX IF NOT EXISTS idx_slots_status_start ON slots(status, start_at);

CREATE TABLE IF NOT EXISTS appointments (
  id BIGSERIAL PRIMARY KEY,
  slot_id BIGINT NOT NULL UNIQUE REFERENCES slots(id),
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  department_code VARCHAR(80) NOT NULL,
  mode VARCHAR(32) NOT NULL,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  cancelled_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments(patient_id, start_at);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor ON appointments(doctor_id, start_at);
CREATE INDEX IF NOT EXISTS idx_appointments_dept ON appointments(department_code, start_at);

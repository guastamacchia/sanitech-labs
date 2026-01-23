-- V1__init.sql
-- Schema iniziale per le sessioni di video-visita (Televisit).

CREATE TABLE IF NOT EXISTS televisit_sessions (
  id BIGSERIAL PRIMARY KEY,
  room_name        VARCHAR(128) NOT NULL UNIQUE,
  department       VARCHAR(80)  NOT NULL,
  doctor_subject   VARCHAR(128) NOT NULL,
  patient_subject  VARCHAR(128) NOT NULL,
  scheduled_at     TIMESTAMPTZ  NOT NULL,
  started_at       TIMESTAMPTZ,
  ended_at         TIMESTAMPTZ,
  status           VARCHAR(16)  NOT NULL,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Indici per filtri frequenti.
CREATE INDEX IF NOT EXISTS idx_televisit_dept_status ON televisit_sessions(department, status);
CREATE INDEX IF NOT EXISTS idx_televisit_doctor ON televisit_sessions(doctor_subject);
CREATE INDEX IF NOT EXISTS idx_televisit_patient ON televisit_sessions(patient_subject);

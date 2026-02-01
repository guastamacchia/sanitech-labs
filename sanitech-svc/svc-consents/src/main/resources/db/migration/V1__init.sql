-- V1__init.sql
-- Schema base del bounded context "Consents".
-- Questo servizio gestisce il consenso del PAZIENTE verso un MEDICO
-- (verificato dai servizi clinici quando un medico accede ai dati del paziente).

CREATE TABLE IF NOT EXISTS consents (
  id          BIGSERIAL PRIMARY KEY,
  patient_id  BIGINT      NOT NULL,
  doctor_id   BIGINT      NOT NULL,
  scope       VARCHAR(64) NOT NULL,
  status      VARCHAR(32) NOT NULL,
  granted_at  TIMESTAMPTZ,
  revoked_at  TIMESTAMPTZ,
  expires_at  TIMESTAMPTZ,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_consents_patient_doctor_scope UNIQUE (patient_id, doctor_id, scope)
);

CREATE INDEX IF NOT EXISTS idx_consents_patient ON consents(patient_id);
CREATE INDEX IF NOT EXISTS idx_consents_doctor ON consents(doctor_id);
CREATE INDEX IF NOT EXISTS idx_consents_status ON consents(status);

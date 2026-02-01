-- V4__privacy_consents.sql
-- Tabella per i consensi privacy generici (GDPR, privacy policy, consenso terapia).
-- Questi consensi sono diversi dai consensi medico-specifici nella tabella "consents".

CREATE TABLE IF NOT EXISTS privacy_consents (
  id           BIGSERIAL PRIMARY KEY,
  patient_id   BIGINT       NOT NULL,
  consent_type VARCHAR(32)  NOT NULL,
  accepted     BOOLEAN      NOT NULL DEFAULT FALSE,
  signed_at    TIMESTAMPTZ,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_privacy_consents_patient_type UNIQUE (patient_id, consent_type)
);

CREATE INDEX IF NOT EXISTS idx_privacy_consents_patient ON privacy_consents(patient_id);
CREATE INDEX IF NOT EXISTS idx_privacy_consents_type ON privacy_consents(consent_type);

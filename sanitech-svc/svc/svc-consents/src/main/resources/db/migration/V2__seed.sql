-- V2__seed.sql
-- Seed minimale (solo per ambienti di sviluppo).
-- In produzione è consigliabile disabilitare o rimuovere i seed automatici.

INSERT INTO consents(patient_id, doctor_id, scope, status, granted_at)
VALUES
  (1, 1, 'RECORDS', 'GRANTED', NOW())
ON CONFLICT DO NOTHING;

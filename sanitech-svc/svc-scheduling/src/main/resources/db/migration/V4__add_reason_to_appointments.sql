-- =========================================================
-- V4__add_reason_to_appointments.sql
-- Aggiunge il campo reason per memorizzare il motivo della visita.
-- =========================================================

ALTER TABLE appointments ADD COLUMN IF NOT EXISTS reason VARCHAR(500);

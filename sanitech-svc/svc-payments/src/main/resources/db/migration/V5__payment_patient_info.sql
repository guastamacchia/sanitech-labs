-- V5__payment_patient_info.sql
-- Aggiunge campi email e nome paziente per invio solleciti di pagamento

ALTER TABLE payment_orders ADD COLUMN IF NOT EXISTS patient_email VARCHAR(255);
ALTER TABLE payment_orders ADD COLUMN IF NOT EXISTS patient_name VARCHAR(255);

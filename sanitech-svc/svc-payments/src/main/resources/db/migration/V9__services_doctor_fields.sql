-- Aggiunge campi per il medico erogatore alla tabella services_performed
ALTER TABLE services_performed
    ADD COLUMN IF NOT EXISTS doctor_id BIGINT,
    ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(255);

COMMENT ON COLUMN services_performed.doctor_id IS 'ID del medico che ha erogato la prestazione';
COMMENT ON COLUMN services_performed.doctor_name IS 'Nome del medico (denormalizzato per UI)';

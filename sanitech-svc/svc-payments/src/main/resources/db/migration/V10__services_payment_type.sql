-- Aggiunge colonna payment_type alla tabella services_performed
ALTER TABLE services_performed
    ADD COLUMN payment_type VARCHAR(32);

-- Aggiorna i record esistenti: MEDICAL_VISIT -> VISITA, HOSPITALIZATION -> RICOVERO
UPDATE services_performed
SET payment_type = CASE
    WHEN service_type = 'MEDICAL_VISIT' THEN 'VISITA'
    WHEN service_type = 'HOSPITALIZATION' THEN 'RICOVERO'
    ELSE 'ALTRO'
END
WHERE payment_type IS NULL;

-- Crea indice per migliorare le query filtrate per tipo pagamento
CREATE INDEX idx_sp_payment_type ON services_performed(payment_type);

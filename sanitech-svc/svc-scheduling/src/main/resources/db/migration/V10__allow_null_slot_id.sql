-- Consente slot_id NULL per appuntamenti cancellati/completati il cui slot e' stato riassegnato.
ALTER TABLE appointments ALTER COLUMN slot_id DROP NOT NULL;

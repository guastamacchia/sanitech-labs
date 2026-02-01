-- V6__doctor_phone.sql
-- Aggiunge il telefono ai medici.

ALTER TABLE doctors
    ADD COLUMN IF NOT EXISTS phone VARCHAR(50);

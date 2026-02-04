-- V7__fix_department_capacity_codes.sql
-- Fix: usa i codici reparti corretti esistenti nel database directory

-- Elimina i vecchi record con codici errati (se esistono)
DELETE FROM department_capacity WHERE dept_code LIKE '%_CENTRAL' OR dept_code LIKE '%_NORD' OR dept_code LIKE '%_SUD';

-- Inserisce i record con i codici corretti
INSERT INTO department_capacity (dept_code, total_beds, updated_at) VALUES
    ('CARD', 30, NOW()),
    ('NEURO', 25, NOW()),
    ('ORTO', 40, NOW()),
    ('PNEUMO', 24, NOW()),
    ('DERM', 15, NOW())
ON CONFLICT (dept_code) DO UPDATE SET total_beds = EXCLUDED.total_beds, updated_at = NOW();

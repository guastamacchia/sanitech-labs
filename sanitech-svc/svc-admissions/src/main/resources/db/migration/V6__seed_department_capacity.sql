-- V6__seed_department_capacity.sql
-- Seed data per capacità posti letto dei reparti
-- Allineato con i dati di svc-directory V11__seed_demo_data.sql

-- =============================================================================
-- CAPACITA' POSTI LETTO (Department Capacity)
-- =============================================================================

-- HOSP_CENTRAL (Ospedale Centrale) - 5 reparti
INSERT INTO department_capacity (dept_code, total_beds, updated_at) VALUES
    ('CARD_CENTRAL', 30, NOW()),
    ('NEUR_CENTRAL', 25, NOW()),
    ('ORTH_CENTRAL', 40, NOW()),
    ('PEDI_CENTRAL', 35, NOW()),
    ('EMER_CENTRAL', 20, NOW())
ON CONFLICT (dept_code) DO UPDATE SET total_beds = EXCLUDED.total_beds, updated_at = NOW();

-- HOSP_NORD (Ospedale Nord) - 4 reparti
INSERT INTO department_capacity (dept_code, total_beds, updated_at) VALUES
    ('ONCO_NORD', 28, NOW()),
    ('GAST_NORD', 22, NOW()),
    ('PNEU_NORD', 24, NOW()),
    ('GERI_NORD', 45, NOW())
ON CONFLICT (dept_code) DO UPDATE SET total_beds = EXCLUDED.total_beds, updated_at = NOW();

-- CLINIC_SUD (Clinica Sud) - 3 reparti
INSERT INTO department_capacity (dept_code, total_beds, updated_at) VALUES
    ('DERM_SUD', 15, NOW()),
    ('OFTA_SUD', 12, NOW()),
    ('OTOL_SUD', 10, NOW())
ON CONFLICT (dept_code) DO UPDATE SET total_beds = EXCLUDED.total_beds, updated_at = NOW();

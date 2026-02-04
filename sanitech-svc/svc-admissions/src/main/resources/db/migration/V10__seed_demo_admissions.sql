-- V10__seed_demo_admissions.sql
-- Seed data per ricoveri di sviluppo/demo
-- Allineato con svc-directory V11__seed_demo_data.sql e svc-consents V2__seed_demo_consents.sql
--
-- Medici:
--   1 = Mario Rossi (CARD_CENTRAL)
--   2 = Marco Bianchi (CARD_CENTRAL)
--   4 = Giuseppe Russo (NEUR_CENTRAL)
--
-- Pazienti con consenso RECORDS:
--   1 = Anna Conti
--   2 = Mario Rossi (paziente)
--   3 = Anna Bianchi
--   4 = Luigi Verde
--   5 = Carla Neri (verso Marco Bianchi)

-- =============================================================================
-- RICOVERI (Admissions)
-- =============================================================================

-- Ricoveri dimessi (DISCHARGED) - per storico
INSERT INTO admissions (id, patient_id, department_code, admission_type, status, admitted_at, discharged_at, notes, attending_doctor_id) VALUES
    -- Anna Conti - ricovero cardiologia (5 giorni, dimessa)
    (2001, 1, 'CARD_CENTRAL', 'ORDINARY', 'DISCHARGED',
     NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days',
     'Ricovero per intervento di ablazione. Decorso regolare, dimessa in buone condizioni.', 1),

    -- Anna Bianchi - ricovero neurologia (3 giorni, dimessa)
    (2021, 3, 'NEUR_CENTRAL', 'ORDINARY', 'DISCHARGED',
     NOW() - INTERVAL '16 days', NOW() - INTERVAL '13 days',
     'Accertamenti per cefalea cronica. TAC negativa, prescritta terapia profilattica.', 4),

    -- Luigi Verde - day hospital annullato (paziente non presentato)
    (2030, 4, 'CARD_CENTRAL', 'DAY_HOSPITAL', 'CANCELLED',
     NOW() - INTERVAL '32 days', NOW() - INTERVAL '30 days',
     'Day hospital per monitoraggio aritmia. Paziente non presentato, annullato.', 1)
ON CONFLICT DO NOTHING;

-- Ricoveri attivi (ADMITTED) - occupano posti letto
INSERT INTO admissions (id, patient_id, department_code, admission_type, status, admitted_at, discharged_at, notes, attending_doctor_id) VALUES
    -- Mario Rossi paziente - ricoverato in cardiologia (attivo)
    (3001, 2, 'CARD_CENTRAL', 'ORDINARY', 'ADMITTED',
     NOW() - INTERVAL '2 days', NULL,
     'Ricovero per monitoraggio post-procedurale. Condizioni stabili.', 1),

    -- Carla Neri - ricoverata in cardiologia (attivo)
    (3002, 5, 'CARD_CENTRAL', 'ORDINARY', 'ADMITTED',
     NOW() - INTERVAL '1 day', NULL,
     'Ricovero per approfondimenti cardiologici. In attesa di ecocardiogramma.', 2),

    -- Francesco Gialli (patient_id=6) - ricoverato in neurologia (attivo)
    (3003, 6, 'NEUR_CENTRAL', 'ORDINARY', 'ADMITTED',
     NOW() - INTERVAL '3 days', NULL,
     'Ricovero per crisi epilettica. In osservazione e monitoraggio EEG.', 4)
ON CONFLICT DO NOTHING;

-- Reset sequence
SELECT setval('admissions_id_seq', (SELECT MAX(id) FROM admissions));

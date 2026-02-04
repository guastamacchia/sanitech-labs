-- V5__seed_demo_prescriptions.sql
-- Seed data per prescrizioni di sviluppo/demo
-- Allineato con svc-directory V11__seed_demo_data.sql e svc-consents V2__seed_demo_consents.sql
--
-- Pazienti con consenso PRESCRIPTIONS verso Mario Rossi (doctor_id=1):
--   1 = Anna Conti
--   2 = Mario Rossi (paziente)
--   3 = Anna Bianchi
--   4 = Luigi Verde

-- =============================================================================
-- PRESCRIZIONI
-- =============================================================================

-- Anna Conti (patient_id=1)
INSERT INTO prescriptions (id, patient_id, doctor_id, department_code, status, notes, created_at, updated_at, issued_at) VALUES
    (1, 1, 1, 'CARD_CENTRAL', 'ISSUED', 'Terapia post visita cardiologica', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    (2, 1, 1, 'CARD_CENTRAL', 'ISSUED', 'Controllo pressorio', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
    (3, 1, 2, 'CARD_CENTRAL', 'DRAFT', 'Bozza prescrizione in attesa di conferma', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NULL)
ON CONFLICT DO NOTHING;

-- Mario Rossi paziente (patient_id=2)
INSERT INTO prescriptions (id, patient_id, doctor_id, department_code, status, notes, created_at, updated_at, issued_at) VALUES
    (10, 2, 1, 'CARD_CENTRAL', 'ISSUED', 'Terapia ipertensione', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days')
ON CONFLICT DO NOTHING;

-- Anna Bianchi (patient_id=3)
INSERT INTO prescriptions (id, patient_id, doctor_id, department_code, status, notes, created_at, updated_at, issued_at) VALUES
    (20, 3, 1, 'CARD_CENTRAL', 'ISSUED', 'Follow-up post ecocardiogramma', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
    (21, 3, 4, 'NEUR_CENTRAL', 'ISSUED', 'Terapia profilassi emicrania', NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days')
ON CONFLICT DO NOTHING;

-- Luigi Verde (patient_id=4)
INSERT INTO prescriptions (id, patient_id, doctor_id, department_code, status, notes, created_at, updated_at, issued_at) VALUES
    (30, 4, 1, 'CARD_CENTRAL', 'ISSUED', 'Terapia antiaritmica', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (31, 4, 1, 'CARD_CENTRAL', 'CANCELLED', 'Prescrizione annullata - dosaggio errato', NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days', NOW() - INTERVAL '20 days')
ON CONFLICT DO NOTHING;

-- Carla Neri (patient_id=5) - consenso verso Marco Bianchi
INSERT INTO prescriptions (id, patient_id, doctor_id, department_code, status, notes, created_at, updated_at, issued_at) VALUES
    (40, 5, 2, 'CARD_CENTRAL', 'ISSUED', 'Terapia post test da sforzo', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days')
ON CONFLICT DO NOTHING;

-- Francesco Gialli (patient_id=6) - consenso verso Giuseppe Russo
INSERT INTO prescriptions (id, patient_id, doctor_id, department_code, status, notes, created_at, updated_at, issued_at) VALUES
    (50, 6, 4, 'NEUR_CENTRAL', 'ISSUED', 'Terapia neurologica', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days')
ON CONFLICT DO NOTHING;

-- Reset sequence
SELECT setval('prescriptions_id_seq', (SELECT MAX(id) FROM prescriptions));

-- =============================================================================
-- PRESCRIPTION ITEMS (farmaci/esami)
-- =============================================================================

-- Prescrizione 1 (Anna Conti - terapia post visita)
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order) VALUES
    (1, 'A10BA02', 'Metformina', '500 mg', '2 volte al giorno', 30, 'Assumere ai pasti principali', 1),
    (1, 'C09AA02', 'Enalapril', '5 mg', '1 volta al giorno', 30, 'Assumere al mattino', 2)
ON CONFLICT DO NOTHING;

-- Prescrizione 2 (Anna Conti - controllo pressorio)
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order) VALUES
    (2, 'C03AA03', 'Idroclorotiazide', '12.5 mg', '1 volta al giorno', 30, 'Assumere al mattino', 1)
ON CONFLICT DO NOTHING;

-- Prescrizione 10 (Mario Rossi - ipertensione)
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order) VALUES
    (10, 'C09CA01', 'Losartan', '50 mg', '1 volta al giorno', 30, 'Assumere al mattino a digiuno', 1),
    (10, 'C07AB07', 'Bisoprololo', '2.5 mg', '1 volta al giorno', 30, 'Assumere al mattino', 2)
ON CONFLICT DO NOTHING;

-- Prescrizione 20 (Anna Bianchi - post ecocardiogramma)
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order) VALUES
    (20, 'B01AC06', 'Acido acetilsalicilico', '100 mg', '1 volta al giorno', 90, 'Assumere durante i pasti', 1)
ON CONFLICT DO NOTHING;

-- Prescrizione 21 (Anna Bianchi - profilassi emicrania)
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order) VALUES
    (21, 'N02CC01', 'Sumatriptan', '50 mg', 'Al bisogno', 30, 'Massimo 2 compresse al giorno. Non superare 6 al mese', 1),
    (21, 'N03AX16', 'Topiramato', '25 mg', '2 volte al giorno', 60, 'Iniziare con 25mg sera, aumentare gradualmente', 2)
ON CONFLICT DO NOTHING;

-- Prescrizione 30 (Luigi Verde - antiaritmica)
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order) VALUES
    (30, 'C01BD01', 'Amiodarone', '200 mg', '1 volta al giorno', 30, 'Assumere a stomaco pieno. Controllo TSH ogni 3 mesi', 1)
ON CONFLICT DO NOTHING;

-- Prescrizione 40 (Carla Neri - post test sforzo)
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order) VALUES
    (40, 'C10AA05', 'Atorvastatina', '20 mg', '1 volta al giorno', 90, 'Assumere alla sera', 1)
ON CONFLICT DO NOTHING;

-- Prescrizione 50 (Francesco Gialli - neurologica)
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order) VALUES
    (50, 'N06AB06', 'Sertralina', '50 mg', '1 volta al giorno', 60, 'Assumere al mattino. Non interrompere bruscamente', 1)
ON CONFLICT DO NOTHING;

-- V8__seed_demo_services.sql
-- Seed data per prestazioni erogate di sviluppo/demo
-- Allineato con svc-directory V11__seed_demo_data.sql
--
-- Medici (doctor_id):
--   1 = Mario Rossi (CARD_CENTRAL)
--   2 = Marco Bianchi (CARD_CENTRAL)
--   4 = Giuseppe Russo (NEUR_CENTRAL)
--   10 = Chiara Ferrari (ONCO_NORD)
--
-- Pazienti (patient_id):
--   1 = Anna Conti
--   2 = Mario Rossi (paziente)
--   3 = Anna Bianchi
--   4 = Luigi Verde
--   5 = Carla Neri

-- =============================================================================
-- PRESTAZIONI SANITARIE EROGATE
-- =============================================================================

-- Visite mediche (MEDICAL_VISIT) - 100 EUR
INSERT INTO services_performed (
    service_type, source_type, source_id, patient_id, patient_subject, patient_name, patient_email,
    department_code, description, amount_cents, currency, status, performed_at, started_at,
    reminder_count, last_reminder_at, notes, created_at, updated_at, created_by
) VALUES
    -- Anna Conti - visita cardiologica con Mario Rossi (PAGATA)
    ('MEDICAL_VISIT', 'TELEVISIT', 1001, 1, 'patient:1', 'Anna Conti', 'anna.conti@email.example',
     'CARD_CENTRAL', 'Visita cardiologica di controllo', 10000, 'EUR', 'PAID',
     NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days', 0, NULL, 'Pagamento effettuato online',
     NOW() - INTERVAL '10 days', NOW() - INTERVAL '8 days', 'mario.rossi.dr'),

    -- Anna Conti - visita con Marco Bianchi (PENDING)
    ('MEDICAL_VISIT', 'TELEVISIT', 1002, 1, 'patient:1', 'Anna Conti', 'anna.conti@email.example',
     'CARD_CENTRAL', 'Visita cardiologica interventistica', 10000, 'EUR', 'PENDING',
     NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 1, NOW() - INTERVAL '1 day', NULL,
     NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day', 'marco.bianchi.dr'),

    -- Mario Rossi paziente - visita con se stesso (FREE)
    ('MEDICAL_VISIT', 'TELEVISIT', 1010, 2, 'patient:2', 'Mario Rossi', 'mario.rossi@email.example',
     'CARD_CENTRAL', 'Visita cardiologica (dipendente)', 10000, 'EUR', 'FREE',
     NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', 0, NULL, 'Dipendente struttura - gratuità',
     NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', 'mario.rossi.dr'),

    -- Anna Bianchi - visita cardiologica (PAID)
    ('MEDICAL_VISIT', 'TELEVISIT', 1020, 3, 'patient:3', 'Anna Bianchi', 'anna.bianchi@email.example',
     'CARD_CENTRAL', 'Ecocardiogramma transtoracico', 10000, 'EUR', 'PAID',
     NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', 0, NULL, NULL,
     NOW() - INTERVAL '7 days', NOW() - INTERVAL '5 days', 'mario.rossi.dr'),

    -- Anna Bianchi - visita neurologica (PENDING con solleciti)
    ('MEDICAL_VISIT', 'TELEVISIT', 1021, 3, 'patient:3', 'Anna Bianchi', 'anna.bianchi@email.example',
     'NEUR_CENTRAL', 'Visita neurologica', 10000, 'EUR', 'PENDING',
     NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', 2, NOW() - INTERVAL '5 days', NULL,
     NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 days', 'giuseppe.russo.dr'),

    -- Luigi Verde - visita cardiologica (PENDING)
    ('MEDICAL_VISIT', 'TELEVISIT', 1030, 4, 'patient:4', 'Luigi Verde', 'luigi.verde@email.example',
     'CARD_CENTRAL', 'Holter ECG 24h', 10000, 'EUR', 'PENDING',
     NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 0, NULL, NULL,
     NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 'mario.rossi.dr'),

    -- Carla Neri - test da sforzo (PAID)
    ('MEDICAL_VISIT', 'TELEVISIT', 1040, 5, 'patient:5', 'Carla Neri', 'carla.neri@email.example',
     'CARD_CENTRAL', 'Test da sforzo', 10000, 'EUR', 'PAID',
     NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', 0, NULL, NULL,
     NOW() - INTERVAL '6 days', NOW() - INTERVAL '4 days', 'marco.bianchi.dr'),

    -- Carla Neri - visita oncologica (PENDING)
    ('MEDICAL_VISIT', 'TELEVISIT', 1041, 5, 'patient:5', 'Carla Neri', 'carla.neri@email.example',
     'ONCO_NORD', 'Follow-up oncologico', 10000, 'EUR', 'PENDING',
     NOW() - INTERVAL '17 days', NOW() - INTERVAL '17 days', 3, NOW() - INTERVAL '2 days', NULL,
     NOW() - INTERVAL '17 days', NOW() - INTERVAL '2 days', 'chiara.ferrari.dr')

ON CONFLICT (source_type, source_id) DO NOTHING;

-- Ricoveri (HOSPITALIZATION) - 20 EUR/giorno
INSERT INTO services_performed (
    service_type, source_type, source_id, patient_id, patient_subject, patient_name, patient_email,
    department_code, description, amount_cents, currency, status, performed_at, started_at, days_count,
    reminder_count, last_reminder_at, notes, created_at, updated_at, created_by
) VALUES
    -- Anna Conti - ricovero cardiologia 5 giorni (PAID)
    ('HOSPITALIZATION', 'ADMISSION', 2001, 1, 'patient:1', 'Anna Conti', 'anna.conti@email.example',
     'CARD_CENTRAL', 'Ricovero per intervento cardiaco', 10000, 'EUR', 'PAID',
     NOW() - INTERVAL '25 days', NOW() - INTERVAL '30 days', 5, 0, NULL, 'Pagamento rateizzato',
     NOW() - INTERVAL '25 days', NOW() - INTERVAL '20 days', 'system'),

    -- Anna Bianchi - ricovero neurologia 3 giorni (PENDING)
    ('HOSPITALIZATION', 'ADMISSION', 2021, 3, 'patient:3', 'Anna Bianchi', 'anna.bianchi@email.example',
     'NEUR_CENTRAL', 'Ricovero per accertamenti neurologici', 6000, 'EUR', 'PENDING',
     NOW() - INTERVAL '13 days', NOW() - INTERVAL '16 days', 3, 1, NOW() - INTERVAL '3 days', NULL,
     NOW() - INTERVAL '13 days', NOW() - INTERVAL '3 days', 'system'),

    -- Luigi Verde - ricovero cardiologia 2 giorni (CANCELLED)
    ('HOSPITALIZATION', 'ADMISSION', 2030, 4, 'patient:4', 'Luigi Verde', 'luigi.verde@email.example',
     'CARD_CENTRAL', 'Day hospital per monitoraggio', 4000, 'EUR', 'CANCELLED',
     NOW() - INTERVAL '30 days', NOW() - INTERVAL '32 days', 2, 0, NULL, 'Prestazione annullata - paziente non presentato',
     NOW() - INTERVAL '30 days', NOW() - INTERVAL '28 days', 'system')

ON CONFLICT (source_type, source_id) DO NOTHING;

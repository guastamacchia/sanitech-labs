-- V6__seed_demo_televisits.sql
-- Seed data per sessioni televisita di sviluppo/demo
-- Allineato con svc-directory V11__seed_demo_data.sql
--
-- Medici (subject format: 'doctor:<id>'):
--   1 = Mario Rossi (CARD_CENTRAL)
--   2 = Marco Bianchi (CARD_CENTRAL)
--   4 = Giuseppe Russo (NEUR_CENTRAL)
--
-- Pazienti (subject format: 'patient:<id>'):
--   1 = Anna Conti
--   3 = Anna Bianchi
--   4 = Luigi Verde
--   5 = Carla Neri

-- =============================================================================
-- SESSIONI TELEVISITA
-- =============================================================================

-- Sessioni completate (ENDED) - per storico
INSERT INTO televisit_sessions (id, room_name, department, doctor_subject, patient_subject, scheduled_at, started_at, ended_at, status, created_at, updated_at) VALUES
    -- Anna Conti con Mario Rossi - 10 giorni fa (completata)
    (1001, 'room-1001-card-20250125', 'CARD_CENTRAL', 'doctor:1', 'patient:1',
     NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days' + TIME '09:00', NOW() - INTERVAL '10 days' + TIME '09:28',
     'ENDED', NOW() - INTERVAL '12 days', NOW() - INTERVAL '10 days'),

    -- Anna Conti con Marco Bianchi - 3 giorni fa (completata)
    (1002, 'room-1002-card-20250202', 'CARD_CENTRAL', 'doctor:2', 'patient:1',
     NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + TIME '11:00', NOW() - INTERVAL '3 days' + TIME '11:25',
     'ENDED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '3 days'),

    -- Mario Rossi paziente con Mario Rossi medico - 15 giorni fa (gratuità dipendente)
    (1010, 'room-1010-card-20250120', 'CARD_CENTRAL', 'doctor:1', 'patient:2',
     NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days' + TIME '14:00', NOW() - INTERVAL '15 days' + TIME '14:22',
     'ENDED', NOW() - INTERVAL '17 days', NOW() - INTERVAL '15 days'),

    -- Anna Bianchi con Mario Rossi - 7 giorni fa
    (1020, 'room-1020-card-20250128', 'CARD_CENTRAL', 'doctor:1', 'patient:3',
     NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days' + TIME '10:00', NOW() - INTERVAL '7 days' + TIME '10:35',
     'ENDED', NOW() - INTERVAL '9 days', NOW() - INTERVAL '7 days'),

    -- Anna Bianchi con Giuseppe Russo (Neurologia) - 20 giorni fa
    (1021, 'room-1021-neur-20250115', 'NEUR_CENTRAL', 'doctor:4', 'patient:3',
     NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days' + TIME '09:30', NOW() - INTERVAL '20 days' + TIME '10:05',
     'ENDED', NOW() - INTERVAL '22 days', NOW() - INTERVAL '20 days'),

    -- Luigi Verde con Mario Rossi - 3 giorni fa
    (1030, 'room-1030-card-20250202', 'CARD_CENTRAL', 'doctor:1', 'patient:4',
     NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + TIME '15:00', NOW() - INTERVAL '3 days' + TIME '15:30',
     'ENDED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '3 days'),

    -- Carla Neri con Marco Bianchi - 6 giorni fa
    (1040, 'room-1040-card-20250129', 'CARD_CENTRAL', 'doctor:2', 'patient:5',
     NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days' + TIME '11:30', NOW() - INTERVAL '6 days' + TIME '11:55',
     'ENDED', NOW() - INTERVAL '8 days', NOW() - INTERVAL '6 days'),

    -- Carla Neri con Chiara Ferrari (Oncologia) - 17 giorni fa
    (1041, 'room-1041-onco-20250118', 'ONCO_NORD', 'doctor:10', 'patient:5',
     NOW() - INTERVAL '17 days', NOW() - INTERVAL '17 days' + TIME '14:00', NOW() - INTERVAL '17 days' + TIME '14:40',
     'ENDED', NOW() - INTERVAL '19 days', NOW() - INTERVAL '17 days')
ON CONFLICT (room_name) DO NOTHING;

-- Sessioni programmate future (SCHEDULED)
INSERT INTO televisit_sessions (id, room_name, department, doctor_subject, patient_subject, scheduled_at, started_at, ended_at, status, created_at, updated_at) VALUES
    -- Anna Conti con Mario Rossi - domani
    (2001, 'room-2001-card-future', 'CARD_CENTRAL', 'doctor:1', 'patient:1',
     NOW() + INTERVAL '1 day' + TIME '10:00', NULL, NULL,
     'SCHEDULED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

    -- Luigi Verde con Mario Rossi - domani pomeriggio
    (2002, 'room-2002-card-future', 'CARD_CENTRAL', 'doctor:1', 'patient:4',
     NOW() + INTERVAL '1 day' + TIME '14:30', NULL, NULL,
     'SCHEDULED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

    -- Carla Neri con Marco Bianchi - domani
    (2003, 'room-2003-card-future', 'CARD_CENTRAL', 'doctor:2', 'patient:5',
     NOW() + INTERVAL '1 day' + TIME '11:30', NULL, NULL,
     'SCHEDULED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),

    -- Anna Bianchi con Giuseppe Russo - domani (Neurologia)
    (2004, 'room-2004-neur-future', 'NEUR_CENTRAL', 'doctor:4', 'patient:3',
     NOW() + INTERVAL '1 day' + TIME '09:30', NULL, NULL,
     'SCHEDULED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days')
ON CONFLICT (room_name) DO NOTHING;

-- Reset sequence
SELECT setval('televisit_sessions_id_seq', (SELECT MAX(id) FROM televisit_sessions));

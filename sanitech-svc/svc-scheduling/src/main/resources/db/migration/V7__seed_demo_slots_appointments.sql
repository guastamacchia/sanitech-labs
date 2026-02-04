-- V7__seed_demo_slots_appointments.sql
-- Seed data per slot e appuntamenti di sviluppo/demo
-- Allineato con svc-directory V11__seed_demo_data.sql e svc-consents V2__seed_demo_consents.sql
--
-- Medici:
--   1 = Mario Rossi (CARD_CENTRAL)
--   2 = Marco Bianchi (CARD_CENTRAL)
--   3 = Laura Verdi (CARD_CENTRAL)
--   4 = Giuseppe Russo (NEUR_CENTRAL)

-- =============================================================================
-- SLOT DISPONIBILI (prossimi 7 giorni)
-- =============================================================================

-- Mario Rossi - slot disponibili (AVAILABLE) e prenotati (BOOKED)
INSERT INTO slots (id, doctor_id, department_code, mode, start_at, end_at, status, created_at) VALUES
    -- Domani mattina
    (1, 1, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '09:00', NOW() + INTERVAL '1 day' + TIME '09:30', 'AVAILABLE', NOW()),
    (2, 1, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '09:30', NOW() + INTERVAL '1 day' + TIME '10:00', 'AVAILABLE', NOW()),
    (3, 1, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '10:00', NOW() + INTERVAL '1 day' + TIME '10:30', 'BOOKED', NOW()),
    (4, 1, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '10:30', NOW() + INTERVAL '1 day' + TIME '11:00', 'AVAILABLE', NOW()),
    -- Domani pomeriggio
    (5, 1, 'CARD_CENTRAL', 'IN_PERSON', NOW() + INTERVAL '1 day' + TIME '14:00', NOW() + INTERVAL '1 day' + TIME '14:30', 'AVAILABLE', NOW()),
    (6, 1, 'CARD_CENTRAL', 'IN_PERSON', NOW() + INTERVAL '1 day' + TIME '14:30', NOW() + INTERVAL '1 day' + TIME '15:00', 'BOOKED', NOW()),
    -- Dopodomani
    (7, 1, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '2 days' + TIME '09:00', NOW() + INTERVAL '2 days' + TIME '09:30', 'AVAILABLE', NOW()),
    (8, 1, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '2 days' + TIME '09:30', NOW() + INTERVAL '2 days' + TIME '10:00', 'AVAILABLE', NOW()),
    -- Tra 3 giorni
    (9, 1, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '3 days' + TIME '10:00', NOW() + INTERVAL '3 days' + TIME '10:30', 'AVAILABLE', NOW()),
    (10, 1, 'CARD_CENTRAL', 'IN_PERSON', NOW() + INTERVAL '3 days' + TIME '15:00', NOW() + INTERVAL '3 days' + TIME '15:30', 'AVAILABLE', NOW())
ON CONFLICT DO NOTHING;

-- Marco Bianchi - slot disponibili
INSERT INTO slots (id, doctor_id, department_code, mode, start_at, end_at, status, created_at) VALUES
    (20, 2, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '11:00', NOW() + INTERVAL '1 day' + TIME '11:30', 'AVAILABLE', NOW()),
    (21, 2, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '11:30', NOW() + INTERVAL '1 day' + TIME '12:00', 'BOOKED', NOW()),
    (22, 2, 'CARD_CENTRAL', 'IN_PERSON', NOW() + INTERVAL '2 days' + TIME '14:00', NOW() + INTERVAL '2 days' + TIME '14:30', 'AVAILABLE', NOW()),
    (23, 2, 'CARD_CENTRAL', 'IN_PERSON', NOW() + INTERVAL '2 days' + TIME '14:30', NOW() + INTERVAL '2 days' + TIME '15:00', 'AVAILABLE', NOW())
ON CONFLICT DO NOTHING;

-- Laura Verdi - slot disponibili
INSERT INTO slots (id, doctor_id, department_code, mode, start_at, end_at, status, created_at) VALUES
    (30, 3, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '16:00', NOW() + INTERVAL '1 day' + TIME '16:30', 'AVAILABLE', NOW()),
    (31, 3, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '16:30', NOW() + INTERVAL '1 day' + TIME '17:00', 'AVAILABLE', NOW())
ON CONFLICT DO NOTHING;

-- Giuseppe Russo (Neurologia) - slot disponibili
INSERT INTO slots (id, doctor_id, department_code, mode, start_at, end_at, status, created_at) VALUES
    (40, 4, 'NEUR_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '09:00', NOW() + INTERVAL '1 day' + TIME '09:30', 'AVAILABLE', NOW()),
    (41, 4, 'NEUR_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '09:30', NOW() + INTERVAL '1 day' + TIME '10:00', 'BOOKED', NOW()),
    (42, 4, 'NEUR_CENTRAL', 'IN_PERSON', NOW() + INTERVAL '2 days' + TIME '10:00', NOW() + INTERVAL '2 days' + TIME '10:30', 'AVAILABLE', NOW())
ON CONFLICT DO NOTHING;

-- Reset sequence
SELECT setval('slots_id_seq', (SELECT MAX(id) FROM slots));

-- =============================================================================
-- APPUNTAMENTI (collegati agli slot BOOKED)
-- =============================================================================

-- Appuntamento slot 3: Anna Conti con Mario Rossi (domani mattina)
INSERT INTO appointments (id, slot_id, patient_id, doctor_id, department_code, mode, start_at, end_at, status, created_at) VALUES
    (1, 3, 1, 1, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '10:00', NOW() + INTERVAL '1 day' + TIME '10:30', 'CONFIRMED', NOW() - INTERVAL '2 days')
ON CONFLICT DO NOTHING;

-- Appuntamento slot 6: Luigi Verde con Mario Rossi (domani pomeriggio)
INSERT INTO appointments (id, slot_id, patient_id, doctor_id, department_code, mode, start_at, end_at, status, created_at) VALUES
    (2, 6, 4, 1, 'CARD_CENTRAL', 'IN_PERSON', NOW() + INTERVAL '1 day' + TIME '14:30', NOW() + INTERVAL '1 day' + TIME '15:00', 'CONFIRMED', NOW() - INTERVAL '1 day')
ON CONFLICT DO NOTHING;

-- Appuntamento slot 21: Carla Neri con Marco Bianchi
INSERT INTO appointments (id, slot_id, patient_id, doctor_id, department_code, mode, start_at, end_at, status, created_at) VALUES
    (3, 21, 5, 2, 'CARD_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '11:30', NOW() + INTERVAL '1 day' + TIME '12:00', 'CONFIRMED', NOW() - INTERVAL '3 days')
ON CONFLICT DO NOTHING;

-- Appuntamento slot 41: Anna Bianchi con Giuseppe Russo (Neurologia)
INSERT INTO appointments (id, slot_id, patient_id, doctor_id, department_code, mode, start_at, end_at, status, created_at) VALUES
    (4, 41, 3, 4, 'NEUR_CENTRAL', 'REMOTE', NOW() + INTERVAL '1 day' + TIME '09:30', NOW() + INTERVAL '1 day' + TIME '10:00', 'CONFIRMED', NOW() - INTERVAL '5 days')
ON CONFLICT DO NOTHING;

-- Reset sequence
SELECT setval('appointments_id_seq', (SELECT MAX(id) FROM appointments));

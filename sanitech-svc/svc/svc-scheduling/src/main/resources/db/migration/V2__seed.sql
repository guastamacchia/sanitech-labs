-- =========================================================
-- V2__seed.sql
-- Dati di esempio per sviluppo locale.
-- =========================================================

INSERT INTO slots(doctor_id, department_code, mode, start_at, end_at, status) VALUES
 (1001, 'CARD', 'IN_PERSON', now() + interval '1 day',  now() + interval '1 day' + interval '30 minutes', 'AVAILABLE'),
 (1001, 'CARD', 'TELEVISIT', now() + interval '2 day',  now() + interval '2 day' + interval '30 minutes', 'AVAILABLE'),
 (1002, 'DERM', 'IN_PERSON', now() + interval '1 day',  now() + interval '1 day' + interval '45 minutes', 'AVAILABLE');

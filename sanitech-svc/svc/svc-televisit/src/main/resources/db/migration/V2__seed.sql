-- V2__seed.sql
-- Seed dati di esempio (solo ambienti dev).

INSERT INTO televisit_sessions(room_name, department, doctor_subject, patient_subject, scheduled_at, status)
VALUES
  ('tv-demo-1', 'HEART', 'doctor-sub-1', 'patient-sub-1', NOW() + INTERVAL '1 day', 'CREATED'),
  ('tv-demo-2', 'RESP',  'doctor-sub-2', 'patient-sub-2', NOW() + INTERVAL '2 days', 'CREATED')
ON CONFLICT (room_name) DO NOTHING;

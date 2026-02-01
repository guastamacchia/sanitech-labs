-- V2__seed.sql
-- Seed demo idempotente (solo DEV/DEMO).

INSERT INTO doctors (first_name, last_name, speciality, department, email)
VALUES
  ('Mario',  'Rossi',  'CARDIOLOGY',  'HEART',  'rossi@sanitech.example')
ON CONFLICT (email) DO NOTHING;

INSERT INTO patients (first_name, last_name, email, phone)
VALUES
  ('Anna',  'Conti',  'conti@sanitech.example', NULL)
ON CONFLICT (email) DO NOTHING;

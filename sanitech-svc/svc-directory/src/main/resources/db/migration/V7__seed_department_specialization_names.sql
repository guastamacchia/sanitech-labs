-- V7__seed_department_specialization_names.sql
-- Aggiorna le etichette italiane per reparti e specializzazioni.

-- Reparti
UPDATE departments
SET name = 'Cardiologia'
WHERE code = 'CARD'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE departments
SET name = 'Neurologia'
WHERE code = 'NEURO'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE departments
SET name = 'Dermatologia'
WHERE code = 'DERM'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE departments
SET name = 'Ortopedia'
WHERE code = 'ORTHO'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE departments
SET name = 'Pneumologia'
WHERE code = 'PNEUMO'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE departments
SET name = 'Cardiologia'
WHERE code = 'HEART'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE departments
SET name = 'Metabolismo'
WHERE code = 'METAB'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE departments
SET name = 'Pneumologia'
WHERE code = 'RESP'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

-- Specializzazioni
UPDATE specializations
SET name = 'Cardiologia'
WHERE code = 'CARDIO'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE specializations
SET name = 'Cardiologia'
WHERE code = 'CARDIOLOGY'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE specializations
SET name = 'Diabetologia'
WHERE code = 'DIABETOLOGY'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE specializations
SET name = 'Pneumologia'
WHERE code = 'PNEUMOLOGY'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE specializations
SET name = 'Neurologia'
WHERE code = 'NEURO'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE specializations
SET name = 'Dermatologia'
WHERE code = 'DERM'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE specializations
SET name = 'Ortopedia'
WHERE code = 'ORTHO'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

UPDATE specializations
SET name = 'Pneumologia'
WHERE code = 'PNEUMO'
  AND (name IS NULL OR name = '' OR UPPER(name) = code);

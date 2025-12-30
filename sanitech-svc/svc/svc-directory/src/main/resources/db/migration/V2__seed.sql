-- V2__seed.sql
-- Dati di esempio (solo per ambienti DEV/DEMO).

INSERT INTO doctors (first_name, last_name, speciality, department, email)
VALUES
  ('Mario',  'Rossi',  'CARDIOLOGY',  'HEART',  'mario.rossi@sanitech.it'),
  ('Giulia', 'Bianchi','DIABETOLOGY', 'METAB',  'giulia.bianchi@sanitech.it'),
  ('Luca',   'Verdi',  'PNEUMOLOGY',  'RESP',   'luca.verdi@sanitech.it');

INSERT INTO patients (first_name, last_name, email, phone)
VALUES
  ('Anna',  'Neri',   'anna.neri@sanitech.it',  '+39 333 000 111'),
  ('Paolo', 'Gialli', 'paolo.gialli@sanitech.it', NULL),
  ('Sara',  'Blu',    'sara.blu@sanitech.it',    '+39 333 000 222');

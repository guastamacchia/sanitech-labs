-- V11__seed_demo_data.sql
-- Seed data completi per ambiente di sviluppo/demo:
-- - Reparti per ogni struttura
-- - Medici per ogni reparto
-- - Pazienti demo

-- =============================================================================
-- REPARTI (Departments)
-- =============================================================================

-- HOSP_CENTRAL (Ospedale Centrale) - 5 reparti
INSERT INTO departments (code, name, facility_id, capacity) VALUES
    ('CARD_CENTRAL', 'Cardiologia', (SELECT id FROM facilities WHERE code = 'HOSP_CENTRAL'), 30),
    ('NEUR_CENTRAL', 'Neurologia', (SELECT id FROM facilities WHERE code = 'HOSP_CENTRAL'), 25),
    ('ORTH_CENTRAL', 'Ortopedia', (SELECT id FROM facilities WHERE code = 'HOSP_CENTRAL'), 40),
    ('PEDI_CENTRAL', 'Pediatria', (SELECT id FROM facilities WHERE code = 'HOSP_CENTRAL'), 35),
    ('EMER_CENTRAL', 'Pronto Soccorso', (SELECT id FROM facilities WHERE code = 'HOSP_CENTRAL'), 20)
ON CONFLICT (code) DO NOTHING;

-- HOSP_NORD (Ospedale Nord) - 4 reparti
INSERT INTO departments (code, name, facility_id, capacity) VALUES
    ('ONCO_NORD', 'Oncologia', (SELECT id FROM facilities WHERE code = 'HOSP_NORD'), 28),
    ('GAST_NORD', 'Gastroenterologia', (SELECT id FROM facilities WHERE code = 'HOSP_NORD'), 22),
    ('PNEU_NORD', 'Pneumologia', (SELECT id FROM facilities WHERE code = 'HOSP_NORD'), 24),
    ('GERI_NORD', 'Geriatria', (SELECT id FROM facilities WHERE code = 'HOSP_NORD'), 45)
ON CONFLICT (code) DO NOTHING;

-- CLINIC_SUD (Clinica Sud) - 3 reparti
INSERT INTO departments (code, name, facility_id, capacity) VALUES
    ('DERM_SUD', 'Dermatologia', (SELECT id FROM facilities WHERE code = 'CLINIC_SUD'), 15),
    ('OFTA_SUD', 'Oculistica', (SELECT id FROM facilities WHERE code = 'CLINIC_SUD'), 12),
    ('OTOL_SUD', 'Otorinolaringoiatria', (SELECT id FROM facilities WHERE code = 'CLINIC_SUD'), 10)
ON CONFLICT (code) DO NOTHING;

-- =============================================================================
-- MEDICI (Doctors)
-- Gli ID Keycloak vengono usati come riferimento; gli username corrispondono
-- alle utenze nel realm sanitech-realm.json
-- =============================================================================

-- HOSP_CENTRAL - Cardiologia
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Marco', 'Bianchi', 'marco.bianchi@sanitech.example', '+39 02 1234001', 'Cardiologia interventistica',
     (SELECT id FROM departments WHERE code = 'CARD_CENTRAL'), 'ACTIVE', NOW(), NOW()),
    ('Laura', 'Verdi', 'laura.verdi@sanitech.example', '+39 02 1234002', 'Elettrofisiologia cardiaca',
     (SELECT id FROM departments WHERE code = 'CARD_CENTRAL'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- HOSP_CENTRAL - Neurologia
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Giuseppe', 'Russo', 'giuseppe.russo@sanitech.example', '+39 02 1234003', 'Neurologia clinica',
     (SELECT id FROM departments WHERE code = 'NEUR_CENTRAL'), 'ACTIVE', NOW(), NOW()),
    ('Francesca', 'Romano', 'francesca.romano@sanitech.example', '+39 02 1234004', 'Neurofisiologia',
     (SELECT id FROM departments WHERE code = 'NEUR_CENTRAL'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- HOSP_CENTRAL - Ortopedia
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Andrea', 'Colombo', 'andrea.colombo@sanitech.example', '+39 02 1234005', 'Chirurgia ortopedica',
     (SELECT id FROM departments WHERE code = 'ORTH_CENTRAL'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- HOSP_CENTRAL - Pediatria
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Elena', 'Ricci', 'elena.ricci@sanitech.example', '+39 02 1234006', 'Pediatria generale',
     (SELECT id FROM departments WHERE code = 'PEDI_CENTRAL'), 'ACTIVE', NOW(), NOW()),
    ('Paolo', 'Gallo', 'paolo.gallo@sanitech.example', '+39 02 1234007', 'Neonatologia',
     (SELECT id FROM departments WHERE code = 'PEDI_CENTRAL'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- HOSP_CENTRAL - Pronto Soccorso
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Simone', 'Conti', 'simone.conti@sanitech.example', '+39 02 1234008', 'Medicina d''urgenza',
     (SELECT id FROM departments WHERE code = 'EMER_CENTRAL'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- HOSP_NORD - Oncologia
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Chiara', 'Ferrari', 'chiara.ferrari@sanitech.example', '+39 02 1234009', 'Oncologia medica',
     (SELECT id FROM departments WHERE code = 'ONCO_NORD'), 'ACTIVE', NOW(), NOW()),
    ('Matteo', 'Esposito', 'matteo.esposito@sanitech.example', '+39 02 1234010', 'Radioterapia oncologica',
     (SELECT id FROM departments WHERE code = 'ONCO_NORD'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- HOSP_NORD - Gastroenterologia
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Luca', 'Martini', 'luca.martini@sanitech.example', '+39 02 1234011', 'Endoscopia digestiva',
     (SELECT id FROM departments WHERE code = 'GAST_NORD'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- HOSP_NORD - Pneumologia
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Giulia', 'Barbieri', 'giulia.barbieri@sanitech.example', '+39 02 1234012', 'Pneumologia interventistica',
     (SELECT id FROM departments WHERE code = 'PNEU_NORD'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- HOSP_NORD - Geriatria
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Roberto', 'Bruno', 'roberto.bruno@sanitech.example', '+39 02 1234013', 'Geriatria e cure palliative',
     (SELECT id FROM departments WHERE code = 'GERI_NORD'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- CLINIC_SUD - Dermatologia
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Valentina', 'Fontana', 'valentina.fontana@sanitech.example', '+39 02 1234014', 'Dermatologia clinica',
     (SELECT id FROM departments WHERE code = 'DERM_SUD'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- CLINIC_SUD - Oculistica
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Alessandro', 'Santoro', 'alessandro.santoro@sanitech.example', '+39 02 1234015', 'Chirurgia refrattiva',
     (SELECT id FROM departments WHERE code = 'OFTA_SUD'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- CLINIC_SUD - Otorinolaringoiatria
INSERT INTO doctors (first_name, last_name, email, phone, specialization, department_id, status, created_at, activated_at) VALUES
    ('Silvia', 'Marini', 'silvia.marini@sanitech.example', '+39 02 1234016', 'Audiologia e foniatria',
     (SELECT id FROM departments WHERE code = 'OTOL_SUD'), 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- =============================================================================
-- PAZIENTI (Patients)
-- =============================================================================

INSERT INTO patients (first_name, last_name, email, phone, fiscal_code, birth_date, address, status, registered_at, activated_at) VALUES
    ('Mario', 'Rossi', 'mario.rossi@email.example', '+39 333 1000001', 'RSSMRA80A01H501A', '1980-01-01',
     'Via Roma 10, 00100 Roma RM', 'ACTIVE', NOW(), NOW()),
    ('Anna', 'Bianchi', 'anna.bianchi@email.example', '+39 333 1000002', 'BNCNNA85B42F205B', '1985-02-02',
     'Via Milano 20, 20100 Milano MI', 'ACTIVE', NOW(), NOW()),
    ('Luigi', 'Verde', 'luigi.verde@email.example', '+39 333 1000003', 'VRDLGU75C03L219C', '1975-03-03',
     'Via Napoli 30, 80100 Napoli NA', 'ACTIVE', NOW(), NOW()),
    ('Carla', 'Neri', 'carla.neri@email.example', '+39 333 1000004', 'NRECRL90D44G273D', '1990-04-04',
     'Via Torino 40, 10100 Torino TO', 'ACTIVE', NOW(), NOW()),
    ('Francesco', 'Gialli', 'francesco.gialli@email.example', '+39 333 1000005', 'GLLFNC88E05A944E', '1988-05-05',
     'Via Firenze 50, 50100 Firenze FI', 'ACTIVE', NOW(), NOW()),
    ('Giovanna', 'Viola', 'giovanna.viola@email.example', '+39 333 1000006', 'VLRGNN82F46D612F', '1982-06-06',
     'Via Bologna 60, 40100 Bologna BO', 'ACTIVE', NOW(), NOW()),
    ('Pietro', 'Azzurri', 'pietro.azzurri@email.example', '+39 333 1000007', 'ZZRPTR78G07H501G', '1978-07-07',
     'Via Genova 70, 16100 Genova GE', 'ACTIVE', NOW(), NOW()),
    ('Maria', 'Grigi', 'maria.grigi@email.example', '+39 333 1000008', 'GRGMRA92H48C351H', '1992-08-08',
     'Via Palermo 80, 90100 Palermo PA', 'ACTIVE', NOW(), NOW()),
    ('Stefano', 'Marroni', 'stefano.marroni@email.example', '+39 333 1000009', 'MRRSFN70I09B354I', '1970-09-09',
     'Via Venezia 90, 30100 Venezia VE', 'PENDING', NOW(), NULL),
    ('Teresa', 'Rosa', 'teresa.rosa@email.example', '+39 333 1000010', 'RSATRS95L50A662L', '1995-07-10',
     'Via Bari 100, 70100 Bari BA', 'PENDING', NOW(), NULL)
ON CONFLICT (email) DO NOTHING;

-- =============================================================================
-- ASSOCIAZIONI PAZIENTE-REPARTO (patient_departments)
-- Associamo i pazienti ad alcuni reparti per test
-- =============================================================================

-- Mario Rossi -> Cardiologia
INSERT INTO patient_departments (patient_id, department_id)
SELECT p.id, d.id FROM patients p, departments d
WHERE p.email = 'mario.rossi@email.example' AND d.code = 'CARD_CENTRAL'
ON CONFLICT DO NOTHING;

-- Anna Bianchi -> Neurologia
INSERT INTO patient_departments (patient_id, department_id)
SELECT p.id, d.id FROM patients p, departments d
WHERE p.email = 'anna.bianchi@email.example' AND d.code = 'NEUR_CENTRAL'
ON CONFLICT DO NOTHING;

-- Luigi Verde -> Ortopedia
INSERT INTO patient_departments (patient_id, department_id)
SELECT p.id, d.id FROM patients p, departments d
WHERE p.email = 'luigi.verde@email.example' AND d.code = 'ORTH_CENTRAL'
ON CONFLICT DO NOTHING;

-- Carla Neri -> Oncologia
INSERT INTO patient_departments (patient_id, department_id)
SELECT p.id, d.id FROM patients p, departments d
WHERE p.email = 'carla.neri@email.example' AND d.code = 'ONCO_NORD'
ON CONFLICT DO NOTHING;

-- Francesco Gialli -> Gastroenterologia
INSERT INTO patient_departments (patient_id, department_id)
SELECT p.id, d.id FROM patients p, departments d
WHERE p.email = 'francesco.gialli@email.example' AND d.code = 'GAST_NORD'
ON CONFLICT DO NOTHING;

-- Giovanna Viola -> Dermatologia
INSERT INTO patient_departments (patient_id, department_id)
SELECT p.id, d.id FROM patients p, departments d
WHERE p.email = 'giovanna.viola@email.example' AND d.code = 'DERM_SUD'
ON CONFLICT DO NOTHING;

-- Pietro Azzurri -> Pediatria (es. genitore)
INSERT INTO patient_departments (patient_id, department_id)
SELECT p.id, d.id FROM patients p, departments d
WHERE p.email = 'pietro.azzurri@email.example' AND d.code = 'PEDI_CENTRAL'
ON CONFLICT DO NOTHING;

-- Maria Grigi -> Oculistica
INSERT INTO patient_departments (patient_id, department_id)
SELECT p.id, d.id FROM patients p, departments d
WHERE p.email = 'maria.grigi@email.example' AND d.code = 'OFTA_SUD'
ON CONFLICT DO NOTHING;

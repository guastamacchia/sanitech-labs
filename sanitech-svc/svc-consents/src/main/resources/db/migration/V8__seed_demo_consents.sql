-- V2__seed_demo_consents.sql
-- Seed data per consensi di sviluppo/demo
-- Allineato con svc-directory V11__seed_demo_data.sql
--
-- NOTA: Gli ID patient_id e doctor_id corrispondono all'ordine di inserimento
-- in svc-directory V11__seed_demo_data.sql:
--
-- Medici (doctor_id):
--   1 = Mario Rossi (CARD_CENTRAL)
--   2 = Marco Bianchi (CARD_CENTRAL)
--   3 = Laura Verdi (CARD_CENTRAL)
--   4 = Giuseppe Russo (NEUR_CENTRAL)
--   5 = Francesca Romano (NEUR_CENTRAL)
--   6 = Andrea Colombo (ORTH_CENTRAL)
--   7 = Elena Ricci (PEDI_CENTRAL)
--   8 = Paolo Gallo (PEDI_CENTRAL)
--   9 = Simone Conti (EMER_CENTRAL)
--   10 = Chiara Ferrari (ONCO_NORD)
--   11 = Matteo Esposito (ONCO_NORD)
--   12 = Luca Martini (GAST_NORD)
--   13 = Giulia Barbieri (PNEU_NORD)
--   14 = Roberto Bruno (GERI_NORD)
--   15 = Valentina Fontana (DERM_SUD)
--   16 = Alessandro Santoro (OFTA_SUD)
--   17 = Silvia Marini (OTOL_SUD)
--
-- Pazienti (patient_id):
--   1 = Anna Conti
--   2 = Mario Rossi (anche paziente)
--   3 = Anna Bianchi
--   4 = Luigi Verde
--   5 = Carla Neri
--   6 = Francesco Gialli
--   7 = Giovanna Viola
--   8 = Pietro Azzurri
--   9 = Maria Grigi
--   10 = Stefano Marroni (PENDING)
--   11 = Teresa Rosa (PENDING)

-- =============================================================================
-- CONSENSI DOCS (Documenti Clinici)
-- =============================================================================

-- Consensi verso Mario Rossi (doctor_id=1)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 1, 'DOCS', 'GRANTED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),  -- Anna Conti -> Mario Rossi
    (2, 1, 'DOCS', 'GRANTED', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days', NOW()),  -- Mario Rossi -> Mario Rossi
    (3, 1, 'DOCS', 'GRANTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', NOW()),  -- Anna Bianchi -> Mario Rossi
    (4, 1, 'DOCS', 'GRANTED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', NOW())   -- Luigi Verde -> Mario Rossi
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Marco Bianchi (doctor_id=2)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 2, 'DOCS', 'GRANTED', NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days', NOW()),  -- Anna Conti -> Marco Bianchi
    (5, 2, 'DOCS', 'GRANTED', NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days', NOW())   -- Carla Neri -> Marco Bianchi
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Laura Verdi (doctor_id=3)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 3, 'DOCS', 'GRANTED', NOW() - INTERVAL '27 days', NOW() - INTERVAL '27 days', NOW())   -- Anna Conti -> Laura Verdi
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Giuseppe Russo (doctor_id=4 - Neurologia)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (3, 4, 'DOCS', 'GRANTED', NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days', NOW()),  -- Anna Bianchi -> Giuseppe Russo
    (6, 4, 'DOCS', 'GRANTED', NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days', NOW())   -- Francesco Gialli -> Giuseppe Russo
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Chiara Ferrari (doctor_id=10 - Oncologia)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (5, 10, 'DOCS', 'GRANTED', NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days', NOW())   -- Carla Neri -> Chiara Ferrari
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- =============================================================================
-- CONSENSI PRESCRIPTIONS (Prescrizioni)
-- =============================================================================

-- Consensi verso Mario Rossi (doctor_id=1)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 1, 'PRESCRIPTIONS', 'GRANTED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),  -- Anna Conti
    (2, 1, 'PRESCRIPTIONS', 'GRANTED', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days', NOW()),  -- Mario Rossi
    (3, 1, 'PRESCRIPTIONS', 'GRANTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', NOW()),  -- Anna Bianchi
    (4, 1, 'PRESCRIPTIONS', 'GRANTED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', NOW())   -- Luigi Verde
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Marco Bianchi (doctor_id=2)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 2, 'PRESCRIPTIONS', 'GRANTED', NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days', NOW()),
    (5, 2, 'PRESCRIPTIONS', 'GRANTED', NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days', NOW())
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Giuseppe Russo (doctor_id=4)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (3, 4, 'PRESCRIPTIONS', 'GRANTED', NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days', NOW()),
    (6, 4, 'PRESCRIPTIONS', 'GRANTED', NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days', NOW())
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- =============================================================================
-- CONSENSI TELEVISIT (Televisite)
-- =============================================================================

-- Consensi verso Mario Rossi (doctor_id=1)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 1, 'TELEVISIT', 'GRANTED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),
    (2, 1, 'TELEVISIT', 'GRANTED', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days', NOW()),
    (4, 1, 'TELEVISIT', 'GRANTED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', NOW())
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Marco Bianchi (doctor_id=2)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 2, 'TELEVISIT', 'GRANTED', NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days', NOW())
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- =============================================================================
-- CONSENSI RECORDS (Ricoveri)
-- =============================================================================

-- Consensi verso Mario Rossi (doctor_id=1)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 1, 'RECORDS', 'GRANTED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),  -- Anna Conti
    (2, 1, 'RECORDS', 'GRANTED', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days', NOW()),  -- Mario Rossi
    (3, 1, 'RECORDS', 'GRANTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', NOW()),  -- Anna Bianchi
    (4, 1, 'RECORDS', 'GRANTED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', NOW())   -- Luigi Verde
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Marco Bianchi (doctor_id=2)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 2, 'RECORDS', 'GRANTED', NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days', NOW()),
    (5, 2, 'RECORDS', 'GRANTED', NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days', NOW())
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

-- Consensi verso Laura Verdi (doctor_id=3)
INSERT INTO consents (patient_id, doctor_id, scope, status, granted_at, created_at, updated_at) VALUES
    (1, 3, 'RECORDS', 'GRANTED', NOW() - INTERVAL '27 days', NOW() - INTERVAL '27 days', NOW())
ON CONFLICT (patient_id, doctor_id, scope) DO UPDATE SET status = 'GRANTED', granted_at = NOW(), updated_at = NOW();

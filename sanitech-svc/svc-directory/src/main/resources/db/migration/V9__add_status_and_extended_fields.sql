-- V9__add_status_and_extended_fields.sql
-- Aggiunge campi per gestione stato utenti, specializzazione medici,
-- dati anagrafici estesi pazienti, indirizzo strutture e capacità reparti.

-- 1. ENUM per lo stato degli utenti (medici e pazienti)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_status') THEN
        CREATE TYPE user_status AS ENUM ('PENDING', 'ACTIVE', 'DISABLED');
    END IF;
END $$;

-- 2. Aggiungere campi a doctors
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS status user_status NOT NULL DEFAULT 'PENDING';
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS specialization VARCHAR(200);
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS activated_at TIMESTAMP;

-- Indice per ricerche su stato
CREATE INDEX IF NOT EXISTS idx_doctors_status ON doctors(status);

-- 3. Aggiungere campi a patients
ALTER TABLE patients ADD COLUMN IF NOT EXISTS status user_status NOT NULL DEFAULT 'PENDING';
ALTER TABLE patients ADD COLUMN IF NOT EXISTS fiscal_code VARCHAR(16);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS birth_date DATE;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS registered_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE patients ADD COLUMN IF NOT EXISTS activated_at TIMESTAMP;

-- Vincolo di unicità sul codice fiscale (se valorizzato)
CREATE UNIQUE INDEX IF NOT EXISTS idx_patients_fiscal_code ON patients(fiscal_code)
    WHERE fiscal_code IS NOT NULL;

-- Indice per ricerche su stato
CREATE INDEX IF NOT EXISTS idx_patients_status ON patients(status);

-- 4. Aggiungere campi a facilities
ALTER TABLE facilities ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE facilities ADD COLUMN IF NOT EXISTS phone VARCHAR(50);

-- 5. Aggiungere campi a departments
ALTER TABLE departments ADD COLUMN IF NOT EXISTS capacity INTEGER DEFAULT 0;

-- 6. Aggiornare i record esistenti a stato ACTIVE (erano già in produzione)
UPDATE doctors SET status = 'ACTIVE', activated_at = created_at WHERE status = 'PENDING';
UPDATE patients SET status = 'ACTIVE', activated_at = registered_at WHERE status = 'PENDING';

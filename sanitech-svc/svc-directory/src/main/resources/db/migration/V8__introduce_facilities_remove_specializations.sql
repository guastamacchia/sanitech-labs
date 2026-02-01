-- V8__introduce_facilities_remove_specializations.sql
-- Introduce il concetto di Struttura (Facility) e rimuove le Specializzazioni.
-- Nuovo modello: Network -> Strutture -> Reparti -> Medici

-- 1. Creare la tabella facilities (Strutture)
CREATE TABLE IF NOT EXISTS facilities (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(200) NOT NULL,
    CONSTRAINT uk_facilities_code UNIQUE (code)
);

-- 2. Inserire strutture predefinite
INSERT INTO facilities (code, name) VALUES
    ('HOSP_CENTRAL', 'Ospedale Centrale'),
    ('HOSP_NORD', 'Ospedale Nord'),
    ('CLINIC_SUD', 'Clinica Sud')
ON CONFLICT (code) DO NOTHING;

-- 3. Aggiungere colonna facility_id alla tabella departments (nullable inizialmente)
ALTER TABLE departments ADD COLUMN IF NOT EXISTS facility_id BIGINT;

-- 4. Associare i reparti esistenti alla prima struttura disponibile
UPDATE departments
SET facility_id = (SELECT MIN(id) FROM facilities)
WHERE facility_id IS NULL;

-- 5. Rendere la colonna NOT NULL e aggiungere vincolo FK
ALTER TABLE departments ALTER COLUMN facility_id SET NOT NULL;
ALTER TABLE departments ADD CONSTRAINT fk_departments_facility
    FOREIGN KEY (facility_id) REFERENCES facilities(id);

-- 6. Rimuovere il vincolo FK su specialization_id dalla tabella doctors (se esiste)
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT tc.constraint_name INTO constraint_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
    WHERE tc.table_name = 'doctors'
    AND kcu.column_name = 'specialization_id'
    AND tc.constraint_type = 'FOREIGN KEY'
    LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE 'ALTER TABLE doctors DROP CONSTRAINT ' || constraint_name;
    END IF;
END $$;

-- 7. Rimuovere la colonna specialization_id dalla tabella doctors (se esiste)
ALTER TABLE doctors DROP COLUMN IF EXISTS specialization_id;

-- 8. Eliminare la tabella specializations
DROP TABLE IF EXISTS specializations;

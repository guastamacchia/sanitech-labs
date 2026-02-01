-- V5__doctor_single_department_specialization.sql
-- Converte le relazioni dei medici in singolo reparto/specializzazione.

ALTER TABLE doctors
    ADD COLUMN IF NOT EXISTS department_id BIGINT,
    ADD COLUMN IF NOT EXISTS specialization_id BIGINT;

-- Popola i riferimenti scegliendo il primo reparto/specializzazione associato.
UPDATE doctors d
SET department_id = dd.department_id
FROM (
    SELECT doctor_id, MIN(department_id) AS department_id
    FROM doctor_departments
    GROUP BY doctor_id
) dd
WHERE d.id = dd.doctor_id;

UPDATE doctors d
SET specialization_id = ds.specialization_id
FROM (
    SELECT doctor_id, MIN(specialization_id) AS specialization_id
    FROM doctor_specializations
    GROUP BY doctor_id
) ds
WHERE d.id = ds.doctor_id;

ALTER TABLE doctors
    ALTER COLUMN department_id SET NOT NULL,
    ALTER COLUMN specialization_id SET NOT NULL;

ALTER TABLE doctors
    ADD CONSTRAINT fk_doctors_department
        FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_doctors_specialization
        FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE RESTRICT;

DROP TABLE IF EXISTS doctor_departments;
DROP TABLE IF EXISTS doctor_specializations;

CREATE INDEX IF NOT EXISTS idx_doctors_department_id ON doctors(department_id);
CREATE INDEX IF NOT EXISTS idx_doctors_specialization_id ON doctors(specialization_id);

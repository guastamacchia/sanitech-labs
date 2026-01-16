-- =====================================================================
-- V1 - Schema base (Admissions)
-- =====================================================================

CREATE TABLE IF NOT EXISTS department_capacity (
  dept_code   VARCHAR(80) PRIMARY KEY,
  total_beds  INTEGER     NOT NULL,
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS admissions (
  id                 BIGSERIAL PRIMARY KEY,
  patient_id          BIGINT      NOT NULL,
  department_code     VARCHAR(80) NOT NULL REFERENCES department_capacity(dept_code),
  admission_type      VARCHAR(32) NOT NULL,
  status              VARCHAR(32) NOT NULL,
  admitted_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  discharged_at       TIMESTAMPTZ,
  notes               VARCHAR(500),
  attending_doctor_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_admissions_dept_status ON admissions(department_code, status);
CREATE INDEX IF NOT EXISTS idx_admissions_patient     ON admissions(patient_id);

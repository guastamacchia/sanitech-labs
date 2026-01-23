-- =====================================================================
-- V2 - Seed dati di esempio (solo per ambienti di sviluppo/demo)
-- =====================================================================

INSERT INTO department_capacity(dept_code, total_beds)
VALUES
  ('HEART', 10),
  ('METAB', 8),
  ('RESP', 6)
ON CONFLICT (dept_code) DO NOTHING;

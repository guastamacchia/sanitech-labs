-- V12__remove_mock_department_capacity.sql
-- Rimuove i codici reparto mock inseriti da V9 (PNEUMO, CARD, NEURO, ORTO, DERM)
-- Il frontend non utilizza più dati mock, quindi questi record sono dati sporchi

DELETE FROM department_capacity WHERE dept_code IN ('PNEUMO', 'CARD', 'NEURO', 'ORTO', 'DERM');

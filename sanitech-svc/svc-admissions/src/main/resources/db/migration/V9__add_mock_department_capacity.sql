-- V9__add_mock_department_capacity.sql
-- Aggiunge capacità per i codici reparto mock usati dal frontend quando l'API fallisce
-- Questo garantisce che l'indicatore posti letto funzioni sia con dati reali che mock

-- Codici brevi usati dal frontend mock (resource-page.state.ts getMockDepartments)
INSERT INTO department_capacity (dept_code, total_beds, updated_at) VALUES
    ('PNEUMO', 24, NOW()),
    ('CARD', 30, NOW()),
    ('NEURO', 25, NOW()),
    ('ORTO', 40, NOW()),
    ('DERM', 15, NOW())
ON CONFLICT (dept_code) DO UPDATE SET total_beds = EXCLUDED.total_beds, updated_at = NOW();

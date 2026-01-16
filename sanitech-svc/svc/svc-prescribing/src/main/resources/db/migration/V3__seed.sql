-- V3__seed.sql
-- Dati di esempio (solo per ambienti demo/dev).

INSERT INTO prescriptions (patient_id, doctor_id, department_code, status, notes, created_at, updated_at, issued_at)
VALUES (1001, 2001, 'CARDIOLOGY', 'ISSUED', 'Prescrizione di esempio generata da Flyway.', now(), now(), now())
ON CONFLICT DO NOTHING;

-- Assumendo che la prima prescrizione abbia id=1 in ambiente pulito.
INSERT INTO prescription_items (prescription_id, medication_code, medication_name, dosage, frequency, duration_days, instructions, sort_order)
VALUES
    (1, 'ATC:A10BA02', 'Metformina', '500mg', '2 volte al giorno', 30, 'Assumere dopo i pasti.', 1),
    (1, NULL, 'Vitamina D', '1000 UI', '1 volta al giorno', 60, NULL, 2)
ON CONFLICT DO NOTHING;

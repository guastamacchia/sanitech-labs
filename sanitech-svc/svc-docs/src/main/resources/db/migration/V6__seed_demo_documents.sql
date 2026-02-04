-- V6__seed_demo_documents.sql
-- Seed data per documenti clinici di sviluppo/demo
-- Allineato con svc-directory V11__seed_demo_data.sql e svc-consents V2__seed_demo_consents.sql
--
-- Pazienti con consenso DOCS verso Mario Rossi (doctor_id=1):
--   1 = Anna Conti
--   2 = Mario Rossi (paziente)
--   3 = Anna Bianchi
--   4 = Luigi Verde

-- =============================================================================
-- DOCUMENTI CLINICI
-- =============================================================================

-- Anna Conti (patient_id=1)
INSERT INTO documents (id, patient_id, uploaded_by, department_code, document_type, file_name, content_type, size_bytes, checksum_sha256, s3_key, description, created_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 1, 'mario.rossi.dr', 'CARD_CENTRAL', 'REFERTO', 'referto_ecg_20250115.pdf', 'application/pdf', 245000, 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', 'documents/1/referto_ecg_20250115.pdf', 'Referto ECG - ritmo sinusale, nei limiti della norma', NOW() - INTERVAL '10 days'),
    ('550e8400-e29b-41d4-a716-446655440002', 1, 'mario.rossi.dr', 'CARD_CENTRAL', 'ESAME', 'esami_sangue_20250110.pdf', 'application/pdf', 180000, 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', 'documents/1/esami_sangue_20250110.pdf', 'Esami del sangue completi - emocromo, glicemia, colesterolo', NOW() - INTERVAL '15 days'),
    ('550e8400-e29b-41d4-a716-446655440003', 1, 'marco.bianchi.dr', 'CARD_CENTRAL', 'LETTERA_DIMISSIONI', 'lettera_dimissioni_20250105.pdf', 'application/pdf', 320000, 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', 'documents/1/lettera_dimissioni_20250105.pdf', 'Lettera di dimissioni post ricovero', NOW() - INTERVAL '20 days')
ON CONFLICT DO NOTHING;

-- Mario Rossi paziente (patient_id=2)
INSERT INTO documents (id, patient_id, uploaded_by, department_code, document_type, file_name, content_type, size_bytes, checksum_sha256, s3_key, description, created_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440010', 2, 'mario.rossi.dr', 'CARD_CENTRAL', 'REFERTO', 'referto_visita_20250120.pdf', 'application/pdf', 150000, 'd4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5', 'documents/2/referto_visita_20250120.pdf', 'Referto visita cardiologica periodica', NOW() - INTERVAL '5 days')
ON CONFLICT DO NOTHING;

-- Anna Bianchi (patient_id=3)
INSERT INTO documents (id, patient_id, uploaded_by, department_code, document_type, file_name, content_type, size_bytes, checksum_sha256, s3_key, description, created_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440020', 3, 'mario.rossi.dr', 'CARD_CENTRAL', 'REFERTO', 'referto_ecocardiogramma_20250118.pdf', 'application/pdf', 280000, 'e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6', 'documents/3/referto_ecocardiogramma_20250118.pdf', 'Ecocardiogramma transtoracico - FE 55%', NOW() - INTERVAL '7 days'),
    ('550e8400-e29b-41d4-a716-446655440021', 3, 'giuseppe.russo.dr', 'NEUR_CENTRAL', 'REFERTO', 'referto_tac_20250112.pdf', 'application/pdf', 450000, 'f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1', 'documents/3/referto_tac_20250112.pdf', 'TAC cerebrale senza mdc - negativa', NOW() - INTERVAL '13 days')
ON CONFLICT DO NOTHING;

-- Luigi Verde (patient_id=4)
INSERT INTO documents (id, patient_id, uploaded_by, department_code, document_type, file_name, content_type, size_bytes, checksum_sha256, s3_key, description, created_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440030', 4, 'mario.rossi.dr', 'CARD_CENTRAL', 'ESAME', 'holter_20250122.pdf', 'application/pdf', 520000, 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', 'documents/4/holter_20250122.pdf', 'Holter ECG 24h - aritmie sporadiche non significative', NOW() - INTERVAL '3 days')
ON CONFLICT DO NOTHING;

-- Carla Neri (patient_id=5) - consenso verso Marco Bianchi
INSERT INTO documents (id, patient_id, uploaded_by, department_code, document_type, file_name, content_type, size_bytes, checksum_sha256, s3_key, description, created_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440040', 5, 'marco.bianchi.dr', 'CARD_CENTRAL', 'REFERTO', 'test_sforzo_20250119.pdf', 'application/pdf', 380000, 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', 'documents/5/test_sforzo_20250119.pdf', 'Test da sforzo - negativo per ischemia inducibile', NOW() - INTERVAL '6 days'),
    ('550e8400-e29b-41d4-a716-446655440041', 5, 'chiara.ferrari.dr', 'ONCO_NORD', 'REFERTO', 'pet_tac_20250108.pdf', 'application/pdf', 890000, 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', 'documents/5/pet_tac_20250108.pdf', 'PET-TAC total body - follow-up negativo', NOW() - INTERVAL '17 days')
ON CONFLICT DO NOTHING;

-- =============================================================================
-- V3__indexes.sql
-- Indici (performance) e lookup tipici
-- =============================================================================

-- Documenti: query tipiche per paziente e per reparto
CREATE INDEX IF NOT EXISTS idx_documents_patient ON documents(patient_id, created_at);
CREATE INDEX IF NOT EXISTS idx_documents_department ON documents(department_code, created_at);

-- Outbox: batch di eventi non pubblicati ordinati per tempo
CREATE INDEX IF NOT EXISTS idx_outbox_unpublished ON outbox_events(published, occurred_at);

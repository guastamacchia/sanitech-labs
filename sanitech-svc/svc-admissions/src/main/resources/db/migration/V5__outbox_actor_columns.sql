-- V5__outbox_actor_columns.sql
-- Aggiunge colonne per tracciare l'attore che ha generato l'evento.
-- Queste informazioni sono fondamentali per il sistema di audit.

ALTER TABLE outbox_events
    ADD COLUMN IF NOT EXISTS actor_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS actor_id VARCHAR(128),
    ADD COLUMN IF NOT EXISTS actor_name VARCHAR(256);

COMMENT ON COLUMN outbox_events.actor_type IS 'Tipo di attore (es. ADMIN, DOCTOR, PATIENT, SYSTEM)';
COMMENT ON COLUMN outbox_events.actor_id IS 'Identificativo univoco dell''attore (es. email, userId)';
COMMENT ON COLUMN outbox_events.actor_name IS 'Nome visualizzabile dell''attore';

-- Indice per query di audit filtrate per attore
CREATE INDEX IF NOT EXISTS idx_outbox_actor ON outbox_events (actor_type, actor_id);

-- V3__outbox.sql
-- Tabella Outbox per eventuali eventi prodotti dal servizio Audit (es. per pipeline analytics).
-- Pattern: scrittura evento outbox nella stessa transazione dell'insert audit.

CREATE TABLE IF NOT EXISTS outbox_events (
  id             BIGSERIAL PRIMARY KEY,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id   VARCHAR(64) NOT NULL,
  event_type     VARCHAR(64) NOT NULL,
  payload        JSONB       NOT NULL,
  occurred_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  published      BOOLEAN     NOT NULL DEFAULT FALSE,
  published_at   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished ON outbox_events(published, occurred_at);

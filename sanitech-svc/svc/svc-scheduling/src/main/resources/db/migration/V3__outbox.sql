-- =========================================================
-- V3__outbox.sql
-- Outbox pattern: tabella eventi per consegna affidabile a Kafka.
-- =========================================================

CREATE TABLE IF NOT EXISTS outbox_events (
  id BIGSERIAL PRIMARY KEY,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id   VARCHAR(64) NOT NULL,
  event_type     VARCHAR(64) NOT NULL,
  payload        JSONB        NOT NULL,
  occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  published      BOOLEAN      NOT NULL DEFAULT FALSE,
  published_at   TIMESTAMPTZ
);

-- Supporta la lettura efficiente degli eventi non pubblicati.
CREATE INDEX IF NOT EXISTS idx_outbox_unpublished ON outbox_events(published, occurred_at);

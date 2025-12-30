-- =============================================================================
-- V2__outbox.sql
-- Outbox pattern per consegna affidabile eventi via Kafka
-- =============================================================================

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

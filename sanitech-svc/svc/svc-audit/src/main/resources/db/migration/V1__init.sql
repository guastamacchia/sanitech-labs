-- V1__init.sql
-- Schema base del bounded context "Audit".
-- Registra eventi di sicurezza/operativi e accessi alle risorse.

CREATE TABLE IF NOT EXISTS audit_events (
  id           BIGSERIAL PRIMARY KEY,
  occurred_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  source       VARCHAR(64)  NOT NULL,
  actor_type   VARCHAR(32)  NOT NULL,
  actor_id     VARCHAR(128),
  action       VARCHAR(64)  NOT NULL,
  resource_type VARCHAR(64),
  resource_id   VARCHAR(128),
  outcome      VARCHAR(32)  NOT NULL,
  ip           VARCHAR(64),
  trace_id     VARCHAR(64),
  details      JSONB,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_occurred_at ON audit_events(occurred_at);
CREATE INDEX IF NOT EXISTS idx_audit_actor_id ON audit_events(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_resource ON audit_events(resource_type, resource_id);

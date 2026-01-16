-- ============================================
-- V1__init.sql
-- Inizializzazione schema svc-notifications
-- ============================================

CREATE TABLE IF NOT EXISTS notifications (
  id            BIGSERIAL PRIMARY KEY,
  recipient_type VARCHAR(16)  NOT NULL,
  recipient_id   VARCHAR(64)  NOT NULL,
  channel        VARCHAR(16)  NOT NULL,
  to_address     VARCHAR(200),
  subject        VARCHAR(200) NOT NULL,
  body           TEXT         NOT NULL,
  status         VARCHAR(16)  NOT NULL,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  sent_at        TIMESTAMPTZ,
  error_message  VARCHAR(400)
);

-- Indice per lista notifiche per destinatario (ordinamento per created_at)
CREATE INDEX IF NOT EXISTS idx_notifications_recipient
  ON notifications(recipient_type, recipient_id, created_at);

-- Indice per job di dispatch (status + created_at)
CREATE INDEX IF NOT EXISTS idx_notifications_status
  ON notifications(status, created_at);

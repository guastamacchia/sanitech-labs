CREATE TABLE IF NOT EXISTS payment_orders (
  id BIGSERIAL PRIMARY KEY,

  appointment_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,

  amount_cents BIGINT NOT NULL,
  currency VARCHAR(3) NOT NULL,

  method VARCHAR(32) NOT NULL,
  provider VARCHAR(32) NOT NULL,
  provider_reference VARCHAR(128),

  status VARCHAR(32) NOT NULL,
  description VARCHAR(255),

  idempotency_key VARCHAR(64),

  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(128) NOT NULL
);

-- Un ordine non deve essere duplicato se arriva lo stesso idempotency key.
CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_idempotency ON payment_orders (idempotency_key) WHERE idempotency_key IS NOT NULL;

-- Se arriva un update da provider via webhook, identifichiamo l'ordine su provider + provider_reference.
CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_provider_ref ON payment_orders (provider, provider_reference)
  WHERE provider_reference IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_payment_patient_created ON payment_orders (patient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_payment_appointment ON payment_orders (appointment_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment_orders (status);

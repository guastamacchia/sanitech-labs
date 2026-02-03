-- Tabella per le prestazioni sanitarie erogate (visite mediche e ricoveri)
-- Le prestazioni vengono registrate automaticamente quando:
-- - Una televisita viene completata (ENDED)
-- - Un paziente viene dimesso da un ricovero (DISCHARGED)

CREATE TABLE IF NOT EXISTS services_performed (
    id BIGSERIAL PRIMARY KEY,

    -- Tipo di prestazione (MEDICAL_VISIT, HOSPITALIZATION)
    service_type VARCHAR(32) NOT NULL,

    -- Sorgente della prestazione (TELEVISIT, ADMISSION)
    source_type VARCHAR(32) NOT NULL,
    source_id BIGINT NOT NULL,

    -- Dati paziente
    patient_id BIGINT NOT NULL,
    patient_subject VARCHAR(128),
    patient_name VARCHAR(255),
    patient_email VARCHAR(255),

    -- Reparto
    department_code VARCHAR(80),

    -- Dettagli prestazione
    description VARCHAR(500),
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',

    -- Stato pagamento (PENDING, PAID, FREE, CANCELLED)
    status VARCHAR(32) NOT NULL,

    -- Date
    performed_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    days_count INTEGER,

    -- Solleciti
    reminder_count INTEGER NOT NULL DEFAULT 0,
    last_reminder_at TIMESTAMPTZ,

    -- Note (es. motivo gratuità)
    notes VARCHAR(500),

    -- Audit
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(128) NOT NULL
);

-- Indice per ricerca per paziente ordinata per data
CREATE INDEX IF NOT EXISTS idx_sp_patient_created ON services_performed (patient_id, created_at DESC);

-- Indice per filtro per stato
CREATE INDEX IF NOT EXISTS idx_sp_status ON services_performed (status);

-- Indice per trovare prestazione da sorgente (evita duplicati)
CREATE UNIQUE INDEX IF NOT EXISTS uk_sp_source ON services_performed (source_type, source_id);

-- Commenti
COMMENT ON TABLE services_performed IS 'Prestazioni sanitarie erogate (visite mediche e ricoveri)';
COMMENT ON COLUMN services_performed.service_type IS 'Tipo: MEDICAL_VISIT (100 EUR) o HOSPITALIZATION (20 EUR/giorno)';
COMMENT ON COLUMN services_performed.source_type IS 'Sorgente: TELEVISIT o ADMISSION';
COMMENT ON COLUMN services_performed.amount_cents IS 'Importo in centesimi. Default: visita=10000, ricovero=2000*giorni';
COMMENT ON COLUMN services_performed.status IS 'Stato: PENDING (da pagare), PAID, FREE (gratuito), CANCELLED';

-- =============================================================================
-- V13: Preferenze notifica per pazienti
-- =============================================================================
-- Tabella per salvare le preferenze di notifica dei pazienti.
-- Ogni paziente ha una singola riga con le sue preferenze (email/SMS per ogni categoria).
-- =============================================================================

CREATE TABLE notification_preferences (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT NOT NULL UNIQUE,

    -- Promemoria appuntamenti
    email_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    sms_reminders   BOOLEAN NOT NULL DEFAULT FALSE,

    -- Nuovi documenti clinici
    email_documents BOOLEAN NOT NULL DEFAULT TRUE,
    sms_documents   BOOLEAN NOT NULL DEFAULT FALSE,

    -- Pagamenti e fatture
    email_payments  BOOLEAN NOT NULL DEFAULT TRUE,
    sms_payments    BOOLEAN NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notification_preferences_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Indice per lookup rapido per patient_id
CREATE INDEX idx_notification_preferences_patient_id ON notification_preferences(patient_id);

-- Commenti per documentazione
COMMENT ON TABLE notification_preferences IS 'Preferenze di notifica dei pazienti (email/SMS per ogni categoria)';
COMMENT ON COLUMN notification_preferences.email_reminders IS 'Ricevi promemoria appuntamenti via email';
COMMENT ON COLUMN notification_preferences.sms_reminders IS 'Ricevi promemoria appuntamenti via SMS';
COMMENT ON COLUMN notification_preferences.email_documents IS 'Ricevi notifica nuovi documenti clinici via email';
COMMENT ON COLUMN notification_preferences.sms_documents IS 'Ricevi notifica nuovi documenti clinici via SMS';
COMMENT ON COLUMN notification_preferences.email_payments IS 'Ricevi notifica pagamenti e fatture via email';
COMMENT ON COLUMN notification_preferences.sms_payments IS 'Ricevi notifica pagamenti e fatture via SMS';

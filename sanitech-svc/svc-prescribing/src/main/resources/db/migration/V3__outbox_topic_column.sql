-- V3__outbox_topic_column.sql
-- Aggiunge colonna topic per routing eventi su code Kafka specifiche.

ALTER TABLE outbox_events
    ADD COLUMN IF NOT EXISTS topic VARCHAR(128);

COMMENT ON COLUMN outbox_events.topic IS 'Topic Kafka di destinazione. Se NULL, usa il default configurato.';

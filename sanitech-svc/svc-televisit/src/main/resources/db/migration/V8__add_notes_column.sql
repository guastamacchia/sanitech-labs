-- V8: Aggiunge colonna 'notes' alla tabella televisit_sessions.
-- Consente ai medici di salvare note cliniche associate alla visita.
ALTER TABLE televisit_sessions
    ADD COLUMN notes TEXT;

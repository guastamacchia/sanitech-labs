-- V7__remove_seed_data.sql
-- Rimuove i dati seed demo inseriti da V6

-- Elimina tutte le sessioni televisita demo
DELETE FROM televisit_sessions WHERE id IN (
    1001, 1002, 1010, 1020, 1021, 1030, 1040, 1041,
    2001, 2002, 2003, 2004
);

-- Reset sequence a 1 se la tabella è vuota
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM televisit_sessions) THEN
        PERFORM setval('televisit_sessions_id_seq', 1, false);
    END IF;
END $$;

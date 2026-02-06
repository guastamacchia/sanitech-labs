-- Aggiunge lo stato COMPLETED e la colonna completed_at per le visite completate.
ALTER TABLE appointments ADD COLUMN completed_at TIMESTAMPTZ;

-- ============================================
-- V2__seed.sql
-- Dati di esempio (solo dev/test)
-- ============================================

INSERT INTO notifications(recipient_type, recipient_id, channel, to_address, subject, body, status)
VALUES
  ('ADMIN',  'admin-1',  'IN_APP', NULL, 'Benvenuto', 'Notifica di esempio IN_APP', 'SENT'),
  ('DOCTOR', 'doc-42',   'IN_APP', NULL, 'Reminder',  'Hai nuove richieste in attesa.', 'SENT');

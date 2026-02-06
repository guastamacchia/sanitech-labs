-- V9__fix_enum_values_seed_data.sql
-- Corregge i valori enum errati inseriti dalla migrazione V7:
--   - mode 'REMOTE' → 'TELEVISIT' (allineato a VisitMode.java)
--   - status 'CONFIRMED' → 'BOOKED' (allineato a AppointmentStatus.java)

UPDATE slots SET mode = 'TELEVISIT' WHERE mode = 'REMOTE';
UPDATE appointments SET mode = 'TELEVISIT' WHERE mode = 'REMOTE';
UPDATE appointments SET status = 'BOOKED' WHERE status = 'CONFIRMED';

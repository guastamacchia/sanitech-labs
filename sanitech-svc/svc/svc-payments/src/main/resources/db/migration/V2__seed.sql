INSERT INTO payment_orders(appointment_id, patient_id, amount_cents, currency, method, provider, provider_reference, status, description, idempotency_key, created_by)
VALUES
  (1001, 200, 4500, 'EUR', 'BANK_TRANSFER', 'MANUAL', NULL, 'CREATED', 'Visita di controllo', 'seed-1001', 'seed')
ON CONFLICT DO NOTHING;

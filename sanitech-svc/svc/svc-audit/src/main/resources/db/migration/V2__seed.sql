-- V2__seed.sql
-- Seed minimo (solo sviluppo).

INSERT INTO audit_events(source, actor_type, actor_id, action, resource_type, resource_id, outcome, ip, trace_id, details)
VALUES
  ('bootstrap', 'SYSTEM', 'system', 'STARTUP', 'SERVICE', 'svc-audit', 'SUCCESS', '127.0.0.1', null, '{"note":"seed"}'::jsonb)
ON CONFLICT DO NOTHING;

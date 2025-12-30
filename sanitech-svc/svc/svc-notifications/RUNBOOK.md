# RUNBOOK — svc-notifications

## Health & readiness
- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`

## Sintomi comuni

### Eventi outbox che non vengono pubblicati
**Verifiche**
1) Kafka raggiungibile (bootstrap servers).
2) Log applicativi del job outbox.
3) Stato record `outbox_events`:
```sql
SELECT published, count(*) FROM outbox_events GROUP BY published;
```

**Mitigazione**
- Se Kafka è down: ripristinare Kafka; il job riprenderà automaticamente.
- Se ci sono eccezioni persistenti: aumentare logging a DEBUG e verificare payload.

### Email non inviate
**Verifiche**
- Config SMTP (`spring.mail.*`)
- MailHog in dev: `http://localhost:8025`

**Mitigazione**
- Se l'SMTP è temporaneamente indisponibile, gli invii rimangono in `PENDING` e verranno ritentati (Retry/backoff).
- Se il contenuto è invalido (es. email mancante), la notifica viene marcata `FAILED`.

## Operazioni
### Smoke test (manuale)
1) Crea una notifica (admin)
2) Verifica che sia `SENT` (o visibile su MailHog in dev)
3) Verifica che sia stato generato un evento outbox pubblicato su Kafka

## Query utili
```sql
-- Notifiche pending
SELECT id, recipient_type, recipient_id, channel, created_at
FROM notifications
WHERE status = 'PENDING'
ORDER BY created_at ASC;

-- Ultimi outbox non pubblicati
SELECT id, aggregate_type, aggregate_id, event_type, occurred_at
FROM outbox_events
WHERE published = false
ORDER BY occurred_at ASC
LIMIT 20;
```

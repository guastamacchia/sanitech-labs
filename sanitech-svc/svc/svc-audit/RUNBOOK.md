# RUNBOOK — svc-audit

## Panoramica

`svc-audit` centralizza la persistenza di eventi di audit.

Fonti tipiche:
1) **API**: `POST /api/audit/events` (server-to-server).
2) **Kafka ingestion**: consumo di topic configurati (`sanitech.audit.ingestion.topics`).

## Health checks

- Liveness: `GET /actuator/health/liveness`
- Readiness: `GET /actuator/health/readiness`
- Prometheus: `GET /actuator/prometheus`

Porta default: `8085`.

## Metriche chiave

- `audit.events.saved.count`
- `outbox.events.saved.count` (se usato outbox)
- `outbox.events.published.count` (se usato outbox)

## Failure modes comuni

### 1) Ingestion Kafka non produce record in `audit_events`
**Sintomi**
- non compaiono eventi con `source='kafka'`
- log con warning su consumer

**Azioni**
- verificare `KAFKA_BOOTSTRAP_SERVERS`
- verificare `AUDIT_INGESTION_TOPICS`
- verificare che `sanitech.audit.ingestion.enabled=true`
- controllare offsets e group-id (`spring.kafka.consumer.group-id`)

### 2) API audit risponde 403
**Cause frequenti**
- token privo dei permessi richiesti
  - `POST`: `ROLE_ADMIN` o `SCOPE_audit.write`
  - `GET`: `ROLE_ADMIN` o `SCOPE_audit.read`

## Query utili (Postgres)

Connessione: DB `sanitech_audit`.

### Ultimi eventi audit
```sql
SELECT id, occurred_at, source, actor_type, actor_id, action, resource_type, resource_id, outcome
FROM audit_events
ORDER BY occurred_at DESC
LIMIT 50;
```

### Eventi ingestiti da Kafka
```sql
SELECT *
FROM audit_events
WHERE source = 'kafka'
ORDER BY occurred_at DESC
LIMIT 50;
```

### Outbox non pubblicata (opzionale)
```sql
SELECT id, aggregate_type, aggregate_id, event_type, occurred_at
FROM outbox_events
WHERE published = false
ORDER BY occurred_at
LIMIT 100;
```

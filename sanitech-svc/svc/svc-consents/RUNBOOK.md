# RUNBOOK — svc-consents

## Panoramica

`svc-consents` gestisce il consenso del **PAZIENTE** verso un **MEDICO** per uno specifico `scope` (es. `RECORDS`).
I servizi clinici interrogano questo microservizio (endpoint `check`) prima di consentire l’accesso ai dati del paziente.

## Health checks

- Liveness: `GET /actuator/health/liveness`
- Readiness: `GET /actuator/health/readiness`
- Prometheus: `GET /actuator/prometheus`

Porta default: `8085`.

## Metriche chiave

- `outbox.events.saved.count` (tag: `aggregateType`, `eventType`)
- `outbox.events.published.count` (tag: `aggregateType`, `eventType`)

## Failure modes comuni

### 1) Gli eventi non arrivano su Kafka (`consents.events`)
**Sintomi**
- outbox cresce (righe con `published=false`)
- nessun consumo lato downstream

**Azioni**
- verificare connettività verso Kafka (`KAFKA_BOOTSTRAP_SERVERS`)
- controllare log: `Pubblicazione outbox fallita (...)`
- verificare configurazione topic e ACL

### 2) Endpoint `/api/consents/check` ritorna sempre `allowed=false`
**Cause frequenti**
- non esiste record di consenso
- consenso `REVOKED`
- consenso `GRANTED` ma scaduto (`expires_at` nel passato)
- `scope` richiesto non coincide

## Query utili (Postgres)

Connessione: DB `sanitech_consents`.

### Consensi attivi per paziente/medico
```sql
SELECT *
FROM consents
WHERE patient_id = 1
  AND doctor_id = 1
  AND scope = 'RECORDS'
  AND status = 'GRANTED'
  AND (expires_at IS NULL OR expires_at > NOW());
```

### Outbox non pubblicata
```sql
SELECT id, aggregate_type, aggregate_id, event_type, occurred_at
FROM outbox_events
WHERE published = false
ORDER BY occurred_at
LIMIT 100;
```

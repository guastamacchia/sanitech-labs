# RUNBOOK — svc-directory (Sanitech)

## Sintomi comuni

### 1) Il servizio non parte: errori Flyway
- Verificare la connessione al DB (`DB_URL/DB_USER/DB_PASSWORD`).
- Controllare che il database sia PostgreSQL 16+.
- Se il DB è già popolato con uno schema precedente, verificare lo stato `flyway_schema_history`.

### 2) Outbox non pubblica su Kafka
- Verificare `KAFKA_BOOTSTRAP_SERVERS`.
- Controllare i log del `OutboxKafkaPublisher` (retry/backoff).
- Verificare che la topic `directory.events` esista (o abilitare auto-create nel cluster dev).

### 3) Troppi 429 su `GET /api/doctors`
- Controllare configurazione `resilience4j.ratelimiter.instances.directoryApi`.
- In caso di necessità aumentare `limitForPeriod` o ridurre le chiamate client.

## Operazioni

### Smoke test manuale
1. `GET /actuator/health` → 200
2. `GET /api/doctors` con token valido → 200
3. `GET /swagger-ui/index.html` → 200

### Gestione outbox bloccata
- Gli eventi non pubblicati rimangono con `published=false`.
- Verificare:
  - Kafka raggiungibile,
  - eventuali errori di serializzazione,
  - tuning retry/backoff.

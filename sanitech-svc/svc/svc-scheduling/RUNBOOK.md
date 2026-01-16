# RUNBOOK — `svc-scheduling` (Sanitech)

## Sintomi comuni

### 1) Health DOWN
- Verifica Postgres e credenziali (`DB_URL/DB_USER/DB_PASSWORD`).
- Verifica Flyway (migrazioni `V1..V3`).

### 2) Eventi Outbox non pubblicati
- Controlla Kafka (`KAFKA_BOOTSTRAP_SERVERS`) e topic `scheduling.events`.
- Verifica log di `OutboxKafkaPublisher`.
- Query utile:
  ```sql
  SELECT count(*) FROM outbox_events WHERE published = false;
  ```

### 3) 429 Too Many Requests
- Il RateLimiter è applicato a `GET /api/slots`.
- Verifica configurazione `resilience4j.ratelimiter.instances.schedulingApi`.

## Smoke check manuale

- Health:
  - `curl -s http://localhost:8083/actuator/health | jq`
- OpenAPI:
  - `curl -s http://localhost:8083/v3/api-docs/scheduling | jq '.info.title'`

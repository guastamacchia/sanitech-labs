# RUNBOOK — svc-televisit (Sanitech)

## Sintomi frequenti

### 1) Token LiveKit non valido / 401 dal client
- Verificare:
  - `LIVEKIT_API_KEY` e `LIVEKIT_API_SECRET`
  - `LIVEKIT_URL` raggiungibile
  - clock skew (orologi non sincronizzati)

### 2) Eventi Outbox non pubblicati
- Controllare:
  - `/actuator/health`
  - log del publisher
  - Kafka raggiungibile (`KAFKA_BOOTSTRAP_SERVERS`)
- Query utili:
  - `SELECT * FROM outbox_events WHERE published = false ORDER BY occurred_at ASC;`

### 3) Rate-limit troppo aggressivo (HTTP 429)
- Rivedere `resilience4j.ratelimiter.instances.televisitApi.*` in `application.yml`.

## Smoke test rapido

- Health:
  - `curl -s http://localhost:8087/actuator/health | jq`
- OpenAPI:
  - `curl -s http://localhost:8087/v3/api-docs/televisit | jq '.info'`

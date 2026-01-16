# Sanitech — svc-consents

Microservizio backend **svc-consents** della piattaforma Sanitech.

## Scopo

Gestisce il **consenso del PAZIENTE verso un MEDICO** per permettere a servizi clinici (es. `svc-docs`, `svc-records`, ecc.)
di decidere se un medico può accedere ai dati del paziente.

### Policy importante

- In questo servizio **NON** si richiede consenso per visualizzare l’elenco medici.
- La verifica consenso avviene **nei servizi clinici**, quando un **DOCTOR** tenta di accedere ai dati del **PAZIENTE**.
- Questo microservizio espone un endpoint `check` per verificare se esiste un consenso **GRANTED** e non scaduto.

## Requisiti

- Java 21+
- Docker (per Postgres/Kafka in locale)

## Avvio rapido (locale)

1) Avvia servizio e dipendenze:

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

> Per sviluppare in locale con `./mvnw spring-boot:run`, ferma il container
> `svc-consents` o avvia solo i servizi `postgres`/`kafka` dal compose.

3) Swagger UI:

- `http://localhost:8085/swagger-ui/index.html`

## API principali

- `GET /api/consents/check?patientId=...&doctorId=...&scope=RECORDS`
- `GET /api/consents/me` (ROLE_PATIENT)
- `POST /api/consents/me` (ROLE_PATIENT)
- `DELETE /api/consents/me/{doctorId}/{scope}` (ROLE_PATIENT)

## Outbox + Kafka

- Gli eventi di dominio vengono salvati in tabella `outbox_events` nella stessa transazione delle operazioni.
- Un job schedulato li pubblica su topic Kafka: `consents.events`.
- Retry/backoff sono configurati via Resilience4j (`resilience4j.retry.instances.outboxPublish`).

## Build & Test

```bash
./mvnw -q test
```

## Docker

```bash
docker build -t sanitech/svc-consents:local .
docker run --rm -p 8085:8085 sanitech/svc-consents:local
```

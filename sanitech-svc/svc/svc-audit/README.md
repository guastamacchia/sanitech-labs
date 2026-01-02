# Sanitech — svc-audit

Microservizio backend **svc-audit** della piattaforma Sanitech.

## Scopo

Centralizza la registrazione di **eventi di audit** (azioni, accessi, operazioni amministrative) in un DB dedicato.

Può ricevere eventi in due modalità:
1) **API** — chiamata diretta `POST /api/audit/events` (tipicamente server-to-server).
2) **Kafka ingestion** — consumo di topic configurati e persistenza come audit (abilitabile via property).

## Avvio rapido (locale)

1) Avvia servizio e dipendenze:

```bash
docker compose -f docker/docker-compose.yml up -d --build
```

> Per sviluppare con `./mvnw spring-boot:run`, ferma il container `svc-audit`
> o avvia solo i servizi `postgres`/`kafka` dal compose.

Swagger UI:
- `http://localhost:8088/swagger-ui/index.html`

## Configurazione ingestion Kafka

In `application.yml`:

- `sanitech.audit.ingestion.enabled=true|false`
- `sanitech.audit.ingestion.topics=directory.events,consents.events` (comma-separated)

## API

- `POST /api/audit/events` — registra evento audit
- `GET /api/audit/events` — ricerca paginata
- `GET /api/audit/events/{id}` — dettaglio evento

## Outbox (opzionale)

Il servizio salva anche un record nella tabella `outbox_events` quando registra un audit via API,
per consentire pipeline esterne (analytics/SIEM) su Kafka topic `audit.events`.

## Build & Test

```bash
./mvnw -q test
```

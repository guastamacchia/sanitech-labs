# Runbook — Sanitech Labs

Questo runbook riassume le azioni operative principali per l’ambiente di sviluppo.

## Prerequisiti
- Docker / Docker Compose
- Java 21
- Node.js (solo se si modifica la toolchain frontend)

## Backend (cartella `sanitech-svc`)
- Avvio stack locale: `bash scripts/up.sh`
- Spegnimento: `bash scripts/down.sh` (usa `REMOVE_VOLUMES=true` per pulire i volumi)
- Log principali: `bash scripts/logs.sh`
- Stato servizi: `bash scripts/status.sh`
- Build/test aggregato: da `sanitech-svc/svc` eseguire `make build` o `make test` (usa `MODULES=...` per filtri)

## Frontend (cartella `sanitech-mfe`)
- Avvio micro-frontend statici: `bash scripts/up.sh` (oppure `docker compose -f infra/docker-compose.yml up -d --build`)
- URL: shell `:4200`, paziente `:4301`, medico `:4302`, admin `:4303`

## Postman
- Collezione root e environment: `sanitech-svc/postman/`

## Osservabilità & Auth
- Keycloak: `http://localhost:8081`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin, datasource già configurata)

# Sanitech Labs

Monorepo per la piattaforma Sanitech:

- **Backend** (`sanitech-svc`): microservizi Spring Boot, compose infra (Keycloak, Kafka, Postgres, MinIO, Prometheus, Grafana), script helper e Postman.
- **Frontend** (`sanitech-fe`): micro‑frontend statici Bootstrap (shell pubblica, aree Paziente/Medico/Admin) con compose dedicato.

## Avvio rapido

### Backend
```bash
cd sanitech-svc
bash scripts/up.sh            # avvia tutto lo stack
# logs:   bash scripts/logs.sh
# stop:   bash scripts/down.sh
```

### Frontend
```bash
cd sanitech-fe
docker compose -f infra/docker-compose.yml up -d --build
# shell:   http://localhost:4200
# paziente http://localhost:4301
# medico   http://localhost:4302
# admin    http://localhost:4303
```

## Postman
- Collezione ed environment: `sanitech-svc/postman/`

## Osservabilità & Auth
- Keycloak: `http://localhost:8081`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin)

## Documentazione operativa
- Runbook generale: `RUNBOOK.md`
- Dettagli backend: `sanitech-svc/svc/README.md` e `sanitech-svc/svc/RUNBOOK.md`
- Frontend: `sanitech-fe/README.md`

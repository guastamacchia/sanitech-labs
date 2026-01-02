# Runbook — Sanitech Frontend (sanitech-mfe)

## Prerequisiti
- Docker / Docker Compose
- (Node non richiesto per le UI statiche)

## Avvio micro-frontend
```bash
bash scripts/up.sh
# stop:   bash scripts/down.sh        # REMOVE_VOLUMES=true per pulire i volumi
# log:    bash scripts/logs.sh
# stato:  bash scripts/status.sh
```

URL:
- Shell pubblica: http://localhost:4200
- Paziente: http://localhost:4301
- Medico: http://localhost:4302
- Admin: http://localhost:4303

## Dockerfile
- Centralizzati in `infra/dockerfiles/` e referenziati da `infra/docker-compose.yml`.

## Layout
- `frontend/shell`, `frontend/mfe-patient`, `frontend/mfe-doctor`, `frontend/mfe-admin`: HTML/Bootstrap statici.
- `scripts/`: helper per up/down/logs/status.

## Note
- Presuppone backend su http://localhost:8080 e Keycloak su http://localhost:8081.

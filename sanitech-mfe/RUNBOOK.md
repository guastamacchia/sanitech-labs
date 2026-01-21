# Runbook — Sanitech Frontend (sanitech-mfe)

## Prerequisiti
- Node.js 18+
- (Opzionale) Docker / Docker Compose per i servizi backend

## Avvio SPA Angular
```bash
cd frontend/sanitech-spa
npm install
npm start
```

URL:
- SPA pubblica/privata: http://localhost:4200

## Dockerfile
- I Dockerfile legacy dei micro-frontend sono in `infra/dockerfiles/`.
- La SPA Angular può essere containerizzata separatamente (es. con Nginx) se richiesto.

## Layout
- `frontend/sanitech-spa`: SPA Angular (portale pubblico + area privata con ruoli).
- `frontend/shell`, `frontend/mfe-patient`, `frontend/mfe-doctor`, `frontend/mfe-admin`: HTML/Bootstrap statici legacy.
- `scripts/`: helper per up/down/logs/status.

## Note
- Presuppone backend su http://localhost:8080 e Keycloak su http://localhost:8081.

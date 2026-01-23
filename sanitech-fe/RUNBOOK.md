# Runbook — Sanitech Frontend (sanitech-fe)

## Prerequisiti
- Node.js 18+
- (Opzionale) Docker / Docker Compose per i servizi backend

## Avvio SPA Angular
```bash
cd spa
npm install
npm start
```

URL:
- SPA pubblica/privata: http://localhost:4200

## Dockerfile
- I Dockerfile legacy dei micro-frontend sono in `../infra/fe/dockerfiles/`.
- La SPA Angular può essere containerizzata separatamente (es. con Nginx) se richiesto.

## Layout
- `spa`: SPA Angular (portale pubblico + area privata con ruoli).
- `../infra/fe/dockerfiles/`: Dockerfile legacy dei micro-frontend statici rimossi.
- `scripts/frontend/`: helper per up/down/logs/status.

## Note
- Presuppone backend su http://localhost:8080 e Keycloak su http://localhost:8081.

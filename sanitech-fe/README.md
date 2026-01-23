# Sanitech — Frontend

Questa repo contiene la nuova Single Page Application (Angular) che sostituisce i micro‑frontend statici (Bootstrap) per shell, paziente, medico e admin.

Nuova SPA:
- `spa`

Legacy (deprecated):
- Dockerfile legacy in `../infra/fe/dockerfiles/` (asset statici rimossi).

## Avvio rapido (presuppone backend su `http://localhost:8080`)

```bash
cd spa
npm install
npm start
# SPA: http://localhost:4200
```

Altri comandi utili:
- Stop: `bash scripts/frontend/down.sh` (usa `REMOVE_VOLUMES=true` per eliminare i volumi)
- Log: `bash scripts/frontend/logs.sh`
- Stato: `bash scripts/frontend/status.sh`

## Dockerfile
I Dockerfile legacy dei micro-frontend restano centralizzati in `../infra/fe/dockerfiles/`, ma la SPA Angular usa `spa` e può essere containerizzata con un Dockerfile dedicato se necessario.

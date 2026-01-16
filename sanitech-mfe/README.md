# Sanitech — Frontend

Questa repo contiene la nuova Single Page Application (Angular) che sostituisce i micro‑frontend statici (Bootstrap) per shell, paziente, medico e admin.

Nuova SPA:
- `frontend/sanitech-spa`

Legacy (deprecated):
- `frontend/shell`
- `frontend/mfe-patient`
- `frontend/mfe-doctor`
- `frontend/mfe-admin`

## Avvio rapido (presuppone backend su `http://localhost:8080`)

```bash
cd frontend/sanitech-spa
npm install
npm start
# SPA: http://localhost:4200
```

Altri comandi utili:
- Stop: `bash scripts/down.sh` (usa `REMOVE_VOLUMES=true` per eliminare i volumi)
- Log: `bash scripts/logs.sh`
- Stato: `bash scripts/status.sh`

## Dockerfile
I Dockerfile legacy dei micro-frontend restano centralizzati in `infra/dockerfiles/`, ma la SPA Angular usa `frontend/sanitech-spa` e può essere containerizzata con un Dockerfile dedicato se necessario.

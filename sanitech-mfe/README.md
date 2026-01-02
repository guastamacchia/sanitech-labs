# Sanitech — Frontend

Questa repo contiene i micro‑frontend statici (Bootstrap) per shell, paziente, medico e admin:

- `frontend/shell`
- `frontend/mfe-patient`
- `frontend/mfe-doctor`
- `frontend/mfe-admin`

## Avvio rapido (presuppone backend su `http://localhost:8080`)

```bash
bash scripts/up.sh   # oppure: docker compose -f infra/docker-compose.yml up -d --build
# Shell:    http://localhost:4200
# Paziente: http://localhost:4301
# Medico:   http://localhost:4302
# Admin:    http://localhost:4303
```

Altri comandi utili:
- Stop: `bash scripts/down.sh` (usa `REMOVE_VOLUMES=true` per eliminare i volumi)
- Log: `bash scripts/logs.sh`
- Stato: `bash scripts/status.sh`

## Dockerfile
Tutti i Dockerfile dei micro-frontend sono centralizzati in `infra/dockerfiles/`, ognuno nella propria sottocartella (es. `infra/dockerfiles/shell/Dockerfile`), e referenziati dal `docker-compose.yml`.

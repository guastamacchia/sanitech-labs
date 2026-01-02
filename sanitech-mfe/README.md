# Sanitech — Frontend

Questa repo contiene i micro‑frontend statici (Bootstrap) per shell, paziente, medico e admin:

- `frontend/shell`
- `frontend/mfe-patient`
- `frontend/mfe-doctor`
- `frontend/mfe-admin`

## Avvio rapido (presuppone backend su `http://localhost:8080`)

```bash
docker compose -f infra/docker-compose.frontend.yml up -d --build
# Shell:    http://localhost:4200
# Paziente: http://localhost:4301
# Medico:   http://localhost:4302
# Admin:    http://localhost:4303
```

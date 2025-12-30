# Clinica Guastamacchia — Frontend (FINAL · ULTIMATE)
Build time: **2025-11-10T11:31:33Z**

Contiene:
- **Bootstrap micro‑frontend** statici: `frontend/` (shell, paziente, medico, admin)
- **Angular 20 Module Federation workspace**: `frontend-angular/` (host *shell* + webpack MF)

## Avvio rapido (UI statiche) — presuppone backend su `http://localhost:8080`
```bash
docker compose -f infra/docker-compose.frontend.yml up -d --build
# Shell:   http://localhost:4200
# Paziente http://localhost:4301
# Medico   http://localhost:4302
# Admin    http://localhost:4303
```

# Sanitech — Frontend

Questa repo contiene la nuova Single Page Application (Angular) che sostituisce i micro‑frontend statici (Bootstrap) per shell, paziente, medico e admin.

Legacy (deprecated):
- Dockerfile legacy in `../infra/fe/dockerfiles/` (asset statici rimossi).

## Avvio rapido (presuppone backend su `http://localhost:8080`)

```bash
npm install
npm start
# SPA: http://localhost:4200
```

## Configurazioni ambiente

Le configurazioni sono in `src/environments/` e allineate ai file backend (`sanitech-svc/svc-gateway/infra/env/`).

- `environment.ts` → local
- `environment.staging.ts` → staging
- `environment.remote.ts` → remote
- `environment.prod.ts` → production

Per build specifici:

```bash
ng build --configuration=staging
ng build --configuration=remote
```

Altri comandi utili:
- Stop: `bash scripts/frontend/down.sh` (usa `REMOVE_VOLUMES=true` per eliminare i volumi)
- Log: `bash scripts/frontend/logs.sh`
- Stato: `bash scripts/frontend/status.sh`

## Dockerfile
I Dockerfile legacy dei micro-frontend restano centralizzati in `../infra/fe/dockerfiles/`, ma la SPA Angular vive direttamente in `sanitech-fe` e può essere containerizzata con un Dockerfile dedicato se necessario.

## Layout
- `src`: sorgenti della SPA Angular (portale pubblico + area privata con ruoli).
- `angular.json`, `package.json`, `tsconfig*.json`: configurazione toolchain SPA.
- `../infra/fe/dockerfiles/`: Dockerfile legacy dei micro-frontend statici rimossi.
- `scripts/frontend/`: helper per up/down/logs/status.

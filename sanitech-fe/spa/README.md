# Sanitech SPA (Angular)

SPA unificata per portale pubblico e area privata (pazienti, medici, amministratori) con autenticazione OAuth2/OpenID Connect via Keycloak.

## Avvio rapido

```bash
npm install
npm start
```

## Configurazioni ambiente

Le configurazioni sono in `src/environments/` e allineate ai file backend (`sanitech-svc/svc/svc-gateway/infra/env/`).

- `environment.ts` → local
- `environment.staging.ts` → staging
- `environment.remote.ts` → remote
- `environment.prod.ts` → production

Per build specifici:

```bash
ng build --configuration=staging
ng build --configuration=remote
```

# Sanitech Labs

Monorepo della piattaforma **Sanitech**.

- **Backend** (`sanitech-svc`): microservizi Spring Boot + script helper. Le configurazioni condivise sono in `.infra`, la compose in `.infra/docker-compose.yml`.
- **Frontend** (`sanitech-fe`): SPA Angular.

## Prerequisiti
- Docker / Docker Compose (Buildx opzionale; utile per build multi-arch)
- Java 21
- Node.js (solo se si modifica la toolchain frontend)

## Avvio rapido

### Backend (stack completo)
```bash
bash .script/backend/up.sh            # avvia tutto lo stack
# logs:   bash .script/backend/logs.sh
# stop:   bash .script/backend/down.sh
# stato:  bash .script/backend/status.sh
```

Per usare un file env diverso da quello locale:
```bash
ENV=staging make compose-up-infra
```

### Frontend
```bash
bash .script/frontend/up.sh
# SPA: http://localhost:4200
```

## Struttura repository
- `.infra/`: configurazioni centralizzate e docker compose per l'infrastruttura backend.
- `sanitech-fe`: SPA Angular e script di build/avvio.
- `sanitech-svc`: microservizi backend, Makefile aggregatore e script di stack.
- `.script/`: script centralizzati per backend, frontend e smoke test dei servizi.

## Backend: servizi e porte

| Servizio | Porta | Descrizione sintetica |
| --- | --- | --- |
| `svc-gateway` | 8080 | API Gateway (routing, JWT, OpenAPI aggregata) |
| `svc-directory` | 8082 | Anagrafiche medici/pazienti/reparti/specializzazioni |
| `svc-scheduling` | 8083 | Gestione slot e appuntamenti |
| `svc-admissions` | 8084 | Ricoveri/ammissioni + outbox Kafka |
| `svc-consents` | 8085 | Consensi paziente/medico e endpoint `check` |
| `svc-docs` | 8086 | Documenti clinici + storage S3/MinIO |
| `svc-notifications` | 8087 | Notifiche e SMTP (MailHog in dev) |
| `svc-audit` | 8088 | Audit centralizzato (API + ingestion Kafka) |
| `svc-televisit` | 8089 | Sessioni di televisita (LiveKit) |
| `svc-payments` | 8090 | Pagamenti e webhook provider |
| `svc-prescribing` | 8091 | Prescrizioni + verifica consensi |

Servizi infrastrutturali (dev):
- Keycloak: `http://localhost:8081`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin)
- MinIO: `http://localhost:9000` (console `http://localhost:9001`)
- MailHog: `http://localhost:8025`
- LiveKit: `http://localhost:7880` (console `http://localhost:7881`)

## Backend: comandi aggregati (sanitech-svc)
Da `sanitech-svc`:

```bash
make build   # package completo con skip test
make test    # test su tutti i moduli (usa MODULES=svc-...)
```

Variabili utili:
- `MODULES=svc-directory,svc-consents`
- `PROFILE=local`
- `MAVEN_ARGS=-DskipTests=false`
- `MVN=mvn`
- `POM=pom.xml`

## Backend: note funzionali

### Consensi
- Il consenso **non** è richiesto per consultare l’elenco dei medici.
- La verifica consenso avviene **nei servizi clinici** (es. `svc-docs`, `svc-prescribing`) quando un **DOCTOR** accede ai dati del **PATIENT**.
- `svc-consents` espone l’endpoint `check` per valutare un consenso **GRANTED** e non scaduto.

### Sicurezza (Keycloak OIDC)
- I servizi validano i JWT via **Spring Security Resource Server**.
- Ruoli da `realm_access.roles` → `ROLE_*`.
- Claim custom `dept` → authority `DEPT_*` per policy ABAC di reparto.

### Outbox & Kafka
I servizi di dominio producono eventi su tabella `outbox_events` nella stessa transazione e li pubblicano su Kafka con retry/backoff. I topic principali sono:
`directory.events`, `scheduling.events`, `admissions.events`, `consents.events`, `docs.events`, `notifications.events`, `audit.events`, `televisit.events`, `payments.events`, `prescribing.events`.

### API Gateway
- Swagger UI: `/swagger-ui/index.html` oppure `/swagger.html` (locked‑down).
- OpenAPI aggregata: `/openapi/merged`.
- OpenAPI per servizio: `/openapi/{service}`.
- CORS: impostare `SANITECH_CORS_ALLOWED_ORIGINS` con una lista di origin separati da virgola (es. `http://localhost:4200,http://localhost:8080`).

## Frontend
La nuova SPA Angular sostituisce i micro‑frontend statici (Bootstrap) per shell, paziente, medico e admin.

Avvio rapido (presuppone backend su `http://localhost:8080`):
```bash
bash .script/frontend/up.sh
# SPA: http://localhost:4200
```

Configurazioni ambiente (`sanitech-fe/src/environments/`):
- `environment.ts` → local
- `environment.staging.ts` → staging
- `environment.prod.ts` → production

Build specifici:
```bash
ng build --configuration=staging
```

Script utili:
- Stop: `bash .script/frontend/down.sh` (usa `REMOVE_VOLUMES=true` per eliminare i volumi)
- Log: `bash .script/frontend/logs.sh`
- Stato: `bash .script/frontend/status.sh`

Stack Docker:
- Compose: `.infra/fe/docker-compose.yml`
- Build multi-stage: `sanitech-fe/Dockerfile` (dist `sanitech-spa`, Nginx con fallback SPA)

Layout:
- `sanitech-fe/src`: sorgenti SPA Angular (portale pubblico + area privata con ruoli).
- `.script/frontend/`: helper per up/down/logs/status.

## Documentazione operativa
- Runbook generale: `RUNBOOK.md`
- Manifest repository: `MANIFEST.md`

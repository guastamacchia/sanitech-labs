# Sanitech — svc-notifications

Microservizio **Notifications** della piattaforma **Sanitech**: gestione notifiche in-app/email,
invio SMTP, outbox → Kafka (`notifications.events`) e policy ABAC per reparto (`DEPT_*`).

## Stack tecnico

- Java 21 (LTS)
- Spring Boot 3.3.x
- Spring Security Resource Server (JWT) per Keycloak OIDC
- PostgreSQL 16+, Flyway
- Kafka + Outbox Pattern
- MailHog (dev) come SMTP
- Resilience4j: CircuitBreaker, Retry, RateLimiter, Bulkhead
- Springdoc OpenAPI (Swagger UI)
- Actuator + Prometheus/Grafana

## Come eseguire (3 comandi)

### 1) Avvio infrastruttura (Postgres + Kafka + Keycloak + MailHog + Prometheus + Grafana)
```bash
make compose-up
# (il target esegue anche mvn package per generare il JAR prima della build dell'immagine)
# oppure, se preferisci solo l'infrastruttura:
# make compose-up-infra
# (espone Postgres/Kafka/MailHog anche verso l'host via docker-compose.infra-ports.yml)
```
- Il servizio Keycloak viene buildato localmente (Dockerfile in `infra/keycloak`) includendo il realm `sanitech`.
- MailHog espone SMTP `1025` e UI `http://localhost:8025`.
- Prometheus e Grafana vengono buildati localmente con la configurazione già inclusa.

### 2) Build + test
```bash
./mvnw -q test
```

### 3) Run
```bash
./mvnw -q spring-boot:run
```

Endpoint utili:
- svc-notifications: `http://localhost:8087`
- Swagger UI: `http://localhost:8087/swagger-ui/index.html`
- Health: `http://localhost:8087/actuator/health`
- Kafka (listener EXTERNAL): `localhost:29092`
- Postgres (via `compose-up-infra`): `localhost:5436`
- MailHog UI: `http://localhost:8025`
- Keycloak: `http://localhost:8081`
- Grafana: `http://localhost:3000`
- I file env sono in `infra/env/env.<env>` (selezionabili via `PROFILE=<env>` nei target Makefile, default `remote`).

## Variabili d'ambiente principali

- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_ADVERTISED_HOST`
- `OAUTH2_ISSUER_URI`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM`
- `CORS_ALLOWED_ORIGINS`

## Outbox pattern

Ogni notifica generata/aggiornata produce un evento in tabella `outbox_events`
nella **stessa transazione** dell'operazione applicativa. Un job schedulato pubblica su Kafka
`notifications.events` con retry/backoff e marca gli eventi come pubblicati.

## Sicurezza (Keycloak JWT)

Il servizio è configurato come **Resource Server** JWT.

- `ROLE_ADMIN`: gestione completa
- `ROLE_DOCTOR`: accesso alle proprie notifiche (ABAC `DEPT_*`)

### Keycloak locale pronto all'uso
- `docker compose up keycloak svc-notifications` importa automaticamente il realm `sanitech` da `keycloak/sanitech-realm.json`.
- Client configurati:
  - `svc-notifications` (secret: `svc-notifications-secret`)
  - `svc-directory` (secret: `svc-directory-secret`)
- Utenti di test:
  - `admin` / `admin` con ruolo `ADMIN`
  - `doctor` / `doctor` con ruolo `DOCTOR` e claim `dept=CARDIO`

### SMTP (MailHog in dev)
- SMTP: `MAIL_HOST`/`MAIL_PORT` (default `localhost:1025`)
- UI: `http://localhost:8025` per verificare le email inviate.

### Smoke test locale
```bash
./scripts/smoke.sh
./scripts/rate-limit.sh
./scripts/bulkhead.sh
./scripts/loop.sh
```
- Gli script verificano health, metriche, rate limit e loop continuo sugli endpoint principali (`SERVICE_URL` di default `http://localhost:8087`).

### Postman
- Collezione: `postman/svc-notifications.postman_collection.json`
- Environment di esempio: `postman/svc-notifications.postman_environment.json`

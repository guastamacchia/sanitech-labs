# Sanitech — svc-scheduling

Microservizio **Scheduling** della piattaforma **Sanitech**: gestione slot/appuntamenti, outbox → Kafka
(`scheduling.events`) e policy ABAC per reparto (`DEPT_*`).

## Stack tecnico

- Java 21 (LTS)
- Spring Boot 3.3.x
- Spring Security Resource Server (JWT) per Keycloak OIDC
- PostgreSQL 16+, Flyway
- Kafka + Outbox Pattern
- Resilience4j: Retry, RateLimiter, Bulkhead
- Springdoc OpenAPI (Swagger UI)
- Actuator + Prometheus/Grafana

## Come eseguire (3 comandi)

### 1) Avvio infrastruttura (Postgres + Kafka + Keycloak + Prometheus + Grafana)
```bash
make compose-up
# (il target esegue anche mvn package per generare il JAR prima della build dell'immagine)
# oppure, se preferisci solo l'infrastruttura:
# make compose-up-infra
# (espone Postgres/Kafka anche verso l'host via docker-compose.infra-ports.yml)
```
- Il servizio Keycloak viene buildato localmente (Dockerfile in `infra/keycloak`) includendo il realm `sanitech`.
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
- svc-scheduling: `http://localhost:8083`
- Swagger UI: `http://localhost:8083/swagger-ui/index.html`
- Health: `http://localhost:8083/actuator/health`
- Kafka (listener EXTERNAL): `localhost:29092`
- Postgres (via `compose-up-infra`): `localhost:5433`
- Keycloak: `http://localhost:8081`
- Grafana: `http://localhost:3000`
- I file env sono in `infra/env/env.<env>` (selezionabili via `PROFILE=<env>` nei target Makefile, default `remote`).

## Variabili d'ambiente principali

- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_ADVERTISED_HOST`
- `OAUTH2_ISSUER_URI`
- `CORS_ALLOWED_ORIGINS`

## Outbox pattern

Ogni slot/appuntamento creato o modificato produce un evento in `outbox_events`
nella **stessa transazione** dell'operazione di dominio. Un job schedulato pubblica su Kafka
`scheduling.events` con retry/backoff e marca gli eventi come pubblicati.

## Sicurezza (Keycloak JWT)

Il servizio è configurato come **Resource Server** JWT.

- `ROLE_ADMIN`: gestione completa delle API
- `ROLE_DOCTOR`: accesso a dati coerenti col reparto (authority `DEPT_<CODE>`)
- `ROLE_PATIENT`: accesso ai propri appuntamenti

### Keycloak locale pronto all'uso
- `docker compose up keycloak svc-scheduling` importa automaticamente il realm `sanitech` da `keycloak/sanitech-realm.json`.
- Client configurati:
  - `svc-scheduling` (secret: `svc-scheduling-secret`)
  - `svc-directory` (secret: `svc-directory-secret`)
- Utenti di test:
  - `admin` / `admin` con ruolo `ADMIN`
  - `doctor` / `doctor` con ruolo `DOCTOR` e claim `dept=CARDIO`

### Smoke test locale
```bash
./scripts/smoke.sh
./scripts/rate-limit.sh
./scripts/bulkhead.sh
./scripts/loop.sh
```
- Gli script verificano health, metriche, rate limit e loop continuo sugli endpoint principali (`SERVICE_URL` di default `http://localhost:8083`).

### Postman
- Collezione: `postman/svc-scheduling.postman_collection.json`
- Environment di esempio: `postman/svc-scheduling.postman_environment.json`

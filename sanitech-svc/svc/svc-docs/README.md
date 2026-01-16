# Sanitech — svc-docs

Microservizio **Documenti** della piattaforma **Sanitech**: metadati su Postgres, binari su MinIO/S3,
eventi affidabili via outbox → Kafka (`docs.events`) e policy ABAC per reparto (`DEPT_*`).

## Stack tecnico

- Java 21 (LTS)
- Spring Boot 3.3.x
- Spring Security Resource Server (JWT) per Keycloak OIDC
- PostgreSQL 16+, Flyway
- Kafka + Outbox Pattern
- MinIO (S3 compatibile) per i binari
- Resilience4j: CircuitBreaker, Retry, RateLimiter, Bulkhead
- Springdoc OpenAPI (Swagger UI)
- Actuator + Prometheus/Grafana

## Come eseguire (3 comandi)

### 1) Avvio infrastruttura (Postgres + Kafka + Keycloak + MinIO + Prometheus + Grafana)
```bash
make compose-up
# (il target esegue anche mvn package per generare il JAR prima della build dell'immagine)
# oppure, se si preferisce solo l'infrastruttura:
# make compose-up-infra
# (espone Postgres/Kafka/MinIO anche verso l'host via docker-compose.infra-ports.yml)
```
- Il servizio Keycloak viene buildato localmente (Dockerfile in `infra/keycloak`) includendo il realm `sanitech`.
- MinIO parte con il bucket `S3_BUCKET` già creato (`minio-mc`), console su `http://localhost:9007`.
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
- svc-docs: `http://localhost:8086`
- Swagger UI: `http://localhost:8086/swagger-ui/index.html`
- Health: `http://localhost:8086/actuator/health`
- Kafka (listener EXTERNAL): `localhost:29092`
- Postgres (via `compose-up-infra`): `localhost:5436`
- MinIO API: `http://localhost:9006` (console: `http://localhost:9007`)
- Keycloak: `http://localhost:8081`
- Grafana: `http://localhost:3000`
- I file env sono in `infra/env/env.<env>` (selezionabili via `PROFILE=<env>` nei target Makefile, default `remote`).

## Variabili d'ambiente principali

- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_ADVERTISED_HOST`
- `OAUTH2_ISSUER_URI`
- `CONSENTS_BASE_URL`
- `S3_ENDPOINT`, `S3_REGION`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`
- `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`
- `CORS_ALLOWED_ORIGINS`

## Outbox pattern

Ogni documento salvato/aggiornato/archiviato produce un evento persistito in tabella `outbox_events`
nella **stessa transazione** dell'operazione di dominio. Un job schedulato pubblica su Kafka
`docs.events` con retry/backoff e marca gli eventi come pubblicati.

## Sicurezza (Keycloak JWT)

Il servizio è configurato come **Resource Server** JWT.

- `ROLE_ADMIN`: accesso completo
- `ROLE_DOCTOR`: list/download consentiti solo con consenso valido (`svc-consents`) e reparto coerente (authority `DEPT_<CODE>`)
- `ROLE_PATIENT`: può leggere solo i propri documenti (claim JWT `pid`)

### Keycloak locale pronto all'uso
- `docker compose up keycloak svc-docs` importa automaticamente il realm `sanitech` da `keycloak/sanitech-realm.json`.
- Client configurati:
  - `svc-docs` (secret: `svc-docs-secret`)
  - `svc-directory` (secret: `svc-directory-secret`)
- Utenti di test:
  - `admin` / `admin` con ruolo `ADMIN`
  - `doctor` / `doctor` con ruolo `DOCTOR` e claim `dept=CARDIO`

### Storage S3/MinIO
- Bucket predefinito configurabile via `S3_BUCKET` (default `sanitech-docs`), creato automaticamente da `minio-mc`.
- Credenziali e endpoint configurabili via env (`MINIO_ROOT_USER`/`MINIO_ROOT_PASSWORD`, `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_REGION`).

### Smoke test locale
```bash
./scripts/smoke.sh
./scripts/rate-limit.sh
./scripts/bulkhead.sh
./scripts/loop.sh
```
- Gli script verificano health, metriche, rate limit e loop continuo sugli endpoint principali (`SERVICE_URL` di default `http://localhost:8086`).

### Postman
- Collezione: `postman/svc-docs.postman_collection.json`
- Environment di esempio: `postman/svc-docs.postman_environment.json`

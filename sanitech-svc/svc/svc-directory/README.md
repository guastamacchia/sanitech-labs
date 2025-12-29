# Sanitech — svc-directory

Microservizio **Directory** della piattaforma **Sanitech**: gestione anagrafiche di **Medici**, **Pazienti**, **Reparti** e **Specializzazioni**.

## Stack tecnico

- Java 21 (LTS)
- Spring Boot 3.3.x
- Spring Security Resource Server (JWT) per Keycloak OIDC (senza adapter legacy)
- PostgreSQL 16+, Flyway
- Kafka + Outbox Pattern
- Resilience4j: RateLimiter, Bulkhead, Retry
- Springdoc OpenAPI (Swagger UI)
- Actuator + Prometheus

## Come eseguire (3 comandi)

### 1) Avvio infrastruttura (Postgres + Kafka + Keycloak + Prometheus + Grafana)
```bash
make compose-up
# (il target esegue anche mvn package per generare il JAR prima della build dell'immagine)
# oppure, se si preferisce solo l'infrastruttura:
# docker compose -f docker/docker-compose.yml up -d postgres kafka keycloak prometheus grafana
```
- Il servizio Keycloak viene buildato localmente (Dockerfile in `docker/Dockerfile.keycloak`) includendo il realm `sanitech` nel layer immagine e con health abilitato (`KEYCLOAK_HEALTH_ENABLED=true`), così l'import avviene anche con Docker Engine remoto (senza bind mount locale).
  Assicurati che il JAR sia presente in `target/` prima della build (il `make compose-up` lo genera automaticamente).
- Prometheus viene buildato localmente (`docker/Dockerfile.prometheus`) con la configurazione già inclusa, per evitare problemi di bind mount.
- Grafana (porta `3000`, credenziali predefinite `admin`/`admin`) parte con la datasource Prometheus già configurata (cartella `docker/grafana/provisioning`).

### 2) Build + test
```bash
./mvnw -q test
```

### 3) Run
```bash
./mvnw -q spring-boot:run
```

Swagger UI:
- `http://localhost:8082/swagger-ui/index.html`

Health:
- `http://localhost:8082/actuator/health`

## Variabili d'ambiente principali

- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `OAUTH2_JWK_SET_URI` (JWK Set URI del realm Keycloak)

## Outbox pattern

Le operazioni di dominio su **Doctor** e **Patient** producono eventi in tabella `outbox_events`
nella **stessa transazione** del salvataggio. Un job schedulato pubblica su Kafka `directory.events`
con retry/backoff e marca gli eventi come pubblicati.

Metriche:
- `outbox.events.saved.count`
- `outbox.events.published`

## Sicurezza (Keycloak JWT)

Il servizio è configurato come **Resource Server** JWT.
Il converter custom mappa:
- `realm_access.roles` → `ROLE_*`
- `scope` → `SCOPE_*`
- claim custom `dept` → `DEPT_*` (ABAC per reparto)

### Keycloak locale pronto all'uso
- `docker compose up keycloak svc-directory` importa automaticamente il realm `sanitech` da `keycloak/sanitech-realm.json`.
- Client configurato: `svc-directory` (secret: `svc-directory-secret`).
- Utenti di test:
  - `admin` / `admin` con ruolo `ADMIN`
  - `doctor` / `doctor` con ruolo `DOCTOR` e claim `dept=CARDIO`

### Smoke test locale
Con Keycloak e il servizio avviati:
```bash
./scripts/smoke.sh
```
- Verifica health Keycloak, health del servizio, token OIDC, RateLimiter (seconda chiamata → 429) e metriche Resilience4j (bulkhead configurato a 1 chiamata concorrente).

### Test Bulkhead (concurrency)
Con configurazione bulkhead locale (maxConcurrentCalls=1) e Keycloak/servizio avviati:
```bash
./scripts/bulkhead-test.sh
```
- Esegue due richieste concorrenti su `/api/admin/patients`: una deve andare a buon fine, la seconda deve essere rifiutata dal bulkhead.

### Loop di test continuo
Per eseguire chiamate ripetute su tutti gli endpoint principali (health, pubblici, admin):
```bash
./scripts/loop.sh
```
- Richiede le stesse credenziali/env di `smoke.sh`, ottiene un token una sola volta e ripete le chiamate all'infinito (intervallo configurabile).

### Postman
- Collezione: `postman/sanitech-directory.postman_collection.json`
- Environment di esempio: `postman/sanitech-directory.postman_environment.json`
- Esegui prima la richiesta "Obtain Token (Keycloak)" per popolare `access_token`, poi usa gli endpoint protetti.

## Note su visibilità pazienti per reparto

Gli endpoint di lettura pazienti (`GET /api/patients/**`) applicano un filtro ABAC:
un utente con profilo **DOCTOR** vede solo i pazienti associati ad almeno un reparto
presente nelle proprie authority `DEPT_*`.

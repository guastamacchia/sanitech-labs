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

### 1) Avvio infrastruttura (Postgres + Kafka + Keycloak)
```bash
docker compose -f docker-compose.yml up -d postgres kafka keycloak
```

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
- `docker compose up keycloak svc-directory` importa automaticamente il realm `sanitech` da `keycloak/realm-export/sanitech-realm.json`.
- Client configurato: `svc-directory` (secret: `svc-directory-secret`).
- Utenti di test:
  - `admin` / `admin` con ruolo `ADMIN`
  - `doctor` / `doctor` con ruolo `DOCTOR` e claim `dept=CARDIO`

### Smoke test locale
Con Keycloak e il servizio avviati:
```bash
./scripts/local-smoke.sh
```

## Note su visibilità pazienti per reparto

Gli endpoint di lettura pazienti (`GET /api/patients/**`) applicano un filtro ABAC:
un utente con profilo **DOCTOR** vede solo i pazienti associati ad almeno un reparto
presente nelle proprie authority `DEPT_*`.

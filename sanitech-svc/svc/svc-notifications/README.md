# Sanitech — svc-notifications

Microservizio **Notifications** della piattaforma Sanitech.

## Scopo
Gestisce:
- creazione e consultazione di notifiche (in-app / email);
- invio email via SMTP;
- pubblicazione eventi tramite **Outbox pattern** su Kafka (topic `notifications.events`).

## Stack
- Java 21, Spring Boot 3.5.x
- PostgreSQL 16+ + Flyway
- Spring Security Resource Server (JWT) con Keycloak OIDC
- Kafka (producer) per outbox
- Resilience4j (RateLimiter/Bulkhead/Retry)
- Actuator + Prometheus

## Avvio locale (dev)
1) Avvia servizio e dipendenze (Postgres, Kafka, MailHog):
```bash
docker compose -f infra/docker-compose.yml up -d --build
```

> Per sviluppo con `./mvnw spring-boot:run`, ferma il container `svc-notifications`
> o avvia solo i servizi `postgres`/`redpanda`/`mailhog` dal compose.

2) URL utili:
- Swagger UI: `http://localhost:8087/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8087/v3/api-docs/notifications`
- Health: `http://localhost:8087/actuator/health`
- MailHog UI: `http://localhost:8025`

## Configurazione (principali variabili)
- DB:
  - `DB_URL` (default: `jdbc:postgresql://localhost:5436/sanitech_notifications`)
  - `DB_USER`, `DB_PASSWORD`
- Keycloak (issuer):
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- Kafka:
  - `KAFKA_BOOTSTRAP_SERVERS` (default `localhost:9092`)
- SMTP:
  - `spring.mail.host` (default `localhost`)
  - `spring.mail.port` (default `1025`)
  - `sanitech.notifications.mail.from`

## Sicurezza
Il servizio è protetto da JWT (Keycloak Resource Server).  
Il converter mappa:
- `realm_access.roles` → `ROLE_*`
- `scope` → `SCOPE_*`
- claim `dept` → `DEPT_*` (ABAC; non essenziale per questo servizio ma mantenuto per coerenza architetturale)

Endpoint pubblici:
- `/v3/api-docs/**`, `/swagger-ui/**`, `/actuator/health/**`

Endpoint amministrativi:
- `/api/admin/**` richiede `ROLE_ADMIN`.

## Outbox pattern (eventi)
Ogni operazione rilevante genera un record su `outbox_events` nella stessa transazione dell'operazione applicativa.
Un job schedulato pubblica gli eventi su Kafka e marca il record come pubblicato.

Topic: `notifications.events`

## Esecuzione test
```bash
./mvnw test
```

## Docker build
```bash
docker build -t svc-notifications:local .
```

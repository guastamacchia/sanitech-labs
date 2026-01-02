# svc-payments (Sanitech)

Microservizio **Sanitech** per la gestione dei **pagamenti** (ordini di pagamento, aggiornamenti stato, outbox events).
Progettato per essere **build/test/deploy-ready** con:
- PostgreSQL + Flyway
- Keycloak OIDC (Resource Server JWT)
- Outbox pattern + Kafka (retry/backoff)
- Resilience4j (RateLimiter/Bulkhead + Retry)
- RFC 7807 per error handling
- Actuator + Micrometer

## Modello dominio (sintesi)
- **PaymentOrder**: rappresenta un ordine di pagamento legato a un `appointmentId` e a un `patientId`.
- Stati: `CREATED`, `CAPTURED`, `FAILED`, `CANCELLED`, `REFUNDED`.

> Nota: l'integrazione con provider di pagamento avviene tramite endpoint **webhook** protetto da secret
> oppure tramite aggiornamenti admin. Non sono presenti stub: tutte le rotte sono operative.

---

## Avvio rapido (3 comandi)
1) Avvio stack locale (servizio + dipendenze):
- `make compose-up`

2) Build (se vuoi produrre l'artefatto prima del docker build):
- `make build`

3) Test:
- `make test`

Swagger:
- `http://localhost:8090/swagger-ui/index.html`

Health:
- `http://localhost:8090/actuator/health`

---

## Variabili ambiente principali
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `OAUTH2_ISSUER_URI`
- `SANITECH_PAYMENTS_WEBHOOK_SECRET` (secret per endpoint webhook provider)

---

## Sicurezza (Keycloak)
- Resource Server JWT: `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- Ruoli da token: `realm_access.roles` → `ROLE_*`
- Scope: `scope` → `SCOPE_*`
- Claim reparto (ABAC): `dept` → `DEPT_*` (non usato in payments ma mantenuto per coerenza)

Accesso API:
- `/api/payments/**` autenticato (PATIENT vede solo i propri ordini; ADMIN vede tutto)
- `/api/admin/payments/**` richiede `ROLE_ADMIN`
- `/api/webhooks/payments/**` protetto da header `X-Webhook-Secret`

---

## Outbox & Kafka
- Tabella: `outbox_events` (Flyway V3)
- Topic: `payments.events`
- Metriche:
  - `outbox.events.saved.count`
  - `outbox.events.published`

---

## Maven Wrapper
Se manca `.mvn/wrapper/maven-wrapper.jar`, rigeneralo con:
- `mvn -N io.takari:maven:wrapper`

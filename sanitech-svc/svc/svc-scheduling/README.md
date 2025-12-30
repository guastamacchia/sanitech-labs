# Sanitech — `svc-scheduling`

Microservizio del dominio **Scheduling** per la piattaforma **Sanitech**.

Questo servizio gestisce:

- **Slot** di disponibilità (es. visite in sede o televisite) associati a un medico e a un reparto.
- **Appuntamenti** prenotati su uno slot disponibile.
- **Outbox pattern**: pubblicazione affidabile di eventi su Kafka (topic: `scheduling.events`) a valle di create/update/cancel.
- **Sicurezza**: Keycloak OIDC come IdP, configurazione **Resource Server JWT** (nessun adapter legacy).

> Nota “consenso”: questo microservizio **non** applica logiche di consenso clinico. Il consenso viene verificato nei servizi clinici quando un **DOCTOR** accede ai dati clinici di un **PAZIENTE**.

---

## Requisiti

- Java 21
- Maven 3.9+ (oppure Maven Wrapper)
- PostgreSQL 16+
- Kafka 3.x

---

## Avvio rapido (dev)

```bash
make compose-up
```

Swagger UI:

- `http://localhost:8083/swagger-ui/index.html`

Health:

- `http://localhost:8083/actuator/health`

---

## Variabili d’ambiente principali

- `DB_URL` (es. `jdbc:postgresql://localhost:5433/sanitech_scheduling`)
- `DB_USER`
- `DB_PASSWORD`
- `OAUTH2_ISSUER_URI` (realm Keycloak, es. `http://localhost:8081/realms/sanitech`)
- `KAFKA_BOOTSTRAP_SERVERS` (es. `localhost:9092`)

---

## API principali (estratto)

### Slot

- `GET /api/slots` — ricerca slot disponibili (RateLimiter)
- `POST /api/admin/slots` — creazione slot (ROLE_ADMIN, opzionale ABAC su reparto)
- `DELETE /api/admin/slots/{id}` — annullamento slot (ROLE_ADMIN)

### Appuntamenti

- `POST /api/appointments` — prenotazione su slot
- `GET /api/appointments` — lista appuntamenti (meccanismo “self” per PATIENT/DOCTOR via claim JWT, oppure filtri per ADMIN)
- `DELETE /api/appointments/{id}` — cancellazione (PATIENT/ADMIN)

---

## Outbox (Kafka)

- Tabella: `outbox_events` (Flyway `V3__outbox.sql`)
- Publisher schedulato: `OutboxKafkaPublisher`
- Topic: `scheduling.events`
- Metriche Micrometer:
  - `outbox.events.saved.count`
  - `outbox.events.published`

---

## Build & test

```bash
make build
make test
```

---

## Profilo `prod`

È disponibile un profilo `prod` minimale in `src/main/resources/application-prod.yml` per esecuzione in ambienti reali (configurazione via env).

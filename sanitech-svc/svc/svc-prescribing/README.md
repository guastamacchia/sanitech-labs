# svc-prescribing

Microservizio **Prescribing**: gestione **prescrizioni e terapie** (creazione, consultazione, aggiornamento, annullamento) con:

- **Security**: Resource Server JWT (Keycloak/OIDC).
- **ABAC di reparto**: claim/authority `DEPT_*` (con override `ROLE_ADMIN`).
- **Consenso**: verifica sincrona verso `svc-consents` (scope `PRESCRIPTIONS`) per operazioni dei medici.
- **Outbox pattern**: eventi affidabili su Kafka (`prescribing.events`) per audit/integrazioni.

## API principali

Base path: `/api`

### Paziente

- `GET /api/prescriptions` — lista delle proprie prescrizioni
- `GET /api/prescriptions/{id}` — dettaglio (solo se appartenente al paziente)

### Medico

- `POST /api/doctor/prescriptions` — crea prescrizione (consenso richiesto)
- `GET /api/doctor/prescriptions?patientId=...&departmentCode=...` — lista per paziente e reparto (consenso richiesto)
- `GET /api/doctor/prescriptions/{id}` — dettaglio (consenso richiesto)
- `PUT /api/doctor/prescriptions/{id}` — replace righe (consenso richiesto)
- `PATCH /api/doctor/prescriptions/{id}` — update parziale note (consenso richiesto)
- `POST /api/doctor/prescriptions/{id}/cancel` — annulla (consenso richiesto)

### Admin

- `GET /api/admin/prescriptions` — lista (filtro opzionale `patientId`, `doctorId`)
- `GET /api/admin/prescriptions/{id}` — dettaglio

## Esecuzione in locale (docker compose)

Avvia Postgres + Kafka (porta Postgres: **5443**):

```bash
docker compose -f docker/docker-compose.yml up -d
```

Avvio applicazione (profilo `local`):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Swagger UI:

- `http://localhost:8091/swagger-ui.html`

Health:

- `http://localhost:8091/actuator/health`

## Variabili d'ambiente

- `PORT` (default 8091)
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS` (default `localhost:9092`)
- `OAUTH2_ISSUER_URI` (default `http://localhost:8081/realms/sanitech`)
- `CONSENTS_BASE_URL` (default `http://localhost:8085`)
- `OUTBOX_PUBLISH_DELAY_MS` (default 1000)

## Note architetturali (decisioni)

- **Consenso**: questo microservizio non “replica” logiche; la policy è centralizzata in `svc-consents`.
  In caso di indisponibilità del servizio consensi, la richiesta viene rifiutata (fail closed) con HTTP 503.
- **No FK cross-service**: `patientId` e `doctorId` sono ID applicativi (eventuali dettagli stanno in `svc-directory`).
- **Eventi**: scritti in outbox in transazione e pubblicati su Kafka per robustezza e auditability.

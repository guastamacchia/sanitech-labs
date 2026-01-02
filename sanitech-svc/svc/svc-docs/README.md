# svc-docs (Sanitech)

Microservizio per la **gestione documentale**:

- **Metadati** su Postgres (tabella `documents`)
- **Binari** su S3/MinIO (bucket configurabile)
- **Eventi affidabili** via **Outbox → Kafka** (`docs.events`)
- **Sicurezza**: Resource Server JWT (Keycloak), ruoli + ABAC per reparto (`DEPT_*`)
- **Consenso**: per accesso medico→paziente integra `svc-consents`

---

## Avvio in locale (docker-compose)

> Il file `infra/docker-compose.yml` avvia Postgres + Kafka + MinIO + svc-docs.

```bash
docker compose -f infra/docker-compose.yml up --build
```

Servizi esposti:
- svc-docs: `http://localhost:8086`
- Swagger UI: `http://localhost:8086/swagger-ui/index.html`
- Actuator: `http://localhost:8086/actuator/health`
- Postgres: `localhost:5436`
- Kafka: `localhost:9096`
- MinIO API: `http://localhost:9006` (console: `http://localhost:9007`)

---

## Configurazione (principali env)

| Variabile | Descrizione |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Connessione Postgres |
| `KAFKA_BOOTSTRAP_SERVERS` | Bootstrap Kafka |
| `OAUTH2_ISSUER_URI` | Issuer Keycloak (realm) |
| `CONSENTS_BASE_URL` | Base URL `svc-consents` |
| `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET` | Storage S3/MinIO |

---

## API (estratto)

- `GET /api/docs` (list; per DOCTOR richiede `patientId`)
- `GET /api/docs/{id}` (metadati)
- `GET /api/docs/{id}/download` (download streaming)
- `POST /api/docs/upload` (multipart upload; ADMIN/DOCTOR)
- `DELETE /api/admin/docs/{id}` (ADMIN)

---

## Sicurezza e policy

- `ROLE_ADMIN`:
  - accesso completo
- `ROLE_DOCTOR`:
  - list/download consentiti **solo** con:
    1) consenso valido (`svc-consents`)
    2) reparto coerente (authority `DEPT_<CODE>`)
- `ROLE_PATIENT`:
  - può leggere solo i propri documenti (claim JWT `pid`)

---

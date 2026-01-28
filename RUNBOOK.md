# Runbook — Sanitech Labs

Questo runbook raccoglie le procedure operative principali per l’ambiente di sviluppo e i check più comuni sui servizi backend.

## Prerequisiti
- Docker / Docker Compose (Buildx opzionale; utile per build multi-arch)
- Java 21
- Node.js (solo se si modifica la toolchain frontend)

## Avvio e stop stack

### Backend (stack completo)
```bash
bash .script/backend/up.sh
bash .script/backend/logs.sh     # log aggregati
bash .script/backend/status.sh   # stato servizi
bash .script/backend/down.sh     # stop (REMOVE_VOLUMES=true per pulire i volumi)
```

### Backend (make compose)
Da repository root:
```bash
make -C sanitech-svc compose-up
make -C sanitech-svc compose-up-infra
COMPOSE_BUILD=0 make -C sanitech-svc compose-up-infra   # salta il build immagini se vuoi usare immagini già disponibili
ENV=staging make -C sanitech-svc compose-up-infra       # usa il file .infra/env/env.staging
make -C sanitech-svc compose-down
make -C sanitech-svc compose-config
make -C sanitech-svc env-print
```



### Frontend
Prerequisiti:
- Node.js 18+
- (Opzionale) Docker / Docker Compose per i servizi backend

Avvio SPA Angular:
```bash
cd sanitech-fe
npm install
npm start
```

URL:
- SPA pubblica/privata: http://localhost:4200

Layout:
- `src`: SPA Angular (portale pubblico + area privata con ruoli).
- `.script/frontend/`: helper per up/down/logs/status.

Note:
- Presuppone backend su http://localhost:8080 e Keycloak su http://localhost:8081.

## Endpoint infrastruttura (dev)
- Keycloak: `http://localhost:8081`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin)
- MinIO: `http://localhost:9000` (console `http://localhost:9001`)
- MailHog: `http://localhost:8025`
- LiveKit: `http://localhost:7880` (console `http://localhost:7881`)

## Configurazione host vs container (OIDC)
- **Host (browser/CLI)**: Keycloak è raggiungibile da `http://localhost:8081`.
- **Container backend**: usare il service name Docker `keycloak` e la porta interna 8080 (es. `OAUTH2_HOST=keycloak`, `OAUTH2_ISSUER_URI=http://keycloak:8080/realms/sanitech`).
- `OAUTH2_HOST`/`OAUTH2_PORT` vengono usati per comporre il `jwk-set-uri` dei servizi (incluso `svc-gateway`).

## Health & osservabilità
- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- Metriche: `/actuator/prometheus`

## Script di test
- Backend stack: `.script/backend/*`
- Frontend stack: `.script/frontend/*`
- Smoke/loop per servizio: `.script/services/<svc-name>/*`

## Runbook servizi backend

### svc-gateway
**Sintomi**
- 503 su route downstream (circuit breaker aperto / retry esauriti).
- Swagger UI non mostra endpoint.
- 401/403 inattesi.

**Azioni**
1. Verificare health del microservizio downstream e URL configurati.
2. Controllare `spring.cloud.gateway.httpclient.*` e `resilience4j.circuitbreaker.instances.*`.
3. Verificare `sanitech.gateway.openapi.targets.*` e `GET /openapi/{service}`.
4. Validare issuer `OAUTH2_ISSUER_URI` e mapping ruoli/scope.

**Endpoint**
- `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.

### svc-directory
**Sintomi**
- Errori Flyway o DB.
- Outbox non pubblica su `directory.events`.
- Troppi 429 su `GET /api/doctors`.

**Azioni**
- Verificare `DB_URL/DB_USER/DB_PASSWORD` e versione Postgres.
- Controllare `KAFKA_HOST`/`KAFKA_PORT` e log del publisher.
- Tuning `resilience4j.ratelimiter.instances.directoryApi`.

**Smoke test manuale**
1. `GET /actuator/health`
2. `GET /api/doctors` con token valido
3. `GET /swagger-ui/index.html`

### svc-scheduling
**Sintomi**
- Health DOWN (DB/Flyway).
- Outbox non pubblicata su `scheduling.events`.
- 429 su `GET /api/slots`.

**Azioni**
- Controllare credenziali DB e migrazioni.
- Verificare Kafka e `OutboxKafkaPublisher`.
- Tuning `resilience4j.ratelimiter.instances.schedulingApi`.

**Smoke check**
- `curl -s http://localhost:8083/actuator/health | jq`
- `curl -s http://localhost:8083/v3/api-docs/scheduling | jq '.info.title'`

### svc-admissions
**Sintomi**
- Outbox cresce e non pubblica su `admissions.events`.
- Errori di connessione DB/Kafka.

**Azioni**
- Verificare `DB_*` e `KAFKA_HOST`/`KAFKA_PORT`.
- Controllare job outbox e log di retry/backoff.

### svc-consents
**Sintomi**
- Eventi non arrivano su Kafka (`consents.events`).
- `GET /api/consents/check` ritorna sempre `allowed=false`.

**Azioni**
- Verificare connettività Kafka, topic e log outbox.
- Controllare record consenso (`GRANTED`, non scaduto, scope corretto).

**Query utili**
```sql
SELECT *
FROM consents
WHERE patient_id = 1
  AND doctor_id = 1
  AND scope = 'RECORDS'
  AND status = 'GRANTED'
  AND (expires_at IS NULL OR expires_at > NOW());

SELECT id, aggregate_type, aggregate_id, event_type, occurred_at
FROM outbox_events
WHERE published = false
ORDER BY occurred_at
LIMIT 100;
```

### svc-docs
**Sintomi**
- Errori Flyway o connessione DB.
- Errori MinIO/S3 (bucket mancante o SignatureDoesNotMatch).
- Outbox non pubblica su `docs.events`.
- 403 su list/download documenti.

**Azioni**
- Verificare `DB_*` e versioni schema.
- Controllare `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`.
- Verificare `KAFKA_HOST`/`KAFKA_PORT` e log outbox.
- Controllare token JWT (ruolo, claim `pid`, consent check) e `CONSENTS_BASE_URL`.

**Verifica bucket MinIO**
```bash
mc alias set local http://localhost:9006 $MINIO_ROOT_USER $MINIO_ROOT_PASSWORD
mc ls local/${S3_BUCKET:-sanitech-docs}
```

### svc-notifications
**Sintomi**
- Outbox non pubblicata.
- Email non inviate.

**Azioni**
- Verificare `KAFKA_HOST`/`KAFKA_PORT` e stato `outbox_events`.
- Controllare configurazione SMTP (`spring.mail.*`) e MailHog (`http://localhost:8025`).

**Query utili**
```sql
SELECT published, count(*) FROM outbox_events GROUP BY published;

SELECT id, recipient_type, recipient_id, channel, created_at
FROM notifications
WHERE status = 'PENDING'
ORDER BY created_at ASC;
```

### svc-audit
**Sintomi**
- Ingestion Kafka non produce record.
- API audit risponde 403.

**Azioni**
- Verificare `KAFKA_HOST`/`KAFKA_PORT`, `AUDIT_INGESTION_TOPICS`, `sanitech.audit.ingestion.enabled`.
- Token con `ROLE_ADMIN` o scope `audit.write`/`audit.read`.

**Query utili**
```sql
SELECT id, occurred_at, source, actor_type, actor_id, action, resource_type, resource_id, outcome
FROM audit_events
ORDER BY occurred_at DESC
LIMIT 50;

SELECT *
FROM audit_events
WHERE source = 'kafka'
ORDER BY occurred_at DESC
LIMIT 50;

SELECT id, aggregate_type, aggregate_id, event_type, occurred_at
FROM outbox_events
WHERE published = false
ORDER BY occurred_at
LIMIT 100;
```

### svc-televisit
**Sintomi**
- Token LiveKit non valido / 401.
- Outbox non pubblicata su `televisit.events`.
- Rate limit aggressivo (429).

**Azioni**
- Verificare `LIVEKIT_API_KEY`, `LIVEKIT_API_SECRET`, `LIVEKIT_URL` e clock skew.
- Controllare Kafka e log outbox.
- Tuning `resilience4j.ratelimiter.instances.televisitApi.*`.

**Smoke test**
- `curl -s http://localhost:8089/actuator/health | jq`
- `curl -s http://localhost:8089/v3/api-docs/televisit | jq '.info'`

### svc-payments
**Sintomi**
- 401/403.
- Outbox non pubblicata su `payments.events`.
- Webhook rifiutato.

**Azioni**
- Verificare `OAUTH2_ISSUER_URI`, ruoli token e claim `pid` (PATIENT).
- Controllare `KAFKA_HOST`/`KAFKA_PORT` e log outbox.
- Verificare header `X-Webhook-Secret` e `SANITECH_PAYMENTS_WEBHOOK_SECRET`.

### svc-prescribing
**Sintomi**
- 403 su endpoint medico.
- 503 perché `svc-consents` non disponibile.

**Azioni**
- Verificare token (ruolo `DOCTOR`, scope `prescriptions.write`, claim `DEPT_*`).
- Controllare `CONSENTS_BASE_URL`, rete e log di `svc-consents`.
- Verificare `KAFKA_HOST`/`KAFKA_PORT` e job outbox.
- Controllare credenziali `DB_*` e `spring.flyway.enabled`.

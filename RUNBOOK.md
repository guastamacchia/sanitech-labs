# Runbook - Sanitech Labs

Procedure operative, health check e guida al troubleshooting per la piattaforma Sanitech Labs.

## Indice

- [Prerequisiti](#prerequisiti)
- [Gestione dello stack](#gestione-dello-stack)
- [Endpoint infrastrutturali](#endpoint-infrastrutturali)
- [Monitoraggio health](#monitoraggio-health)
- [Runbook dei servizi](#runbook-dei-servizi)
- [Operazioni database](#operazioni-database)
- [Operazioni Kafka](#operazioni-kafka)
- [Problemi comuni](#problemi-comuni)

## Prerequisiti

| Requisito | Versione | Scopo |
|-----------|----------|-------|
| Docker | 24+ | Runtime container |
| Docker Compose | V2 | Orchestrazione servizi |
| Java | 21 | Sviluppo backend |
| Node.js | 18+ | Sviluppo frontend |
| curl | qualsiasi | Test HTTP |
| jq | qualsiasi | Parsing JSON |

## Gestione dello stack

### Avvio dello stack

```bash
# Avvia lo stack completo (backend + infrastruttura + frontend)
bash .script/backend/up.sh

# Avvia con ambiente specifico
ENV=staging bash .script/backend/up.sh
ENV=prod bash .script/backend/up.sh

# Usando comandi Make
make -C sanitech-svc compose-up              # Avvia con build
COMPOSE_BUILD=0 make -C sanitech-svc compose-up  # Avvia senza rebuild
```

### Monitoraggio

```bash
# Visualizza log aggregati
bash .script/backend/logs.sh

# Controlla stato servizi
bash .script/backend/status.sh

# Segui log di un servizio specifico
docker compose -f .infra/docker-compose.yml logs -f svc-directory
```

### Arresto dello stack

```bash
# Ferma i servizi (mantiene i dati)
bash .script/backend/down.sh

# Ferma e rimuovi tutti i volumi (stato pulito)
REMOVE_VOLUMES=true bash .script/backend/down.sh

# Usando Make
make -C sanitech-svc compose-down
```

### Sviluppo frontend

```bash
# Server di sviluppo con hot reload
cd sanitech-fe
npm install
npm start
# Accessibile su http://localhost:4200

# Frontend basato su Docker
bash .script/frontend/up.sh
bash .script/frontend/down.sh
```

## Endpoint infrastrutturali

| Servizio | Porta | URL | Health check |
|----------|-------|-----|--------------|
| API Gateway | 8080 | http://localhost:8080 | `/actuator/health` |
| Keycloak | 8081 | http://localhost:8081 | Console admin |
| Kafka | 9092/29092 | `kafka:9092` (interno) | N/A |
| Prometheus | 9090 | http://localhost:9090 | `/-/healthy` |
| Grafana | 3000 | http://localhost:3000 | `/api/health` |
| MinIO API | 9000 | http://localhost:9000 | `/minio/health/live` |
| MinIO Console | 9001 | http://localhost:9001 | - |
| Mailpit | 8025 | http://localhost:8025 | - |
| LiveKit | 7880 | http://localhost:7880 | - |

### Credenziali

| Servizio | Username | Password | Note |
|----------|----------|----------|------|
| Keycloak | Vedi env | Vedi env | `KC_BOOTSTRAP_ADMIN_*` |
| Grafana | admin | admin | Cambiare al primo login |
| MinIO | Vedi env | Vedi env | `MINIO_ROOT_*` |

## Monitoraggio health

### Endpoint health dei servizi

Tutti i servizi backend espongono endpoint Spring Boot Actuator:

| Endpoint | Scopo |
|----------|-------|
| `/actuator/health` | Stato health complessivo |
| `/actuator/health/liveness` | Probe liveness Kubernetes |
| `/actuator/health/readiness` | Probe readiness Kubernetes |
| `/actuator/prometheus` | Metriche Prometheus |
| `/actuator/info` | Info applicazione |

### Script di health check rapido

```bash
#!/usr/bin/env bash
SERVICES=(
  "svc-gateway:8080"
  "svc-directory:8082"
  "svc-scheduling:8083"
  "svc-admissions:8084"
  "svc-consents:8085"
  "svc-docs:8086"
  "svc-notifications:8087"
  "svc-audit:8088"
  "svc-televisit:8089"
  "svc-payments:8090"
  "svc-prescribing:8091"
)

for svc in "${SERVICES[@]}"; do
  name="${svc%%:*}"
  port="${svc##*:}"
  status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/actuator/health")
  echo "${name}: ${status}"
done
```

### Smoke test

Smoke test per servizio disponibili:

```bash
# Smoke test gateway
bash .script/services/svc-gateway/smoke.sh

# Test rate limiting
bash .script/services/svc-gateway/rate-limit.sh

# Test circuit breaker
bash .script/services/svc-gateway/circuit-breaker.sh
```

## Runbook dei servizi

### svc-gateway (Porta 8080)

**Sintomi**
- HTTP 503 su route downstream
- Swagger UI non carica gli endpoint
- Errori autenticazione HTTP 401/403

**Troubleshooting**

1. **Verificare health dei servizi downstream**
   ```bash
   curl -s http://localhost:8082/actuator/health | jq
   curl -s http://localhost:8083/actuator/health | jq
   ```

2. **Verificare stato circuit breaker**
   ```bash
   curl -s http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state | jq
   ```

3. **Controllare configurazione OIDC**
   ```bash
   curl -s http://localhost:8081/realms/sanitech/.well-known/openid-configuration | jq
   ```

4. **Verificare configurazione routing**
   - Controllare variabili ambiente `DIRECTORY_URL`, `SCHEDULING_URL`, ecc.
   - Verificare risoluzione DNS servizi nella rete Docker

**Configurazione**
- `resilience4j.circuitbreaker.instances.*` - Impostazioni circuit breaker
- `spring.cloud.gateway.httpclient.*` - Impostazioni client HTTP
- `OAUTH2_ISSUER_URI` - URL realm Keycloak

---

### svc-directory (Porta 8082)

**Sintomi**
- Errori migrazione Flyway
- Outbox non pubblica su `directory.events`
- Rate limiting HTTP 429

**Troubleshooting**

1. **Verificare connettività database**
   ```bash
   docker exec -it sanitech-pg-directory psql -U sanitech -d directory -c "SELECT 1"
   ```

2. **Verificare migrazioni Flyway**
   ```bash
   docker exec -it sanitech-pg-directory psql -U sanitech -d directory \
     -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5"
   ```

3. **Controllare stato outbox**
   ```sql
   SELECT published, COUNT(*) FROM outbox_events GROUP BY published;
   SELECT * FROM outbox_events WHERE published = false ORDER BY occurred_at LIMIT 10;
   ```

4. **Testare API**
   ```bash
   curl -s http://localhost:8082/actuator/health | jq
   curl -s http://localhost:8082/v3/api-docs/directory | jq '.info'
   ```

---

### svc-scheduling (Porta 8083)

**Sintomi**
- Health DOWN (DB/Flyway)
- Outbox non pubblica su `scheduling.events`
- HTTP 429 su query slot

**Troubleshooting**

1. **Verificare health servizio**
   ```bash
   curl -s http://localhost:8083/actuator/health | jq
   ```

2. **Verificare database**
   ```bash
   docker exec -it sanitech-pg-scheduling psql -U sanitech -d scheduling -c "SELECT COUNT(*) FROM slots"
   ```

3. **Controllare rate limiter**
   ```bash
   curl -s http://localhost:8083/actuator/metrics/resilience4j.ratelimiter.available.permissions | jq
   ```

---

### svc-consents (Porta 8085)

**Sintomi**
- Eventi non pubblicati su `consents.events`
- `/api/consents/check` ritorna sempre `allowed=false`

**Troubleshooting**

1. **Verificare esistenza consenso**
   ```sql
   SELECT id, patient_id, doctor_id, scope, status, granted_at, expires_at
   FROM consents
   WHERE patient_id = ? AND doctor_id = ?
     AND status = 'GRANTED'
     AND (expires_at IS NULL OR expires_at > NOW());
   ```

2. **Controllare outbox**
   ```sql
   SELECT id, aggregate_type, event_type, published, occurred_at
   FROM outbox_events
   WHERE published = false
   ORDER BY occurred_at
   LIMIT 100;
   ```

3. **Testare endpoint check consenso**
   ```bash
   curl -s "http://localhost:8085/api/consents/check?patientId=1&doctorId=1&scope=RECORDS" \
     -H "Authorization: Bearer ${TOKEN}" | jq
   ```

---

### svc-docs (Porta 8086)

**Sintomi**
- Errori MinIO/S3 (bucket mancante, signature mismatch)
- Outbox non pubblica su `docs.events`
- HTTP 403 su accesso documenti

**Troubleshooting**

1. **Verificare bucket MinIO**
   ```bash
   mc alias set local http://localhost:9000 ${MINIO_ROOT_USER} ${MINIO_ROOT_PASSWORD}
   mc ls local/sanitech-docs
   mc admin info local
   ```

2. **Controllare configurazione S3**
   - Verificare `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`

3. **Verificare integrazione consensi**
   - Controllare che `CONSENTS_BASE_URL` punti a svc-consents
   - Verificare che svc-consents sia healthy

---

### svc-notifications (Porta 8087)

**Sintomi**
- Email non inviate
- Outbox cresce senza pubblicare

**Troubleshooting**

1. **Controllare Mailpit per email consegnate**
   - Aprire http://localhost:8025

2. **Verificare configurazione SMTP**
   - `MAIL_HOST`, `MAIL_PORT` devono puntare al container mailhog

3. **Controllare notifiche pendenti**
   ```sql
   SELECT id, recipient_type, recipient_id, channel, status, created_at
   FROM notifications
   WHERE status = 'PENDING'
   ORDER BY created_at ASC
   LIMIT 20;
   ```

---

### svc-audit (Porta 8088)

**Sintomi**
- Ingestion Kafka non produce record
- HTTP 403 su API audit

**Troubleshooting**

1. **Verificare consumer Kafka**
   ```bash
   curl -s http://localhost:8088/actuator/health | jq '.components.kafka'
   ```

2. **Controllare configurazione ingestion**
   - `AUDIT_INGESTION_TOPICS` deve elencare tutti i topic sorgente
   - `sanitech.audit.ingestion.enabled` deve essere `true`

3. **Verificare autorizzazione**
   - Il token deve avere `ROLE_ADMIN` o scope `audit.read`/`audit.write`

4. **Query eventi audit**
   ```sql
   SELECT id, occurred_at, source, actor_type, actor_id, action, resource_type, outcome
   FROM audit_events
   ORDER BY occurred_at DESC
   LIMIT 50;
   ```

---

### svc-televisit (Porta 8089)

**Sintomi**
- Errori token LiveKit (401)
- Rate limiting (429)

**Troubleshooting**

1. **Verificare configurazione LiveKit**
   - Controllare `LIVEKIT_API_KEY`, `LIVEKIT_API_SECRET`, `LIVEKIT_URL`
   - Assicurarsi che non ci sia clock skew tra i servizi

2. **Testare connettività LiveKit**
   ```bash
   curl -s http://localhost:7880/healthz
   ```

3. **Controllare rate limiter**
   ```bash
   curl -s http://localhost:8089/actuator/metrics/resilience4j.ratelimiter.available.permissions | jq
   ```

---

### svc-payments (Porta 8090)

**Sintomi**
- HTTP 401/403
- Webhook rifiutato

**Troubleshooting**

1. **Verificare secret webhook**
   - Controllare `SANITECH_PAYMENTS_WEBHOOK_SECRET`
   - Le richieste webhook devono includere header `X-Webhook-Secret`

2. **Controllare claim paziente**
   - Il token deve avere claim `pid` per il contesto paziente

---

### svc-prescribing (Porta 8091)

**Sintomi**
- HTTP 403 su endpoint medico
- HTTP 503 quando svc-consents non disponibile

**Troubleshooting**

1. **Verificare autorizzazione medico**
   - Il token deve avere `ROLE_DOCTOR` e claim `DEPT_*`

2. **Controllare servizio consensi**
   ```bash
   curl -s http://localhost:8085/actuator/health | jq
   ```
   - Verificare che `CONSENTS_BASE_URL` sia corretto

## Operazioni database

### Connessione ai database dei servizi

```bash
# Database directory
docker exec -it sanitech-pg-directory psql -U sanitech -d directory

# Lista tutti i database dei servizi
for svc in directory scheduling admissions consents docs notifications audit televisit payments prescribing gateway; do
  echo "=== ${svc} ==="
  docker exec -it sanitech-pg-${svc} psql -U sanitech -d ${svc} -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'"
done
```

### Query comuni

**Stato outbox tra i servizi:**
```sql
SELECT
  published,
  COUNT(*) as count,
  MIN(occurred_at) as oldest,
  MAX(occurred_at) as newest
FROM outbox_events
GROUP BY published;
```

**Eventi non pubblicati:**
```sql
SELECT id, aggregate_type, aggregate_id, event_type, occurred_at
FROM outbox_events
WHERE published = false
ORDER BY occurred_at
LIMIT 100;
```

## Operazioni Kafka

### Visualizzazione topic

```bash
# Lista tutti i topic
docker exec -it sanitech-kafka kafka-topics --bootstrap-server kafka:9092 --list

# Descrivi un topic
docker exec -it sanitech-kafka kafka-topics --bootstrap-server kafka:9092 \
  --describe --topic directory.events
```

### Consumo messaggi

```bash
# Consuma dall'inizio
docker exec -it sanitech-kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic directory.events \
  --from-beginning \
  --max-messages 10

# Consuma ultimi messaggi
docker exec -it sanitech-kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic audit.events
```

### Consumer group

```bash
# Lista consumer group
docker exec -it sanitech-kafka kafka-consumer-groups --bootstrap-server kafka:9092 --list

# Descrivi consumer group
docker exec -it sanitech-kafka kafka-consumer-groups --bootstrap-server kafka:9092 \
  --describe --group audit-ingestion-group
```

## Problemi comuni

### Mismatch configurazione OIDC

**Problema**: I servizi falliscono la validazione JWT con mismatch issuer.

**Soluzione**:
- Browser/CLI usa `http://localhost:8081`
- I container usano `http://keycloak:8080`
- Impostare `OAUTH2_HOST=keycloak` e `OAUTH2_PORT=8080` per i container

### Outbox non pubblica

**Problema**: Eventi bloccati nella tabella outbox.

**Checklist**:
1. Verificare `OUTBOX_PUBLISHER_ENABLED=true`
2. Controllare connettività Kafka: `KAFKA_HOST`, `KAFKA_PORT`
3. Esaminare log servizio per errori publisher
4. Controllare configurazione `OUTBOX_TOPIC`

### Connessione database rifiutata

**Problema**: Il servizio non riesce a connettersi a PostgreSQL.

**Checklist**:
1. Verificare che il container database sia healthy: `docker ps`
2. Controllare variabili ambiente: `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_USER`, `DATABASE_PASSWORD`
3. Assicurarsi che il container sia sulla stessa rete Docker

### Rate limiting (429)

**Problema**: Troppe risposte 429.

**Soluzione**:
1. Controllare metriche rate limiter:
   ```bash
   curl -s http://localhost:${PORT}/actuator/metrics/resilience4j.ratelimiter.available.permissions | jq
   ```
2. Regolare `resilience4j.ratelimiter.instances.*.limitForPeriod` e `limitRefreshPeriod`

### Circuit breaker aperto

**Problema**: Richieste falliscono con 503, circuit breaker aperto.

**Soluzione**:
1. Controllare stato circuit breaker:
   ```bash
   curl -s http://localhost:${PORT}/actuator/health | jq '.components.circuitBreakers'
   ```
2. Risolvere il problema del servizio sottostante
3. Attendere che il circuit breaker passi a HALF_OPEN e poi CLOSED

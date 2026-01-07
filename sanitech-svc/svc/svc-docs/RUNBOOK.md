# RUNBOOK — svc-docs (Sanitech)

## Sintomi comuni

### 1) Il servizio non parte: errori Flyway o connessione DB
- Verificare `DB_URL/DB_USER/DB_PASSWORD`.
- Controllare che il database sia PostgreSQL 16+ e raggiungibile dal container.
- Se il DB è stato inizializzato con una versione precedente, ispezionare `flyway_schema_history`.

### 2) Errori MinIO/S3 (SignatureDoesNotMatch / bucket mancante)
- Controllare `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`.
- Verificare che il bucket esista (`mc alias set local ... && mc ls local`).
- Se l'errore riguarda SSL, allineare `S3_REGION` e l'endpoint (http/https).

### 3) Outbox non pubblica su Kafka
- Verificare `KAFKA_BOOTSTRAP_SERVERS`.
- Controllare i log del `OutboxKafkaPublisher` e la topic `docs.events` (creazione abilitata nel cluster dev).
- Se i retry sono esauriti, validare i messaggi in `outbox_events` e riprovare dopo aver stabilizzato Kafka.

### 4) 403 su list/download documenti
- Verificare il token JWT:
  - `ROLE_DOCTOR` richiede consenso valido da `svc-consents` e authority `DEPT_<CODE>` coerente.
  - `ROLE_PATIENT` necessita del claim `pid` valorizzato.
- Controllare `CONSENTS_BASE_URL` e reachability di `svc-consents`.

## Operazioni

### Smoke test manuale
1. `GET /actuator/health` → 200
2. `GET /actuator/metrics` → 200
3. `GET /swagger-ui/index.html` → 200

### Verifica bucket MinIO
- Console: `http://localhost:9007` (credenziali da `MINIO_ROOT_USER/MINIO_ROOT_PASSWORD`).
- Oppure via CLI:
  ```bash
  mc alias set local http://localhost:9006 $MINIO_ROOT_USER $MINIO_ROOT_PASSWORD
  mc ls local/${S3_BUCKET:-sanitech-docs}
  ```

### Gestione outbox bloccata
- Gli eventi non pubblicati rimangono con `published=false`.
- Verificare:
  - Kafka raggiungibile (`KAFKA_BOOTSTRAP_SERVERS`)
  - eventuali errori di serializzazione o timeout
  - tuning retry/backoff per `outboxPublish`

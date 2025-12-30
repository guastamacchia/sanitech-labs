# RUNBOOK — svc-prescribing

## 1) Sintomi comuni

### 1.1 HTTP 403 su endpoint medico

Possibili cause:
- token senza ruolo `DOCTOR` (o senza scope `prescriptions.write`);
- token privo di authority `DEPT_*` coerente con `departmentCode`;
- consenso non presente in `svc-consents` (scope `PRESCRIPTIONS`).

### 1.2 HTTP 503 su operazioni medico

Il servizio `svc-consents` è indisponibile o il circuit breaker è aperto.
Verificare:
- `CONSENTS_BASE_URL`
- disponibilità rete / DNS
- log di `svc-consents`

## 2) Verifiche operative

### 2.1 Health

- `/actuator/health`

### 2.2 Prometheus

- `/actuator/prometheus`

Metriche utili:
- `outbox.events.saved.count{aggregateType="PRESCRIPTION"...}`
- `outbox.events.published.count{aggregateType="PRESCRIPTION"...}`

## 3) Kafka / Outbox

Topic: `prescribing.events`

Se gli eventi non escono:
- controllare `KAFKA_BOOTSTRAP_SERVERS`;
- verificare errori in log (timeout, auth Kafka);
- verificare che il job schedulato sia attivo (`sanitech.outbox.publisher.delay-ms`).

## 4) Database

Schema gestito da Flyway (`db/migration`).
In caso di problemi:
- controllare credenziali `DB_*`;
- verificare che Flyway sia abilitato (`spring.flyway.enabled=true`).

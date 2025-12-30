# Sanitech — Platform (docker-compose + README)

Questa cartella contiene i file **root** per avviare in locale (dev) l’intera piattaforma **Sanitech** tramite **Docker Compose**.

> Nota: ogni microservizio è distribuito come progetto Maven separato (ZIP dedicato).  
> Per usare questo `docker-compose.yml`, estrai ciascun microservizio in una cartella **allo stesso livello** di questo file:
>
> ```text
> sanitech-platform/
> ├─ docker-compose.yml
> ├─ README.md
> ├─ infra/...
> ├─ svc-directory/
> ├─ svc-scheduling/
> ├─ svc-admissions/
> ├─ svc-consents/
> ├─ svc-docs/
> ├─ svc-notifications/
> ├─ svc-audit/
> ├─ svc-televisit/
> ├─ svc-payments/
> └─ svc-prescribing/
> ```

---

## 1) Avvio rapido (locale)

```bash
docker compose up -d --build
```

Attendi che tutti i container risultino `healthy`:

```bash
docker compose ps
```

---

## 2) Servizi esposti (porte host)

- Keycloak: `http://localhost:8081`
- API Gateway: `http://localhost:8080`
- svc-directory: `http://localhost:8082`
- svc-scheduling: `http://localhost:8083`
- svc-admissions: `http://localhost:8084`
- svc-consents: `http://localhost:8085`
- svc-docs: `http://localhost:8086`
- svc-notifications: `http://localhost:8087`
- svc-audit: `http://localhost:8088`
- svc-televisit: `http://localhost:8089`
- svc-payments: `http://localhost:8090`
- svc-prescribing: `http://localhost:8091`
- Kafka (interno): `kafka:9092`
- MinIO (S3): `http://localhost:9000` (console: `http://localhost:9001`)
- MailHog: `http://localhost:8025`

---

## 3) Keycloak (SSO)

Questo compose importa automaticamente un realm **sanitech** da:

`infra/keycloak/realm-export/sanitech-realm.json`

Credenziali amministratore (dev):

- user: `admin`
- password: `admin`

Policy token (riassunto):
- i servizi validano JWT via **Spring Security Resource Server** (no adapter legacy Keycloak).
- i ruoli arrivano da `realm_access.roles`.
- attributo custom `dept` viene pubblicato nel token come claim per ABAC di reparto (authority `DEPT_*`).

---

## 4) Consenso (importante)

Il **consenso NON è gestito in `svc-directory`** e **NON è richiesto** per consultare l’elenco dei medici.

Il consenso viene verificato nei servizi clinici (es. prescrizioni / cartella clinica / documenti), quando un **DOCTOR** tenta di accedere ai dati di un **PATIENT**.  
Questa logica è centralizzata in `svc-consents` e chiamata dai servizi a valle (con Circuit Breaker / retry, fail-closed).

---

## 5) Smoke test

Esegui:

```bash
bash scripts/smoke.sh
```

---

## 6) Shutdown

```bash
docker compose down -v
```

---

## 7) Note operative

- I DB Postgres sono container dedicati (uno per microservizio). In produzione saranno gestiti esternamente.
- Kafka e MinIO sono per sviluppo locale; in produzione si usano cluster gestiti.

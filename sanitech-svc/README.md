# Sanitech — Platform (docker-compose + backend)

Questa cartella contiene i file **root** per avviare in locale (dev) l’intera piattaforma **Sanitech** tramite **Docker Compose** e per build/test dell’aggregatore backend.

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

---

## Struttura backend (svc/)

La directory `svc/` contiene tutti i microservizi Spring Boot sotto un unico POM `packaging=pom` e il Maven Wrapper (`./svc/mvnw`).

| Servizio            | Cartella                       | Porta di default | Note |
| ------------------- | ------------------------------ | ---------------- | ---- |
| API Gateway         | `svc/svc-gateway`              | 8080             | Autenticazione/ingress |
| Directory           | `svc/svc-directory`            | 8082             | Anagrafe medici/strutture |
| Scheduling          | `svc/svc-scheduling`           | 8083             | Agenda appuntamenti |
| Admissions          | `svc/svc-admissions`           | 8084             | Ricoveri / accettazione |
| Consents            | `svc/svc-consents`             | 8085             | Gestione consensi paziente |
| Docs                | `svc/svc-docs`                 | 8086             | Documenti clinici + MinIO |
| Notifications       | `svc/svc-notifications`        | 8087             | Email/SMS via MailHog |
| Audit               | `svc/svc-audit`                | 8088             | Tracciamento audit |
| Televisit           | `svc/svc-televisit`            | 8089             | Livekit / videocall |
| Payments            | `svc/svc-payments`             | 8090             | Pagamenti (Stripe) |
| Prescribing         | `svc/svc-prescribing`          | 8091             | Prescrizioni mediche |

Ogni microservizio include:

- `README.md`: dettagli specifici del servizio
- `Makefile`: comandi rapidi di build/run per il singolo servizio
- `RUNBOOK.md`: procedure operative e troubleshooting
- `scripts/`: script di smoke/load test locali

Le collezioni Postman non sono più versionate nel repository.

## Makefile (root)

Target principali:

- `make build` – build completa con skip dei test
- `make test` – esegue i test su tutti (o solo sui moduli indicati con `MODULES=...`)
- `make clean` – pulizia artefatti
- `make docker-build` – build immagini via Docker Compose
- `make docker-run` – avvio stack via Docker Compose
- `make compose-up` – avvio stack completo (con build)
- `make compose-up-infra` – avvio sola infrastruttura (postgres, kafka, keycloak, prometheus, grafana)
- `make compose-down` – stop completo (con volumi)
- `make compose-config` – stampa configurazione Compose risultante
- `make env-print` – stampa le variabili Compose risolte

Il Makefile segue lo standard usato dai microservizi (sezioni e naming), mantenendo però la selezione moduli tramite `MODULES`/`PROFILE`.

Variabili utili:

- `MODULES` (opzionale): lista separata da virgole di moduli da includere, es. `MODULES=svc-directory,svc-consents`
- `PROFILE` (opzionale): profilo Maven, es. `PROFILE=local`
- `MAVEN_ARGS` (opzionale): argomenti extra passati a Maven
- `MVN` (opzionale): comando Maven da usare (default: `mvn` se disponibile, altrimenti `./svc/mvnw`)
- `POM` (opzionale): path al POM aggregatore (default: `svc/pom.xml`)
- `COMPOSE_FILE` (opzionale): path al `docker-compose.yml` (default: `infra/docker-compose.yml`)
- `COMPOSE_INFRA_PORTS_FILE` (opzionale): override compose extra per porte infrastruttura
- `COMPOSE_INFRA_SERVICES` (opzionale): lista servizi avviati da `compose-up-infra`

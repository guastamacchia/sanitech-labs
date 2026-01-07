# Sanitech — Backend microservices (aggregator)

Questa cartella raccoglie tutti i microservizi Spring Boot della piattaforma Sanitech sotto un unico POM `packaging=pom`. È il punto di ingresso per build/test di tutto il backend con il Maven Wrapper incluso in questa directory.

## Struttura e collegamenti

| Servizio            | Cartella                       | Porta di default | Note |
| ------------------- | ------------------------------ | ---------------- | ---- |
| API Gateway         | `svc-gateway`                  | 8080             | Autenticazione/ingress |
| Directory           | `svc-directory`                | 8082             | Anagrafe medici/strutture |
| Scheduling          | `svc-scheduling`               | 8083             | Agenda appuntamenti |
| Admissions          | `svc-admissions`               | 8084             | Ricoveri / accettazione |
| Consents            | `svc-consents`                 | 8085             | Gestione consensi paziente |
| Docs                | `svc-docs`                     | 8086             | Documenti clinici + MinIO |
| Notifications       | `svc-notifications`            | 8087             | Email/SMS via MailHog |
| Audit               | `svc-audit`                    | 8088             | Tracciamento audit |
| Televisit           | `svc-televisit`                | 8089             | Livekit / videocall |
| Payments            | `svc-payments`                 | 8090             | Pagamenti (Stripe) |
| Prescribing         | `svc-prescribing`              | 8091             | Prescrizioni mediche |

Ogni cartella contiene:

- `README.md`: dettagli specifici del servizio
- `Makefile`: comandi rapidi di build/run per il singolo servizio
- `RUNBOOK.md`: procedure operative e troubleshooting
- `scripts/`: script di smoke/load test locali
- `postman/`: collezioni ed environment Postman dedicati

## Come usare il Maven Wrapper

Il wrapper (`./mvnw`, `./mvnw.cmd`) è già configurato per Maven 3.9.12, ma il Makefile preferisce un `mvn` già installato sull'host (fallback al wrapper). Esempi:

```bash
# Build di tutti i moduli (skip test)
./mvnw -T1C -DskipTests clean package

# Test di un sottoinsieme
MODULES="svc-directory,svc-consents" ./mvnw -T1C -pl ${MODULES} -am test
```

> Suggerimento: usa il `Makefile` in questa cartella per i comandi più comuni.

## Makefile (aggregatore)

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
- `MVN` (opzionale): comando Maven da usare (default: `mvn` se disponibile, altrimenti `./mvnw`)
- `COMPOSE_FILE` (opzionale): path al `docker-compose.yml` (default: `../infra/docker-compose.yml`)
- `COMPOSE_INFRA_PORTS_FILE` (opzionale): override compose extra per porte infrastruttura
- `COMPOSE_INFRA_SERVICES` (opzionale): lista servizi avviati da `compose-up-infra`

## Risorse utili

- Docker Compose + infrastruttura: `../infra/docker-compose.yml` e script wrapper in `../scripts`
- Postman di piattaforma: `../postman`
- Runbook di piattaforma: `../README.md` (avvio stack) e `RUNBOOK.md` in questa cartella (operatività backend)

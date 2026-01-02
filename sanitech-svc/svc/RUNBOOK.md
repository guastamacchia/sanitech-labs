# Runbook — Sanitech backend microservices

Questo runbook copre le attività operative sul monorepo dei microservizi (`svc/`).

## Prerequisiti

- Java 21
- Docker (per test locali con Compose)
- Nessuna installazione Maven necessaria: usare il wrapper (`./mvnw`)

## Build & test

### Build completa
```bash
make build
```

### Test mirati su moduli specifici
```bash
MODULES=svc-directory,svc-consents make test
```

### Pulizia
```bash
make clean
```

Variabili disponibili:
- `MODULES`: lista di moduli (virgola) da includere con `-pl`/`-am`
- `PROFILE`: profilo Maven (es. `local`, `prod`)
- `MAVEN_ARGS`: extra flag (es. `-DskipTests=false`)

## Esecuzione di un singolo servizio

1. Entrare nella cartella del servizio, es.:
   ```bash
   cd svc-directory
   ```
2. Usare il relativo Makefile:
   ```bash
   make run          # spring-boot:run
   make build        # build con skip test
   make test         # test del singolo servizio
   ```

## Avvio dello stack completo

Dal root progetto `sanitech-svc/`:
```bash
bash scripts/up.sh
```
Questo usa `infra/docker-compose.yml` e avvia Keycloak, gateway, Kafka, Postgres, MinIO, Prometheus, Grafana, ecc.

Spegnimento:
```bash
bash scripts/down.sh           # mantiene i volumi
REMOVE_VOLUMES=true bash scripts/down.sh  # elimina i volumi
```

Log live dei principali servizi:
```bash
bash scripts/logs.sh
```

## Postman

- Collezione di piattaforma: `../postman/sanitech-backend.postman_collection.json`
- Environment locale: `../postman/sanitech-backend.postman_environment.json`
- Ogni servizio espone una cartella `postman/` con collezioni dedicate.

## Troubleshooting rapido

- **Build fallisce per versioni Maven**: assicurarsi di usare `./mvnw` da questa cartella, non una installazione globale.
- **Porta occupata**: verificare con `docker compose ps` (da `infra/`) o modificare le porte nei singoli `application-*.yml`.
- **Keycloak non pronto**: lo stack imposta healthcheck; consultare `scripts/logs.sh keycloak` per il dettaglio.
- **Kafka/DB non raggiungibili**: controllare che `infra/docker-compose.yml` sia stato avviato e le variabili `KAFKA_BOOTSTRAP_SERVERS` / `DB_URL` dei servizi puntino ai container interni.

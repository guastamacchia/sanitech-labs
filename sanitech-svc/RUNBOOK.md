# Runbook — Sanitech Backend (sanitech-svc)

## Prerequisiti
- Docker / Docker Compose
- Java 21
- Maven (wrapper incluso: `./svc/mvnw`)

## Avvio stack locale (infra + microservizi)
```bash
bash scripts/up.sh
# log:   bash scripts/logs.sh
# stop:  bash scripts/down.sh        # REMOVE_VOLUMES=true per pulire i volumi
# stato: bash scripts/status.sh
```

Servizi esposti (host):
- Gateway: http://localhost:8080
- Keycloak: http://localhost:8081
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- MinIO: http://localhost:9000 (console 9001)

## Build & test (aggregatore)
```bash
make build   # package con skip test
make test    # test su tutti i moduli (usa MODULES=svc-... per filtrare)
```

Variabili utili:
- `MODULES=svc-directory,svc-consents`
- `PROFILE=local`
- `MAVEN_ARGS=-DskipTests=false`
- `MVN=mvn`
- `POM=svc/pom.xml`

## Docker / Compose (Makefile root)

Avvio stack completo:
```bash
make compose-up
```

Avvio sola infrastruttura:
```bash
make compose-up-infra
```

Stop completo:
```bash
make compose-down
```

Debug configurazione:
```bash
make compose-config
```

Stampa variabili Compose risolte:
```bash
make env-print
```

## Postman
- Collezione ed environment: `postman/`

## Note operative
- I Dockerfile per Prometheus/Grafana/Keycloak sono in `../infra/svc/`.
- Ogni microservizio ha il proprio `infra/docker-compose.yml` e Makefile locale per run/build.
- Gli script sotto `scripts/` sono utili per uno start/stop completo con opzioni extra (es. cleanup volumi), mentre i target `make compose-*` sono più rapidi per iterazioni locali.

ARTIFACT_ID ?= svc-directory
SVC_NAME ?= $(ARTIFACT_ID)
PORT ?= 8082
# Prefer Maven already installed on the host to avoid wrapper downloads when offline.
MVN ?= $(shell command -v mvn >/dev/null 2>&1 && echo mvn || echo ./mvnw)

# =====================================================
# Variabili di progetto esportate
# =====================================================
export ARTIFACT_ID
export SVC_NAME

# =====================================================
# Mapping profilo/env
# =====================================================
# PROFILE: profilo Spring (local | staging | prod)
# ENV:     nome del file env (di default coincide con PROFILE)
# PROFILE ?= local
# PROFILE ?= staging
PROFILE ?= remote
ENV ?= $(PROFILE)
export PROFILE
export ENV

# =====================================================
# File di configurazione
# =====================================================
COMPOSE_FILE ?= infra/docker-compose.yml
COMPOSE_INFRA_PORTS_FILE ?= infra/docker-compose.infra-ports.yml
DOCKERFILE ?= infra/Dockerfile

# Tag immagine: sanitech/<svc-name>:<env>
IMAGE ?= sanitech/$(SVC_NAME):$(ENV)

# File env di default: infra/env/env.<env>
ENV_FILE ?= infra/env/env.$(ENV)

# =====================================================
# Path assoluti per evitare problemi con IDE e working dir diversi
# =====================================================
MAKEFILE_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
ENV_FILE := $(abspath $(ENV_FILE))
COMPOSE_FILE := $(abspath $(COMPOSE_FILE))
COMPOSE_INFRA_PORTS_FILE := $(abspath $(COMPOSE_INFRA_PORTS_FILE))

# Rende visibile ENV_FILE a docker-compose.yml (es. env_file: - ${ENV_FILE})
export ENV_FILE

# =====================================================
# Fail-fast: il file env deve esistere
# =====================================================
ifeq (,$(wildcard $(ENV_FILE)))
$(error Env file non trovato: $(ENV_FILE))
endif

# =====================================================
# Caricamento variabili dal file .env per comandi host (mvn run/test/build)
# =====================================================
# NOTE:
# - include legge le coppie KEY=VALUE dal file
# - export rende disponibili le variabili ai processi figli (mvn, ecc.)
include $(ENV_FILE)
export $(shell sed -n 's/^\([A-Za-z_][A-Za-z0-9_]*\)=.*/\1/p' $(ENV_FILE))

# =====================================================
# Selezione comando Docker Compose
# =====================================================
# Se presente, usa docker-compose (legacy); altrimenti docker compose (v2).
DOCKER_COMPOSE := $(shell command -v docker-compose >/dev/null 2>&1 && echo docker-compose || echo "docker compose")

.PHONY: build test run docker-build docker-run compose-up compose-up-infra compose-down compose-down-infra env-print compose-config

# =====================================================
# Maven
# =====================================================
# I target Maven usano:
# - il profilo Spring definito in PROFILE
# - le variabili caricate da ENV_FILE (DATABASE_*, OAUTH2_*, KAFKA_*, ecc.)
build:
	$(MVN) clean -DskipTests package

test:
	$(MVN) test

run:
	$(MVN) spring-boot:run

# =====================================================
# Docker
# =====================================================
# Build dell'immagine del microservizio usando il Dockerfile sotto infra/
docker-build: build
	docker build -f $(DOCKERFILE) -t $(IMAGE) .

# Run standalone dell'immagine (utile per smoke test rapido)
docker-run:
	docker run --rm --env-file $(ENV_FILE) -p $(PORT):$(PORT) $(IMAGE)

# =====================================================
# Docker Compose
# =====================================================
# Avvio stack completo (database, Kafka, Keycloak, svc, osservabilità)
compose-up: build
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) up -d --build

# Avvio infrastruttura senza il microservizio
compose-up-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) up -d postgres kafka keycloak prometheus grafana

# Stop + cleanup completo (include volumi)
compose-down:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down -v

# Stop + cleanup dell'infrastruttura
compose-down-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) down -v

# =====================================================
# Utilità / Debug
# =====================================================
# Stampa a video il contesto env/profilo risolto
env-print:
	@echo "ARTIFACT_ID=$(ARTIFACT_ID)"
	@echo "SVC_NAME=$(SVC_NAME)"
	@echo "PROFILE=$(PROFILE)"
	@echo "ENV=$(ENV)"
	@echo "ENV_FILE=$(ENV_FILE)"
	@echo "IMAGE=$(IMAGE)"
	@echo "COMPOSE_FILE=$(COMPOSE_FILE)"

# Stampa la configurazione Compose risultante (utile per debug interpolazione variabili)
compose-config:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) config

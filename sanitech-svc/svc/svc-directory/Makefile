# =====================================================
# Makefile — Sanitech svc-directory
# =====================================================
# Convenzioni:
# - PROFILE: profilo Spring (es. local | remote | staging | prod)
# - ENV:     selettore del file env (di default coincide con PROFILE)
#
# Note sui file env:
# - infra/env/env.<ENV> deve essere in formato semplice KEY=VALUE (senza spazi non quotati).
# - Le variabili del file env vengono importate (include) ed esportate ai processi figli.
# =====================================================

ARTIFACT_ID ?= svc-directory
SVC_NAME    ?= $(ARTIFACT_ID)
PORT        ?= 8082

# Si preferisce Maven già installato sull'host per evitare il download del wrapper quando si è offline.
MVN ?= $(shell command -v mvn >/dev/null 2>&1 && echo mvn || echo ./mvnw)

# =====================================================
# Mapping profilo/env
# =====================================================
PROFILE ?= remote
ENV     ?= $(PROFILE)

# =====================================================
# Variabili di progetto esportate
# =====================================================
export ARTIFACT_ID
export SVC_NAME
export PROFILE
export ENV

# =====================================================
# File di configurazione
# =====================================================
# Full stack (include anche il microservizio)
COMPOSE_FILE        ?= infra/docker-compose.yml
# Infra-only (postgres/kafka/keycloak/etc.)
COMPOSE_INFRA_FILE  ?= infra/docker-compose.infra.yml

DOCKERFILE ?= infra/Dockerfile

# Tag immagine: sanitech/<svc-name>:<env>
IMAGE ?= sanitech/$(SVC_NAME):$(ENV)

# File env di default: infra/env/env.<env>
ENV_FILE ?= infra/env/env.$(ENV)

# =====================================================
# Path assoluti per evitare problemi con IDE e working dir diversi
# =====================================================
MAKEFILE_DIR         := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
ENV_FILE             := $(abspath $(ENV_FILE))
COMPOSE_FILE         := $(abspath $(COMPOSE_FILE))
COMPOSE_INFRA_FILE   := $(abspath $(COMPOSE_INFRA_FILE))
DOCKERFILE           := $(abspath $(DOCKERFILE))

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
include $(ENV_FILE)
export $(shell sed -n 's/^\([A-Za-z_][A-Za-z0-9_]*\)=.*/\1/p' $(ENV_FILE))

# =====================================================
# Selezione comando Docker Compose
# =====================================================
DOCKER_COMPOSE := $(shell command -v docker-compose >/dev/null 2>&1 && echo docker-compose || echo "docker compose")

.PHONY: help \
        build test run \
        docker-build docker-run \
        compose-up compose-down compose-config \
        infra-up infra-down infra-config \
        env-print

help:
	@echo "Target disponibili:"
	@echo "  build                 mvn clean package (skipTests)"
	@echo "  test                  mvn test"
	@echo "  run                   avvia Spring Boot su host (SPRING_PROFILES_ACTIVE=$(PROFILE))"
	@echo "  docker-build          build immagine microservizio (richiede build jar)"
	@echo "  docker-run            run immagine microservizio standalone"
	@echo ""
	@echo "  compose-up            avvia FULL stack (include $(ARTIFACT_ID)) via compose"
	@echo "  compose-down          stop + cleanup FULL stack (include volumi)"
	@echo "  compose-config        stampa config FULL stack risultante (debug)"
	@echo ""
	@echo "  infra-up              avvia solo infrastruttura via compose infra-only"
	@echo "  infra-down            stop + cleanup solo infrastruttura (include volumi)"
	@echo "  infra-config          stampa config infra-only risultante (debug)"
	@echo ""
	@echo "  env-print             stampa contesto risolto"

# =====================================================
# Maven (host)
# =====================================================
build:
	$(MVN) clean -DskipTests package

test:
	$(MVN) test

run:
	SPRING_PROFILES_ACTIVE=$(PROFILE) $(MVN) spring-boot:run

# =====================================================
# Docker (microservizio)
# =====================================================
docker-build: build
	docker build -f $(DOCKERFILE) -t $(IMAGE) .

docker-run:
	docker run --rm --env-file $(ENV_FILE) -p $(PORT):$(PORT) $(IMAGE)

# =====================================================
# Docker Compose — FULL stack (include microservizio)
# =====================================================
compose-up: build
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up -d --build

compose-down:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down -v --remove-orphans

compose-config:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) config

# =====================================================
# Docker Compose — INFRA ONLY (senza microservizio)
# =====================================================
infra-up:
	$(DOCKER_COMPOSE) -f $(COMPOSE_INFRA_FILE) --env-file $(ENV_FILE) up -d --build

infra-down:
	$(DOCKER_COMPOSE) -f $(COMPOSE_INFRA_FILE) --env-file $(ENV_FILE) down -v --remove-orphans

infra-config:
	$(DOCKER_COMPOSE) -f $(COMPOSE_INFRA_FILE) --env-file $(ENV_FILE) config

# =====================================================
# Utilità / Debug
# =====================================================
env-print:
	@echo "ARTIFACT_ID=$(ARTIFACT_ID)"
	@echo "SVC_NAME=$(SVC_NAME)"
	@echo "PROFILE=$(PROFILE)"
	@echo "ENV=$(ENV)"
	@echo "ENV_FILE=$(ENV_FILE)"
	@echo "IMAGE=$(IMAGE)"
	@echo "PORT=$(PORT)"
	@echo "COMPOSE_FILE=$(COMPOSE_FILE)"
	@echo "COMPOSE_INFRA_FILE=$(COMPOSE_INFRA_FILE)"
	@echo "DOCKERFILE=$(DOCKERFILE)"
	@echo "DOCKER_COMPOSE=$(DOCKER_COMPOSE)"

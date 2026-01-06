SVC_NAME ?= svc-directory
PORT ?= 8082
MVN ?= ./mvnw

# Default env
ENV ?= staging

COMPOSE_FILE ?= infra/docker-compose.yml
COMPOSE_INFRA_PORTS_FILE ?= infra/docker-compose.infra-ports.yml
DOCKERFILE ?= infra/Dockerfile
IMAGE ?= sanitech/$(SVC_NAME):$(ENV)

# Default env file
ENV_FILE ?= infra/env/env.$(ENV)

# =====================================================
# Path assoluti per evitare problemi con IDE e working dir diversi
# =====================================================
MAKEFILE_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
ENV_FILE := $(abspath $(ENV_FILE))
COMPOSE_FILE := $(abspath $(COMPOSE_FILE))
COMPOSE_INFRA_PORTS_FILE := $(abspath $(COMPOSE_INFRA_PORTS_FILE))

# Rende visibile ENV_FILE al docker-compose.yml
export ENV_FILE

# =====================================================
# Caricamento variabili da .env per comandi host (run/test)
# =====================================================
ifneq (,$(wildcard $(ENV_FILE)))
include $(ENV_FILE)
export $(shell sed -n 's/^\([A-Za-z_][A-Za-z0-9_]*\)=.*/\1/p' $(ENV_FILE))
endif

# =====================================================
# Docker Compose command resolver
# =====================================================
DOCKER_COMPOSE := $(shell command -v docker-compose >/dev/null 2>&1 && echo docker-compose || echo "docker compose")

.PHONY: build test run docker-build docker-run compose-up compose-up-infra compose-down compose-down-infra

# =====================================================
# Maven
# =====================================================
build:
	$(MVN) clean -DskipTests package

test:
	$(MVN) -q test

run:
	$(MVN) -q spring-boot:run

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
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up -d --build

# Avvio infrastruttura senza il microservizio svc-directory
compose-up-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) up -d postgres kafka keycloak prometheus grafana

# Stop + cleanup completo (include volumi)
compose-down:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down -v

# Stop + cleanup dell'infrastruttura
compose-down-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) down -v

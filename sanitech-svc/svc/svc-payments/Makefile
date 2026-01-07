.PHONY: build test run docker-build docker-run compose-up compose-up-infra compose-down compose-down-infra env-print compose-config

ARTIFACT_ID ?= svc-payments
SVC_NAME ?= $(ARTIFACT_ID)
PORT ?= 8090
# Prefer Maven già installato sull'host per evitare download del wrapper offline.
MVN ?= $(shell command -v mvn >/dev/null 2>&1 && echo mvn || echo ./mvnw)
COMPOSE_FILE ?= infra/docker-compose.yml
COMPOSE_INFRA_PORTS_FILE ?= infra/docker-compose.infra-ports.yml
DOCKERFILE ?= infra/Dockerfile
PROFILE ?= remote
ENV ?= $(PROFILE)
IMAGE ?= sanitech/$(SVC_NAME):$(ENV)
ENV_FILE ?= infra/env/env.$(ENV)

export ARTIFACT_ID
export SVC_NAME
export PROFILE
export ENV

MAKEFILE_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
ENV_FILE := $(abspath $(ENV_FILE))
COMPOSE_FILE := $(abspath $(COMPOSE_FILE))
COMPOSE_INFRA_PORTS_FILE := $(abspath $(COMPOSE_INFRA_PORTS_FILE))
export ENV_FILE

ifeq (,$(wildcard $(ENV_FILE)))
$(error Env file non trovato: $(ENV_FILE))
endif

include $(ENV_FILE)
export $(shell sed -n 's/^\([A-Za-z_][A-Za-z0-9_]*\)=.*/\1/p' $(ENV_FILE))

DOCKER_COMPOSE := $(shell command -v docker-compose >/dev/null 2>&1 && echo docker-compose || echo "docker compose")

build:
	$(MVN) clean -DskipTests package

test:
	$(MVN) test

run:
	$(MVN) spring-boot:run

docker-build: build
	docker build -f $(DOCKERFILE) -t $(IMAGE) .

docker-run:
	docker run --rm --env-file $(ENV_FILE) -p $(PORT):$(PORT) $(IMAGE)

compose-up: build
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) up -d --build

compose-up-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) up -d postgres kafka keycloak prometheus grafana

compose-down:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down -v

compose-down-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) down -v

env-print:
	@echo "ARTIFACT_ID=$(ARTIFACT_ID)"
	@echo "SVC_NAME=$(SVC_NAME)"
	@echo "PROFILE=$(PROFILE)"
	@echo "ENV=$(ENV)"
	@echo "ENV_FILE=$(ENV_FILE)"
	@echo "IMAGE=$(IMAGE)"
	@echo "COMPOSE_FILE=$(COMPOSE_FILE)"

compose-config:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) config

# Prefer Maven already installed on the host to avoid wrapper downloads when offline.
SVC_DIR ?= sanitech-svc
INFRA_DIR ?= infra/svc
MVN ?= $(shell command -v mvn >/dev/null 2>&1 && echo mvn || echo ./$(SVC_DIR)/mvnw)
POM ?= $(SVC_DIR)/pom.xml
MODULES ?=
PROFILE ?=
MAVEN_ARGS ?=

# =====================================================
# Docker Compose
# =====================================================
COMPOSE_FILE ?= $(INFRA_DIR)/docker-compose.yml
COMPOSE_INFRA_PORTS_FILE ?= $(INFRA_DIR)/docker-compose.infra-ports.yml
ENV ?= local
COMPOSE_ENV_FILE ?= $(INFRA_DIR)/env/env.$(ENV)
COMPOSE_INFRA_SERVICES ?= pg-directory pg-scheduling pg-admissions pg-consents pg-docs pg-notifications pg-audit pg-televisit pg-payments pg-prescribing kafka keycloak prometheus grafana minio mailhog

MAKEFILE_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
COMPOSE_FILE := $(abspath $(COMPOSE_FILE))
COMPOSE_INFRA_PORTS_FILE := $(abspath $(COMPOSE_INFRA_PORTS_FILE))
COMPOSE_ENV_FILE := $(abspath $(COMPOSE_ENV_FILE))

ifeq (,$(wildcard $(COMPOSE_ENV_FILE)))
$(error Env file non trovato: $(COMPOSE_ENV_FILE))
endif

DOCKER_COMPOSE := $(shell command -v docker-compose >/dev/null 2>&1 && echo docker-compose || echo "docker compose")

MODULE_SELECTOR = $(if $(strip $(MODULES)),-pl $(MODULES) -am,)
PROFILE_SELECTOR = $(if $(strip $(PROFILE)),-P $(PROFILE),)

.PHONY: build test verify clean docker-build docker-run compose-up compose-up-infra compose-down compose-down-infra compose-config env-print

# =====================================================
# Maven (aggregator)
# =====================================================
# I target Maven usano:
# - il profilo Maven definito in PROFILE
# - la selezione moduli definita in MODULES
build:
	$(MVN) -f $(POM) -T1C -DskipTests $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) clean package

test:
	$(MVN) -f $(POM) -T1C $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) test

verify:
	$(MVN) -f $(POM) -T1C $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) verify

clean:
	$(MVN) -f $(POM) $(MAVEN_ARGS) clean

# =====================================================
# Docker / Compose
# =====================================================
docker-build: build
	$(DOCKER_COMPOSE) --env-file $(COMPOSE_ENV_FILE) -f $(COMPOSE_FILE) build

docker-run:
	$(DOCKER_COMPOSE) --env-file $(COMPOSE_ENV_FILE) -f $(COMPOSE_FILE) up -d

compose-up: build
	$(DOCKER_COMPOSE) --env-file $(COMPOSE_ENV_FILE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) up -d --build

compose-up-infra:
	$(DOCKER_COMPOSE) --env-file $(COMPOSE_ENV_FILE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) up -d $(COMPOSE_INFRA_SERVICES)

compose-down:
	$(DOCKER_COMPOSE) --env-file $(COMPOSE_ENV_FILE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) down -v

compose-down-infra:
	$(DOCKER_COMPOSE) --env-file $(COMPOSE_ENV_FILE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) down -v

compose-config:
	$(DOCKER_COMPOSE) --env-file $(COMPOSE_ENV_FILE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) config

env-print:
	@echo "SVC_DIR=$(SVC_DIR)"
	@echo "INFRA_DIR=$(INFRA_DIR)"
	@echo "COMPOSE_FILE=$(COMPOSE_FILE)"
	@echo "COMPOSE_INFRA_PORTS_FILE=$(COMPOSE_INFRA_PORTS_FILE)"
	@echo "COMPOSE_ENV_FILE=$(COMPOSE_ENV_FILE)"
	@echo "ENV=$(ENV)"
	@echo "COMPOSE_INFRA_SERVICES=$(COMPOSE_INFRA_SERVICES)"

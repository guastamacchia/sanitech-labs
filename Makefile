# Prefer Maven already installed on the host to avoid wrapper downloads when offline.
SVC_DIR ?= sanitech-svc
INFRA_DIR ?= .infra/svc
MVN ?= $(shell command -v mvn >/dev/null 2>&1 && echo mvn || echo ./$(SVC_DIR)/mvnw)
POM ?= $(SVC_DIR)/pom.xml
MODULES ?=
PROFILE ?=
MAVEN_ARGS ?=

# =====================================================
# Servizi (singolo microservizio)
# =====================================================
SERVICE ?= svc-directory
ARTIFACT_ID ?= $(SERVICE)
SVC_NAME ?= $(ARTIFACT_ID)
PORT ?= 8082
SERVICE_PROFILE ?= remote
SERVICE_ENV ?= $(SERVICE_PROFILE)

SERVICE_DIR ?= $(SVC_DIR)/$(SERVICE)
SERVICE_COMPOSE_FILE ?= $(INFRA_DIR)/$(SERVICE)/docker-compose.yml
SERVICE_COMPOSE_INFRA_FILE ?= $(INFRA_DIR)/$(SERVICE)/docker-compose.infra.yml
SERVICE_ENV_FILE ?= $(INFRA_DIR)/$(SERVICE)/env/env.$(SERVICE_ENV)
SERVICE_DOCKERFILE ?= $(INFRA_DIR)/$(SERVICE)/Dockerfile
IMAGE ?= sanitech/$(SVC_NAME):$(SERVICE_ENV)

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
SERVICE_COMPOSE_FILE := $(abspath $(SERVICE_COMPOSE_FILE))
SERVICE_COMPOSE_INFRA_FILE := $(abspath $(SERVICE_COMPOSE_INFRA_FILE))
SERVICE_ENV_FILE := $(abspath $(SERVICE_ENV_FILE))
SERVICE_DOCKERFILE := $(abspath $(SERVICE_DOCKERFILE))

ifeq (,$(wildcard $(COMPOSE_ENV_FILE)))
$(error Env file non trovato: $(COMPOSE_ENV_FILE))
endif

ifeq (,$(wildcard $(SERVICE_ENV_FILE)))
$(error Env file non trovato: $(SERVICE_ENV_FILE))
endif

DOCKER_COMPOSE := $(shell command -v docker-compose >/dev/null 2>&1 && echo docker-compose || echo "docker compose")

MODULE_SELECTOR = $(if $(strip $(MODULES)),-pl $(MODULES) -am,)
PROFILE_SELECTOR = $(if $(strip $(PROFILE)),-P $(PROFILE),)
SERVICE_SELECTOR = -pl $(SERVICE) -am

# =====================================================
# Caricamento variabili dal file .env per comandi host (svc-*)
# =====================================================
include $(SERVICE_ENV_FILE)
export $(shell sed -n 's/^\([A-Za-z_][A-Za-z0-9_]*\)=.*/\1/p' $(SERVICE_ENV_FILE))

.PHONY: build test verify clean \
	docker-build docker-run compose-up compose-up-infra compose-down compose-down-infra compose-config \
	svc-build svc-test svc-run \
	svc-docker-build svc-docker-run \
	svc-compose-up svc-compose-down svc-compose-config \
	svc-infra-up svc-infra-down svc-infra-config \
	env-print help

export ARTIFACT_ID
export SVC_NAME
export SERVICE_PROFILE
export SERVICE_ENV

help:
	@echo "Target disponibili (root):"
	@echo "  build                 mvn clean package (skipTests) su aggregator"
	@echo "  test                  mvn test su aggregator"
	@echo "  verify                mvn verify su aggregator"
	@echo "  clean                 mvn clean su aggregator"
	@echo ""
	@echo "  compose-up            avvia FULL stack (.infra/svc/docker-compose.yml)"
	@echo "  compose-up-infra      avvia solo infra (stack globale)"
	@echo "  compose-down          stop + cleanup FULL stack"
	@echo "  compose-down-infra    stop + cleanup infra (stack globale)"
	@echo "  compose-config        stampa config stack globale"
	@echo ""
	@echo "Target disponibili (svc-*, SERVICE=$(SERVICE)):"
	@echo "  svc-build             mvn clean package (skipTests) sul servizio"
	@echo "  svc-test              mvn test sul servizio"
	@echo "  svc-run               avvia Spring Boot (SPRING_PROFILES_ACTIVE=$(SERVICE_PROFILE))"
	@echo "  svc-docker-build      build immagine microservizio"
	@echo "  svc-docker-run        run immagine microservizio"
	@echo "  svc-compose-up        avvia stack del servizio"
	@echo "  svc-compose-down      stop + cleanup stack del servizio"
	@echo "  svc-compose-config    stampa config stack del servizio"
	@echo "  svc-infra-up           avvia solo infra del servizio"
	@echo "  svc-infra-down         stop + cleanup infra del servizio"
	@echo "  svc-infra-config       stampa config infra del servizio"
	@echo ""
	@echo "  env-print             stampa contesto risolto"

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
# Maven (singolo servizio)
# =====================================================
svc-build:
	$(MVN) -f $(POM) -T1C -DskipTests $(SERVICE_SELECTOR) $(MAVEN_ARGS) clean package

svc-test:
	$(MVN) -f $(POM) -T1C $(SERVICE_SELECTOR) $(MAVEN_ARGS) test

svc-run:
	SPRING_PROFILES_ACTIVE=$(SERVICE_PROFILE) $(MVN) -f $(POM) $(SERVICE_SELECTOR) $(MAVEN_ARGS) spring-boot:run

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

# =====================================================
# Docker (singolo servizio)
# =====================================================
svc-docker-build: svc-build
	docker build -f $(SERVICE_DOCKERFILE) -t $(IMAGE) $(SERVICE_DIR)

svc-docker-run:
	docker run --rm --env-file $(SERVICE_ENV_FILE) -p $(PORT):$(PORT) $(IMAGE)

# =====================================================
# Docker Compose — stack servizio
# =====================================================
svc-compose-up: svc-build
	$(DOCKER_COMPOSE) -f $(SERVICE_COMPOSE_FILE) --env-file $(SERVICE_ENV_FILE) up -d --build

svc-compose-down:
	$(DOCKER_COMPOSE) -f $(SERVICE_COMPOSE_FILE) --env-file $(SERVICE_ENV_FILE) down -v --remove-orphans

svc-compose-config:
	$(DOCKER_COMPOSE) -f $(SERVICE_COMPOSE_FILE) --env-file $(SERVICE_ENV_FILE) config

# =====================================================
# Docker Compose — INFRA ONLY (servizio)
# =====================================================
svc-infra-up:
	$(DOCKER_COMPOSE) -f $(SERVICE_COMPOSE_INFRA_FILE) --env-file $(SERVICE_ENV_FILE) up -d --build

svc-infra-down:
	$(DOCKER_COMPOSE) -f $(SERVICE_COMPOSE_INFRA_FILE) --env-file $(SERVICE_ENV_FILE) down -v --remove-orphans

svc-infra-config:
	$(DOCKER_COMPOSE) -f $(SERVICE_COMPOSE_INFRA_FILE) --env-file $(SERVICE_ENV_FILE) config

env-print:
	@echo "SVC_DIR=$(SVC_DIR)"
	@echo "INFRA_DIR=$(INFRA_DIR)"
	@echo "COMPOSE_FILE=$(COMPOSE_FILE)"
	@echo "COMPOSE_INFRA_PORTS_FILE=$(COMPOSE_INFRA_PORTS_FILE)"
	@echo "COMPOSE_ENV_FILE=$(COMPOSE_ENV_FILE)"
	@echo "ENV=$(ENV)"
	@echo "COMPOSE_INFRA_SERVICES=$(COMPOSE_INFRA_SERVICES)"
	@echo "SERVICE=$(SERVICE)"
	@echo "SERVICE_DIR=$(SERVICE_DIR)"
	@echo "ARTIFACT_ID=$(ARTIFACT_ID)"
	@echo "SVC_NAME=$(SVC_NAME)"
	@echo "SERVICE_PROFILE=$(SERVICE_PROFILE)"
	@echo "SERVICE_ENV=$(SERVICE_ENV)"
	@echo "SERVICE_ENV_FILE=$(SERVICE_ENV_FILE)"
	@echo "SERVICE_COMPOSE_FILE=$(SERVICE_COMPOSE_FILE)"
	@echo "SERVICE_COMPOSE_INFRA_FILE=$(SERVICE_COMPOSE_INFRA_FILE)"
	@echo "SERVICE_DOCKERFILE=$(SERVICE_DOCKERFILE)"
	@echo "IMAGE=$(IMAGE)"
	@echo "PORT=$(PORT)"

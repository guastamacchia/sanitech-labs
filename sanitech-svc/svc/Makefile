# Prefer Maven already installed on the host to avoid wrapper downloads when offline.
MVN ?= $(shell command -v mvn >/dev/null 2>&1 && echo mvn || echo ./mvnw)
MODULES ?=
PROFILE ?=
MAVEN_ARGS ?=

# =====================================================
# Docker Compose
# =====================================================
COMPOSE_FILE ?= ../infra/docker-compose.yml
COMPOSE_INFRA_PORTS_FILE ?=
COMPOSE_INFRA_SERVICES ?= postgres kafka keycloak prometheus grafana

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
	$(MVN) -T1C -DskipTests $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) clean package

test:
	$(MVN) -T1C $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) test

verify:
	$(MVN) -T1C $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) verify

clean:
	$(MVN) $(MAVEN_ARGS) clean

# =====================================================
# Docker / Compose
# =====================================================
docker-build: build
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) build

docker-run:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) up -d

compose-up: build
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) up -d --build

compose-up-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) up -d $(COMPOSE_INFRA_SERVICES)

compose-down:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) down -v

compose-down-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) down -v

compose-config:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) $(if $(strip $(COMPOSE_INFRA_PORTS_FILE)),-f $(COMPOSE_INFRA_PORTS_FILE),) config

env-print:
	@echo "COMPOSE_FILE=$(COMPOSE_FILE)"
	@echo "COMPOSE_INFRA_PORTS_FILE=$(COMPOSE_INFRA_PORTS_FILE)"
	@echo "COMPOSE_INFRA_SERVICES=$(COMPOSE_INFRA_SERVICES)"

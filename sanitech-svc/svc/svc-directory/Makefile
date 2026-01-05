SVC_NAME ?= svc-directory
PORT ?= 8082
MVN ?= ./mvnw

COMPOSE_FILE ?= infra/docker-compose.yml
COMPOSE_INFRA_PORTS_FILE ?= infra/docker-compose.infra-ports.yml
DOCKERFILE ?= infra/Dockerfile
IMAGE ?= sanitech/$(SVC_NAME):local
ENV_FILE ?= .env.local

ifneq (,$(wildcard $(ENV_FILE)))
include $(ENV_FILE)
export $(shell sed -n 's/^\([A-Za-z_][A-Za-z0-9_]*\)=.*/\1/p' $(ENV_FILE))
endif

# Se presente, viene utilizzato Docker Compose v2 (`docker compose`);
# in alternativa viene utilizzato il comando legacy `docker-compose`.
DOCKER_COMPOSE := $(shell command -v docker-compose >/dev/null 2>&1 && echo docker-compose || echo "docker compose")

.PHONY: build test run docker-build docker-run compose-up compose-up-infra compose-down

build:
	$(MVN) clean -DskipTests package

test:
	$(MVN) -q test

run:
	$(MVN) -q spring-boot:run

# Build dell'immagine del microservizio usando il Dockerfile sotto infra/
docker-build: build
	docker build -f $(DOCKERFILE) -t $(IMAGE) .

# Run standalone dell'immagine (utile per smoke test rapido)
docker-run:
	docker run --rm -p $(PORT):$(PORT) $(IMAGE)

# Avvio stack completo (database, Kafka, Keycloak, svc, osservabilità)
compose-up: build
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up -d --build

# Avvio infrastruttura senza il microservizio svc-directory
compose-up-infra:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -f $(COMPOSE_INFRA_PORTS_FILE) --env-file $(ENV_FILE) up -d postgres zookeeper kafka keycloak prometheus grafana

# Stop + cleanup completo (include volumi)
compose-down:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) down -v

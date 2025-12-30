SVC_NAME ?= svc-consents
PORT ?= 8085
MVN ?= ./mvnw
COMPOSE_FILE ?= docker-compose.dev.yml
RUN_OPTS ?=

.PHONY: build test run docker-build docker-run compose-up compose-down

build:
	$(MVN) -q clean -DskipTests package

test:
	$(MVN) -q test

run:
	$(MVN) -q spring-boot:run $(RUN_OPTS)

docker-build: build
	docker build -t sanitech/$(SVC_NAME):local .

docker-run:
	docker run --rm -p $(PORT):$(PORT) sanitech/$(SVC_NAME):local

compose-up: build
	docker compose -f $(COMPOSE_FILE) up -d --build

compose-down:
	docker compose -f $(COMPOSE_FILE) down -v

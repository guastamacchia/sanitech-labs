SVC_NAME ?= svc-scheduling
PORT ?= 8083
MVN ?= ./mvnw
COMPOSE_FILE ?= infra/docker-compose.yml
RUN_OPTS ?= -Dspring-boot.run.profiles=local

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

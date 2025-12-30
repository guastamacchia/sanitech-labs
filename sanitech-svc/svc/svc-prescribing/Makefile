SVC_NAME ?= svc-prescribing
PORT ?= 8092
MVN ?= ./mvnw

.PHONY: build test run docker-build docker-run compose-up compose-down helm-lint

build:
	$(MVN) -q -DskipTests package

test:
	$(MVN) -q test

run:
	$(MVN) -q spring-boot:run

docker-build:
	docker build -t sanitech/$(SVC_NAME):local .

docker-run:
	docker run --rm -p $(PORT):$(PORT) sanitech/$(SVC_NAME):local

compose-up:
	docker compose -f docker-compose.dev.yml up -d

compose-down:
	docker compose -f docker-compose.dev.yml down -v

helm-lint:
	helm lint helm/$(SVC_NAME)

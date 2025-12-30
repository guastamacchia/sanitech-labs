SVC_NAME ?= svc-directory
PORT ?= 8082
MVN ?= ./mvnw

.PHONY: build test run docker-build docker-run compose-up compose-down

build:
	$(MVN) clean -DskipTests package

test:
	$(MVN) -q test

run:
	$(MVN) -q spring-boot:run

docker-build: build
	docker build -f docker/Dockerfile.svc -t sanitech/$(SVC_NAME):local .

docker-run:
	docker run --rm -p $(PORT):$(PORT) sanitech/$(SVC_NAME):local

compose-up: build
	docker-compose -f docker/docker-compose.yml --env-file .env.local up -d --build

compose-down:
	docker-compose -f docker/docker-compose.yml down -v

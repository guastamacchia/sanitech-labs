APP=svc-scheduling

build:
	./mvnw -q -DskipTests package || mvn -q -DskipTests package

test:
	./mvnw -q test || mvn -q test

run:
	./mvnw -q spring-boot:run -Dspring-boot.run.profiles=local || mvn -q spring-boot:run -Dspring-boot.run.profiles=local

docker:
	docker build -t sanitech/$(APP):local .

compose-up:
	docker compose -f docker-compose.dev.yml up -d --build

compose-down:
	docker compose -f docker-compose.dev.yml down -v

.PHONY: build test run docker compose-up compose-down

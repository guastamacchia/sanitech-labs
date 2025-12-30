\.PHONY: build test run docker-up docker-down

build:
	./mvnw -q -DskipTests package || mvn -q -DskipTests package

test:
	./mvnw -q test || mvn -q test

run:
	./mvnw -q spring-boot:run || mvn -q spring-boot:run

docker-up:
	docker compose up -d --build

docker-down:
	docker compose down -v

.PHONY: build test run docker

build:
	./mvnw -q -DskipTests package || mvn -q -DskipTests package

test:
	./mvnw -q test || mvn -q test

run:
	./mvnw -q spring-boot:run || mvn -q spring-boot:run

docker:
	docker build -t sanitech/svc-gateway:local .

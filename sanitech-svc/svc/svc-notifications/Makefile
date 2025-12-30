SVC_NAME ?= svc-notifications

.PHONY: build test run docker docker-run

build:
	./mvnw -q -DskipTests package

test:
	./mvnw -q test

run:
	./mvnw -q spring-boot:run

docker:
	docker build -t $(SVC_NAME):local .

docker-run:
	docker run --rm -p 8087:8087 --name $(SVC_NAME) $(SVC_NAME):local

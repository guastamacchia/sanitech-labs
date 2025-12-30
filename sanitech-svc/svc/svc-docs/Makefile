.PHONY: build test run docker-build

build:
	./mvnw -B -DskipTests package

test:
	./mvnw -B test

run:
	./mvnw -B spring-boot:run

docker-build:
	docker build -t sanitech/svc-docs:local .

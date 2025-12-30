.PHONY: build test run docker

build:
	./mvnw -q -DskipTests package

test:
	./mvnw -q test

run:
	./mvnw -q spring-boot:run

docker:
	docker build -t sanitech/svc-admissions:local .

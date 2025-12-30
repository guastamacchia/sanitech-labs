.PHONY: build test run docker helm-lint

build:
	./mvnw -q -DskipTests package

test:
	./mvnw -q test

run:
	./mvnw -q spring-boot:run

docker:
	docker build -t sanitech/svc-admissions:local .

helm-lint:
	helm lint helm/svc-admissions

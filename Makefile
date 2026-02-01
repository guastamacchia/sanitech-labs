SVC_DIR ?= sanitech-svc
INFRA_DIR ?= .infra
ENV_DIR ?= .infra/env
MVN ?= $(shell command -v mvn >/dev/null 2>&1 && echo mvn || echo ./$(SVC_DIR)/mvnw)
POM ?= $(SVC_DIR)/pom.xml
MODULE ?=
MODULES ?=
PROFILE ?=
MAVEN_ARGS ?=
ENV ?= local

# =====================================================
# Servizi (singolo microservizio)
# =====================================================
SERVICE ?= svc-directory
SERVICE_PROFILE ?= $(ENV)

# =====================================================
# Docker Compose
# =====================================================
COMPOSE_FILE ?= $(INFRA_DIR)/docker-compose.yml
ENV_FILE ?= $(ENV_DIR)/env.$(ENV)
COMPOSE_INFRA_SERVICES ?= pg-directory pg-scheduling pg-admissions pg-consents pg-docs pg-notifications pg-audit pg-televisit pg-payments pg-prescribing kafka keycloak prometheus grafana minio mailhog
COMPOSE_BUILD_DEFAULT := $(shell command -v docker >/dev/null 2>&1 && echo 1 || echo 0)
COMPOSE_BUILD ?= $(COMPOSE_BUILD_DEFAULT)

COMPOSE_FILE := $(abspath $(COMPOSE_FILE))
ENV_FILE := $(abspath $(ENV_FILE))

ifeq (,$(wildcard $(ENV_FILE)))
$(error Env file non trovato: $(ENV_FILE))
endif

DOCKER_COMPOSE := $(shell command -v docker-compose >/dev/null 2>&1 && echo docker-compose || echo "docker compose")
COMPOSE_BUILD_FLAG := $(if $(filter 1 true yes,$(COMPOSE_BUILD)),--build,--no-build)

EFFECTIVE_MODULES := $(strip $(if $(MODULE),$(MODULE),$(MODULES)))
MODULE_SELECTOR = $(if $(EFFECTIVE_MODULES),-pl $(EFFECTIVE_MODULES) -am,)
PROFILE_SELECTOR = $(if $(strip $(PROFILE)),-P $(PROFILE),)
SERVICE_SELECTOR = -pl $(SERVICE) -am

# =====================================================
# Caricamento variabili dal file .env per comandi host (svc-*)
# =====================================================
include $(ENV_FILE)
export $(shell sed -n 's/^\([A-Za-z_][A-Za-z0-9_]*\)=.*/\1/p' $(ENV_FILE))

# =====================================================
# Mapping variabili environment specifiche per servizio
# =====================================================
SERVICE_PREFIX := $(strip $(if $(filter svc-admissions,$(SERVICE)),ADMISSIONS,\
  $(if $(filter svc-audit,$(SERVICE)),AUDIT,\
  $(if $(filter svc-consents,$(SERVICE)),CONSENTS,\
  $(if $(filter svc-directory,$(SERVICE)),DIRECTORY,\
  $(if $(filter svc-docs,$(SERVICE)),DOCS,\
  $(if $(filter svc-gateway,$(SERVICE)),GATEWAY,\
  $(if $(filter svc-notifications,$(SERVICE)),NOTIFICATIONS,\
  $(if $(filter svc-payments,$(SERVICE)),PAYMENTS,\
  $(if $(filter svc-prescribing,$(SERVICE)),PRESCRIBING,\
  $(if $(filter svc-scheduling,$(SERVICE)),SCHEDULING,\
  $(if $(filter svc-televisit,$(SERVICE)),TELEVISIT,))))))))))))

SERVICE_ENV_KEYS := ADMISSIONS_URL AUDIT_URL CLUSTER_ID CONSENTS_BASE_URL CONSENTS_URL \
  DATABASE_HOST DATABASE_NAME DATABASE_PASSWORD DATABASE_PORT DATABASE_USER \
  DB_URL DIRECTORY_URL DOCS_URL GF_SECURITY_ADMIN_PASSWORD GF_SECURITY_ADMIN_USER GRAFANA_URL \
  KAFKA_ADVERTISED_HOST KAFKA_HOST KAFKA_PORT KAFKA_PRODUCER_ACKS \
  KAFKA_PRODUCER_LINGER_MS KAFKA_PRODUCER_RETRIES KC_HEALTH_ENABLED KC_BOOTSTRAP_ADMIN_USERNAME \
  KC_BOOTSTRAP_ADMIN_PASSWORD KEYCLOAK_URL LIVEKIT_API_KEY LIVEKIT_API_SECRET LIVEKIT_CONSOLE_URL \
  LIVEKIT_URL MAILHOG_URL MAILPIT_UI_AUTH MAIL_FROM MAIL_HOST MAIL_PORT MINIO_ROOT_PASSWORD \
  MINIO_ROOT_USER MINIO_URL NOTIFICATIONS_URL OAUTH2_HOST OAUTH2_ISSUER_URI OAUTH2_PORT \
  OAUTH2_REALM OAUTH2_SCHEME OPENAPI_ADMISSIONS_URL OPENAPI_AUDIT_URL OPENAPI_CONSENTS_URL \
  OPENAPI_DIRECTORY_URL OPENAPI_DOCS_URL OPENAPI_NOTIFICATIONS_URL OPENAPI_PAYMENTS_URL \
  OPENAPI_PRESCRIBING_URL OPENAPI_SCHEDULING_URL OPENAPI_TELEVISIT_URL PAYMENTS_URL \
  PRESCRIBING_URL PROMETHEUS_URL RESILIENCE4J_BULKHEAD_INSTANCES_DIRECTORYREAD_MAXCONCURRENTCALLS \
  RESILIENCE4J_BULKHEAD_INSTANCES_DIRECTORYREAD_MAXWAITDURATION \
  RESILIENCE4J_RATELIMITER_INSTANCES_DIRECTORYAPI_LIMITFORPERIOD \
  RESILIENCE4J_RATELIMITER_INSTANCES_DIRECTORYAPI_LIMITREFRESHPERIOD \
  RESILIENCE4J_RATELIMITER_INSTANCES_DIRECTORYAPI_TIMEOUTDURATION S3_ACCESS_KEY S3_BUCKET \
  S3_ENDPOINT S3_REGION S3_SECRET_KEY SANITECH_PAYMENTS_WEBHOOK_SECRET SCHEDULING_URL \
  SERVICE_URL SPRING_PROFILES_ACTIVE TELEVISIT_URL

$(foreach key,$(SERVICE_ENV_KEYS),$(eval $(key) := $($(SERVICE_PREFIX)_$(key))))
export $(SERVICE_ENV_KEYS)

.PHONY: build test verify clean \
	compose-up compose-up-infra compose-down compose-down-infra compose-config \
	svc-build svc-test svc-run \
	env-print help

export SERVICE_PROFILE

help:
	@echo "Target disponibili (root):"
	@echo "  build                 mvn clean package (skipTests) su aggregator"
	@echo "  test                  mvn test su aggregator"
	@echo "  verify                mvn verify su aggregator"
	@echo "  clean                 mvn clean su aggregator"
	@echo "  MODULE=<mod>          seleziona un singolo modulo backend"
	@echo "  MODULES=<mod1,mod2>   seleziona moduli backend multipli"
	@echo "  COMPOSE_BUILD=0       disabilita build immagini compose (auto: $(COMPOSE_BUILD_DEFAULT))"
	@echo ""
	@echo "  compose-up            avvia FULL stack (.infra/docker-compose.yml)"
	@echo "  compose-up-infra      avvia solo infra (stack globale)"
	@echo "  compose-down          stop + cleanup FULL stack"
	@echo "  compose-down-infra    stop + cleanup infra (stack globale)"
	@echo "  compose-config        stampa config stack globale"
	@echo ""
	@echo "Target disponibili (svc-*, SERVICE=$(SERVICE)):"
	@echo "  svc-build             mvn clean package (skipTests) sul servizio"
	@echo "  svc-test              mvn test sul servizio"
	@echo "  svc-run               avvia Spring Boot (SPRING_PROFILES_ACTIVE=$(SERVICE_PROFILE))"
	@echo ""
	@echo "  env-print             stampa contesto risolto"

# =====================================================
# Maven (aggregator)
# =====================================================
# I target Maven usano:
# - il profilo Maven definito in PROFILE
# - la selezione moduli definita in MODULES
build:
	$(MVN) -f $(POM) -T1C -DskipTests $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) clean package

test:
	$(MVN) -f $(POM) -T1C $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) test

verify:
	$(MVN) -f $(POM) -T1C $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) verify

clean:
	$(MVN) -f $(POM) $(MAVEN_ARGS) clean

# =====================================================
# Maven (singolo servizio)
# =====================================================
svc-build:
	$(MVN) -f $(POM) -T1C -DskipTests $(SERVICE_SELECTOR) $(MAVEN_ARGS) clean package

svc-test:
	$(MVN) -f $(POM) -T1C $(SERVICE_SELECTOR) $(MAVEN_ARGS) test

svc-run:
	SPRING_PROFILES_ACTIVE=$(SERVICE_PROFILE) $(MVN) -f $(POM) $(SERVICE_SELECTOR) $(MAVEN_ARGS) spring-boot:run

# =====================================================
# Docker Compose
# =====================================================
compose-up: build
	@set -a; . $(ENV_FILE); set +a; $(DOCKER_COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) up -d $(COMPOSE_BUILD_FLAG)

compose-up-infra:
	@set -a; . $(ENV_FILE); set +a; $(DOCKER_COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) up -d $(COMPOSE_BUILD_FLAG) $(COMPOSE_INFRA_SERVICES)

compose-down:
	$(DOCKER_COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) down -v

compose-down-infra:
	$(DOCKER_COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) down -v

compose-config:
	@set -a; . $(ENV_FILE); set +a; $(DOCKER_COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) config

env-print:
	@echo "SVC_DIR=$(SVC_DIR)"
	@echo "INFRA_DIR=$(INFRA_DIR)"
	@echo "COMPOSE_FILE=$(COMPOSE_FILE)"
	@echo "ENV=$(ENV)"
	@echo "ENV_FILE=$(ENV_FILE)"
	@echo "COMPOSE_INFRA_SERVICES=$(COMPOSE_INFRA_SERVICES)"
	@echo "MODULE=$(MODULE)"
	@echo "MODULES=$(MODULES)"
	@echo "SERVICE=$(SERVICE)"
	@echo "SERVICE_PROFILE=$(SERVICE_PROFILE)"

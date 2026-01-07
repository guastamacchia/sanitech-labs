MVN ?= ./mvnw
MODULES ?=
PROFILE ?=
MAVEN_ARGS ?=

MODULE_SELECTOR = $(if $(strip $(MODULES)),-pl $(MODULES) -am,)
PROFILE_SELECTOR = $(if $(strip $(PROFILE)),-P $(PROFILE),)

.PHONY: build test clean verify

build:
	$(MVN) -T1C -DskipTests $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) clean package

test:
	$(MVN) -T1C $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) test

verify:
	$(MVN) -T1C $(MODULE_SELECTOR) $(PROFILE_SELECTOR) $(MAVEN_ARGS) verify

clean:
	$(MVN) $(MAVEN_ARGS) clean

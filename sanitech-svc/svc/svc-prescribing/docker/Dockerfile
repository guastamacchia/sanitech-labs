# =======================
# Build stage
# =======================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) Copia solo i file necessari al build per massimizzare la cache Docker
COPY pom.xml mvnw ./
COPY .mvn .mvn

RUN chmod +x mvnw
RUN ./mvnw -q -B -Dmaven.test.skip=true dependency:go-offline

# 2) Copia sorgenti e compila (senza test)
COPY src src
RUN ./mvnw -q -B -DskipTests package

# =======================
# Runtime stage
# =======================
FROM eclipse-temurin:21-jre

# 3) Esecuzione con utente non privilegiato (hardening base container)
RUN useradd -r -u 1001 -g root sanitech &&     mkdir -p /app &&     chown -R sanitech:root /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

USER sanitech

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

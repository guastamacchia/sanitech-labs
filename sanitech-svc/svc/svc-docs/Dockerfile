# =======================
# Build stage
# =======================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia solo i file di build per sfruttare la cache Docker sui download Maven
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q -B -Dmaven.test.skip=true dependency:go-offline

# Copia il sorgente e compila (senza test)
COPY src src
RUN ./mvnw -q -B -DskipTests package

# =======================
# Runtime stage
# =======================
FROM eclipse-temurin:21-jre

# Crea un utente non privilegiato per eseguire l'applicazione
RUN useradd -r -u 1001 -g root sanitech && mkdir -p /app && chown -R sanitech:root /app

# Opzioni JVM di default (override via env in docker-compose)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

USER sanitech

EXPOSE 8086
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

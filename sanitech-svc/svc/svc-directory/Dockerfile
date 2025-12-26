# =======================
# Build stage
# =======================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 1) Copia i POM e il wrapper Maven (parent + modulo) per preparare la cache
COPY pom.xml ./pom.xml
COPY svc-directory/pom.xml svc-directory/mvnw ./svc-directory/
COPY svc-directory/.mvn ./svc-directory/.mvn

WORKDIR /app/svc-directory
RUN chmod +x mvnw
RUN ./mvnw -q -B -Dmaven.test.skip=true dependency:go-offline

# 2) Copia i sorgenti del modulo e compila (senza test)
COPY svc-directory/src ./src
RUN ./mvnw -q -B -DskipTests package

# =======================
# Runtime stage
# =======================
FROM eclipse-temurin:21-jre

# 3) Esecuzione con utente non privilegiato (hardening base container)
RUN useradd -r -u 1001 -g root sanitech &&     mkdir -p /app &&     chown -R sanitech:root /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

WORKDIR /app
COPY --from=build /app/svc-directory/target/*.jar app.jar

USER sanitech

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

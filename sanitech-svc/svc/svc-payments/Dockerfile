# =======================
# Build stage
# =======================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache Maven deps
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q -B -Dmaven.test.skip=true dependency:go-offline || mvn -q -B -Dmaven.test.skip=true dependency:go-offline

# Build
COPY src src
RUN ./mvnw -q -B -DskipTests package || mvn -q -B -DskipTests package

# =======================
# Runtime stage
# =======================
FROM eclipse-temurin:21-jre

RUN useradd -r -u 1001 -g root sanitech \
  && mkdir -p /app \
  && chown -R sanitech:root /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
USER sanitech

EXPOSE 8091
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

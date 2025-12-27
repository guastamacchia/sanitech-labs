# Runtime-only image: assumes the JAR is already built at svc-directory/target/
FROM eclipse-temurin:21-jre

# Esecuzione con utente non privilegiato
RUN useradd -r -u 1001 -g root sanitech && \
    mkdir -p /app && \
    chown -R sanitech:root /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

WORKDIR /app

# Copia del JAR già compilato (build esterna)
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

USER sanitech

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

# =======================
# Runtime stage
# =======================
FROM eclipse-temurin:25-jdk-jammy

# (3) Utente non privilegiato per esecuzione in produzione (best practice security).
RUN useradd -r -u 1001 -g root sanitech   && mkdir -p /app   && chown -R sanitech:root /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

WORKDIR /app
COPY target/*.jar app.jar

USER sanitech

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

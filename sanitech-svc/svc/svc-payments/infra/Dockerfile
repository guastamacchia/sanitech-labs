# Immagine base Java Runtime (JRE) 21, distribuzione Eclipse Temurin.
# Include solo il runtime necessario per eseguire l'applicazione.
FROM eclipse-temurin:21-jre

# -----------------------------------------------------
# Parametri di runtime
# -----------------------------------------------------
# Questi ARG permettono di configurare l'utente applicativo.
ARG APP_USER=sanitech
ARG APP_UID=1001
ARG APP_GID=0

# -----------------------------------------------------
# Utente non privilegiato
# -----------------------------------------------------
# L'applicazione non viene eseguita come root per ridurre la superficie di attacco.
RUN useradd -r -u ${APP_UID} -g ${APP_GID} ${APP_USER} \
    && mkdir -p /app \
    && chown ${APP_UID}:${APP_GID} /app

# -----------------------------------------------------
# Opzioni JVM container-friendly
# -----------------------------------------------------
# JDK_JAVA_OPTIONS è riconosciuta automaticamente dalla JVM.
# Le opzioni qui definite:
# - limitano l'uso della memoria in base al container
# - forzano l'uscita del processo in caso di OOM
# - garantiscono encoding UTF-8 coerente
#
# Possono essere estese o sovrascritte a runtime
# tramite JAVA_TOOL_OPTIONS o variabili d'ambiente.
ENV JDK_JAVA_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

# -----------------------------------------------------
# Directory di lavoro
# -----------------------------------------------------
WORKDIR /app

# -----------------------------------------------------
# Artefatto applicativo
# -----------------------------------------------------
ARG JAR_FILE=target/*.jar
COPY --chown=${APP_UID}:${APP_GID} ${JAR_FILE} app.jar

# -----------------------------------------------------
# Utente runtime
# -----------------------------------------------------
USER ${APP_UID}:${APP_GID}

# -----------------------------------------------------
# Porta applicativa
# -----------------------------------------------------
EXPOSE 8090

# -----------------------------------------------------
# Avvio dell'applicazione
# -----------------------------------------------------
ENTRYPOINT ["java","-jar","/app/app.jar"]

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
# Tutti i comandi successivi vengono eseguiti in /app.
WORKDIR /app

# -----------------------------------------------------
# Artefatto applicativo
# -----------------------------------------------------
# Il JAR viene compilato esternamente (es. mvn package)
# e copiato all'interno dell'immagine durante la build.
#
# Il parametro JAR_FILE consente di specificare quale
# artefatto copiare.
ARG JAR_FILE=target/*.jar
COPY --chown=${APP_UID}:${APP_GID} ${JAR_FILE} app.jar

# -----------------------------------------------------
# Utente runtime
# -----------------------------------------------------
# Da questo punto in poi il container viene eseguito
# con l'utente non privilegiato definito sopra.
USER ${APP_UID}:${APP_GID}

# -----------------------------------------------------
# Porta applicativa
# -----------------------------------------------------
# Porta TCP su cui il servizio espone le API HTTP.
# EXPOSE ha valore documentale e informativo.
EXPOSE 8086

# -----------------------------------------------------
# Avvio dell'applicazione
# -----------------------------------------------------
# Il processo Java viene eseguito come PID 1,
# consentendo una corretta gestione dei segnali
# (SIGTERM, SIGINT) e uno shutdown pulito.
ENTRYPOINT ["java","-jar","/app/app.jar"]

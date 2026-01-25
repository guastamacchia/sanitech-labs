FROM quay.io/keycloak/keycloak:26.0.7

# -----------------------------------------------------
# Import automatico del realm
# -----------------------------------------------------
# Crea la directory standard utilizzata da Keycloak
# per l'import automatico dei realm all'avvio.
#
# I file presenti in /opt/keycloak/data/import
# vengono caricati automaticamente quando si avvia
# Keycloak con l'opzione --import-realm.
RUN mkdir -p /opt/keycloak/data/import

# Copia i file JSON del realm all'interno dell'immagine.
# Questo consente di avere un Identity Provider
# già configurato al primo avvio, senza operazioni manuali.
COPY realm-export /opt/keycloak/data/import

# -----------------------------------------------------
# Avvio di Keycloak
# -----------------------------------------------------
# Avvia Keycloak in modalità "dev":
# - server HTTP sulla porta 8080
# - import automatico del realm configurato
# - hostname-strict disabilitato (utile in ambienti Docker)
ENTRYPOINT ["/opt/keycloak/bin/kc.sh","start-dev","--http-port=8080","--import-realm","--hostname-strict=false"]

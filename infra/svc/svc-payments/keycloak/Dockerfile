FROM quay.io/keycloak/keycloak:26.0.7

# -----------------------------------------------------
# Import automatico del realm
# -----------------------------------------------------
# Crea la directory standard utilizzata da Keycloak
# per l'import automatico dei realm all'avvio.
RUN mkdir -p /opt/keycloak/data/import

# Copia il file JSON del realm all'interno dell'immagine.
COPY sanitech-realm.json /opt/keycloak/data/import/sanitech-realm.json

# -----------------------------------------------------
# Avvio di Keycloak
# -----------------------------------------------------
ENTRYPOINT ["/opt/keycloak/bin/kc.sh","start-dev","--http-port=8080","--import-realm","--hostname-strict=false"]

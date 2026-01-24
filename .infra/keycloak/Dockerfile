FROM quay.io/keycloak/keycloak:26.0.7

# -----------------------------------------------------
# Import automatico del realm
# -----------------------------------------------------
# Crea la directory standard utilizzata da Keycloak
# per l'import automatico dei realm all'avvio.
# I file presenti in /opt/keycloak/data/import
# vengono caricati automaticamente con l'opzione --import-realm.
RUN mkdir -p /opt/keycloak/data/import

# Copia i file JSON del realm all'interno dell'immagine.
# Questo consente di avere un Identity Provider
# già configurato al primo avvio, senza operazioni manuali.
COPY realm-export /opt/keycloak/data/import

# Keep configuration runtime driven so credentials can be overridden via compose.

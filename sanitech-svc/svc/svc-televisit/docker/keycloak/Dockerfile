FROM quay.io/keycloak/keycloak:26.0.7

# Copia il realm da importare all'avvio
RUN mkdir -p /opt/keycloak/data/import
COPY sanitech-realm.json /opt/keycloak/data/import/sanitech-realm.json

# Default: avvio in dev mode e import del realm
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--http-port=8080", "--import-realm", "--hostname-strict=false"]

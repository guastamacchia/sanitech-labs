FROM quay.io/keycloak/keycloak:25.0

# Copy pre-seeded realm exports to speed up local setup.
COPY realm-export /opt/keycloak/data/import

# Keep configuration runtime driven so credentials can be overridden via compose.

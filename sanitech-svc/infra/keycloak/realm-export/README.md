# Keycloak realm exports

Place JSON exports for the realms you want to load during startup in this directory. The `docker-compose.yml` mounts the folder into `/opt/keycloak/data/import` and the Keycloak container is started with `--import-realm` to automatically import them at boot.

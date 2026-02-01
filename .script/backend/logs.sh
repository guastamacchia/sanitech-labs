#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INFRA_DIR="${INFRA_DIR:-${ROOT_DIR}/.infra/svc}"
COMPOSE_FILE="${COMPOSE_FILE:-${INFRA_DIR}/docker-compose.yml}"
COMPOSE="docker compose"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker non trovato. Impossibile mostrare i log." >&2
  exit 1
fi

if [ ! -f "${COMPOSE_FILE}" ]; then
  echo "File docker-compose non trovato in ${COMPOSE_FILE}" >&2
  exit 1
fi

services=("$@")
if [ ${#services[@]} -eq 0 ]; then
  services=(keycloak api-gateway kafka prometheus grafana)
fi

echo "[logs] seguo servizi: ${services[*]}"
${COMPOSE} -f "${COMPOSE_FILE}" logs -f "${services[@]}"

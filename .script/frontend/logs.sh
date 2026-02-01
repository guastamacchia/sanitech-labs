#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INFRA_DIR="${INFRA_DIR:-${ROOT_DIR}/.infra/fe}"
COMPOSE_FILE="${COMPOSE_FILE:-${INFRA_DIR}/docker-compose.yml}"
COMPOSE="docker compose"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker non trovato. Impossibile mostrare i log frontend." >&2
  exit 1
fi

if [ ! -f "${COMPOSE_FILE}" ]; then
  echo "File docker-compose non trovato in ${COMPOSE_FILE}" >&2
  exit 1
fi

services=("$@")
if [ ${#services[@]} -eq 0 ]; then
  services=(sanitech-fe)
fi

echo "[frontend:logs] seguo servizi: ${services[*]}"
${COMPOSE} -f "${COMPOSE_FILE}" logs -f "${services[@]}"

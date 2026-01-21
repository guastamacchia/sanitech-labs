#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/infra/docker-compose.yml}"
COMPOSE="docker compose"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker non trovato. Installa Docker per avviare il frontend." >&2
  exit 1
fi

if [ ! -f "${COMPOSE_FILE}" ]; then
  echo "File docker-compose non trovato in ${COMPOSE_FILE}" >&2
  exit 1
fi

echo "[frontend:up] usando compose file: ${COMPOSE_FILE}"
${COMPOSE} -f "${COMPOSE_FILE}" pull
${COMPOSE} -f "${COMPOSE_FILE}" up -d "$@"

echo "[frontend:up] servizi attivi:"
${COMPOSE} -f "${COMPOSE_FILE}" ps

#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INFRA_DIR="${INFRA_DIR:-${ROOT_DIR}/.infra/svc}"
COMPOSE_FILE="${COMPOSE_FILE:-${INFRA_DIR}/docker-compose.yml}"
COMPOSE="docker compose"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker non trovato. Installa Docker per avviare lo stack." >&2
  exit 1
fi

if [ ! -f "${COMPOSE_FILE}" ]; then
  echo "File docker-compose non trovato in ${COMPOSE_FILE}" >&2
  exit 1
fi

echo "[up] usando compose file: ${COMPOSE_FILE}"
${COMPOSE} -f "${COMPOSE_FILE}" pull
${COMPOSE} -f "${COMPOSE_FILE}" up -d "$@"

echo "[up] stack avviato. Servizi attivi:"
${COMPOSE} -f "${COMPOSE_FILE}" ps

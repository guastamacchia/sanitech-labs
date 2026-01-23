#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INFRA_DIR="${INFRA_DIR:-${ROOT_DIR}/.infra/fe}"
COMPOSE_FILE="${COMPOSE_FILE:-${INFRA_DIR}/docker-compose.yml}"
COMPOSE="docker compose"
REMOVE_VOLUMES="${REMOVE_VOLUMES:-false}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker non trovato. Nulla da fermare (frontend)." >&2
  exit 1
fi

if [ ! -f "${COMPOSE_FILE}" ]; then
  echo "File docker-compose non trovato in ${COMPOSE_FILE}" >&2
  exit 1
fi

args=(-f "${COMPOSE_FILE}" down)
if [ "${REMOVE_VOLUMES}" = "true" ]; then
  args+=(-v)
fi

echo "[frontend:down] arresto stack (volumi: ${REMOVE_VOLUMES})..."
${COMPOSE} "${args[@]}"

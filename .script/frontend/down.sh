#!/usr/bin/env bash
#
# Ferma il servizio frontend.
# Ferma solo il container frontend, non l'intero stack.
#
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/.infra/docker-compose.yml}"
readonly COMPOSE="docker compose"
readonly SERVICE="sanitech-fe"

if ! command -v docker >/dev/null 2>&1; then
    echo "[ERRORE] Docker non trovato. Nulla da fermare." >&2
    exit 1
fi

if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo "[ERRORE] File Docker Compose non trovato: ${COMPOSE_FILE}" >&2
    exit 1
fi

echo "[frontend:down] Arresto servizio: ${SERVICE}"
${COMPOSE} -f "${COMPOSE_FILE}" stop "${SERVICE}"

echo "[frontend:down] Rimozione container..."
${COMPOSE} -f "${COMPOSE_FILE}" rm -f "${SERVICE}"

echo "[frontend:down] Frontend arrestato."

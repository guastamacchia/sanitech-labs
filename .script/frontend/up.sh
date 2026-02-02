#!/usr/bin/env bash
#
# Avvia il servizio frontend dal docker-compose principale.
# Il frontend dipende da svc-gateway che deve essere healthy.
#
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/.infra/docker-compose.yml}"
readonly COMPOSE="docker compose"
readonly SERVICE="sanitech-fe"

if ! command -v docker >/dev/null 2>&1; then
    echo "[ERRORE] Docker non trovato. Installa Docker per avviare il frontend." >&2
    exit 1
fi

if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo "[ERRORE] File Docker Compose non trovato: ${COMPOSE_FILE}" >&2
    exit 1
fi

echo "[frontend:up] Uso file compose: ${COMPOSE_FILE}"
echo "[frontend:up] Avvio servizio: ${SERVICE}"

${COMPOSE} -f "${COMPOSE_FILE}" up -d --build "${SERVICE}" "$@"

echo "[frontend:up] Stato servizio:"
${COMPOSE} -f "${COMPOSE_FILE}" ps "${SERVICE}"

echo ""
echo "[frontend:up] Frontend disponibile su: http://localhost:4200"

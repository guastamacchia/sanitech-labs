#!/usr/bin/env bash
#
# Segue i log dei servizi backend.
#
# Utilizzo:
#   bash .script/backend/logs.sh                    # Tutti i servizi
#   bash .script/backend/logs.sh svc-directory      # Servizio specifico
#   bash .script/backend/logs.sh svc-gateway kafka  # Più servizi
#
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/.infra/docker-compose.yml}"
readonly COMPOSE="docker compose"

if ! command -v docker >/dev/null 2>&1; then
    echo "[ERRORE] Docker non trovato. Impossibile mostrare i log." >&2
    exit 1
fi

if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo "[ERRORE] File Docker Compose non trovato: ${COMPOSE_FILE}" >&2
    exit 1
fi

services=("$@")

if [[ ${#services[@]} -eq 0 ]]; then
    echo "[logs] Seguo log di tutti i servizi (Ctrl+C per interrompere)"
    ${COMPOSE} -f "${COMPOSE_FILE}" logs -f
else
    echo "[logs] Seguo log per: ${services[*]}"
    ${COMPOSE} -f "${COMPOSE_FILE}" logs -f "${services[@]}"
fi

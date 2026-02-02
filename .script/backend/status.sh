#!/usr/bin/env bash
#
# Mostra lo stato di tutti i servizi backend.
#
# Utilizzo:
#   bash .script/backend/status.sh
#
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/.infra/docker-compose.yml}"
readonly COMPOSE="docker compose"

# Colori per output
readonly GREEN='\033[0;32m'
readonly NC='\033[0m'

if ! command -v docker >/dev/null 2>&1; then
    echo "[ERRORE] Docker non trovato. Impossibile mostrare lo stato." >&2
    exit 1
fi

if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo "[ERRORE] File Docker Compose non trovato: ${COMPOSE_FILE}" >&2
    exit 1
fi

echo -e "${GREEN}[status]${NC} Stato container:"
echo ""
${COMPOSE} -f "${COMPOSE_FILE}" ps

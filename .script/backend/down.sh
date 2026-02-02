#!/usr/bin/env bash
#
# Ferma lo stack Sanitech backend.
#
# Utilizzo:
#   bash .script/backend/down.sh                      # Ferma servizi, mantiene dati
#   REMOVE_VOLUMES=true bash .script/backend/down.sh  # Ferma e rimuovi tutti i dati
#
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/.infra/docker-compose.yml}"
readonly COMPOSE="docker compose"
readonly REMOVE_VOLUMES="${REMOVE_VOLUMES:-false}"

# Colori per output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly NC='\033[0m'

log_info() {
    echo -e "${GREEN}[down]${NC} $*"
}

log_warn() {
    echo -e "${YELLOW}[down]${NC} $*"
}

log_error() {
    echo -e "${RED}[down]${NC} $*" >&2
}

if ! command -v docker >/dev/null 2>&1; then
    log_error "Docker non trovato. Nulla da fermare."
    exit 1
fi

if [[ ! -f "${COMPOSE_FILE}" ]]; then
    log_error "File Docker Compose non trovato: ${COMPOSE_FILE}"
    exit 1
fi

args=(-f "${COMPOSE_FILE}" down)
if [[ "${REMOVE_VOLUMES}" == "true" ]]; then
    args+=(-v)
    log_warn "Rimozione volumi - tutti i dati saranno eliminati!"
fi

log_info "Arresto stack (rimuovi volumi: ${REMOVE_VOLUMES})..."
${COMPOSE} "${args[@]}"

log_info "Stack arrestato."

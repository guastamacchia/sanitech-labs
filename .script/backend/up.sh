#!/usr/bin/env bash
#
# Avvia lo stack completo Sanitech backend.
# Include: servizi infrastrutturali, tutti i microservizi e frontend.
#
# Utilizzo:
#   bash .script/backend/up.sh              # Avvia tutti i servizi
#   bash .script/backend/up.sh --no-build   # Avvia senza rebuild
#   ENV=staging bash .script/backend/up.sh  # Usa ambiente staging
#
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/.infra/docker-compose.yml}"
readonly ENV_FILE="${ROOT_DIR}/.infra/env/env.${ENV:-local}"
readonly COMPOSE="docker compose"

# Colori per output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly NC='\033[0m'

log_info() {
    echo -e "${GREEN}[up]${NC} $*"
}

log_warn() {
    echo -e "${YELLOW}[up]${NC} $*"
}

log_error() {
    echo -e "${RED}[up]${NC} $*" >&2
}

# Verifica prerequisiti
if ! command -v docker >/dev/null 2>&1; then
    log_error "Docker non trovato. Installa Docker per avviare lo stack."
    exit 1
fi

if [[ ! -f "${COMPOSE_FILE}" ]]; then
    log_error "File Docker Compose non trovato: ${COMPOSE_FILE}"
    exit 1
fi

if [[ ! -f "${ENV_FILE}" ]]; then
    log_warn "File ambiente non trovato: ${ENV_FILE}"
    log_warn "Uso valori di default da docker-compose.yml"
fi

log_info "Avvio stack Sanitech..."
log_info "File compose: ${COMPOSE_FILE}"
log_info "Ambiente: ${ENV:-local}"

# Build e avvio servizi
log_info "Build immagini..."
${COMPOSE} -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" build

log_info "Avvio servizi..."
${COMPOSE} -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" up -d "$@"

log_info "Stack avviato con successo!"
echo ""
log_info "Stato servizi:"
${COMPOSE} -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" ps

echo ""
log_info "Endpoint:"
echo "  - Frontend:   http://localhost:4200"
echo "  - Gateway:    http://localhost:8080"
echo "  - Swagger:    http://localhost:8080/swagger-ui/index.html"
echo "  - Keycloak:   http://localhost:8081"
echo "  - Grafana:    http://localhost:3000"
echo "  - Prometheus: http://localhost:9090"
echo "  - Mailpit:    http://localhost:8025"
echo ""
log_info "Usa 'bash .script/backend/logs.sh' per visualizzare i log"

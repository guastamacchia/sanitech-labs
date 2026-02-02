#!/usr/bin/env bash
#
# Test circuit breaker per svc-gateway.
# Verifica stato e metriche del circuit breaker.
#
# Utilizzo:
#   bash .script/services/svc-gateway/circuit-breaker.sh
#   SERVICE_URL=http://custom:8080 bash .script/services/svc-gateway/circuit-breaker.sh
#
set -euo pipefail

readonly SERVICE_URL="${SERVICE_URL:-http://localhost:8080}"
readonly TIMEOUT="${TIMEOUT:-5}"

# Colori
readonly GREEN='\033[0;32m'
readonly RED='\033[0;31m'
readonly YELLOW='\033[1;33m'
readonly NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $*"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $*"
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $*"
}

echo "================================"
echo "  svc-gateway Test Circuit Breaker"
echo "  URL: ${SERVICE_URL}"
echo "================================"
echo ""

# Verifica metrica stato CircuitBreaker
state_url="${SERVICE_URL}/actuator/metrics/resilience4j.circuitbreaker.state"

log_info "Verifica metrica stato CircuitBreaker..."
resp=$(curl -s -w "\n%{http_code}" --max-time "${TIMEOUT}" "${state_url}" 2>/dev/null || echo -e "\n000")
status=$(echo "${resp}" | tail -n1)

if [[ "${status}" != "200" ]]; then
    log_warn "Metrica CircuitBreaker non disponibile (HTTP ${status})"
    log_warn "Il circuit breaker potrebbe non essere configurato."
    echo ""
    exit 0
fi

log_info "Metrica CircuitBreaker disponibile"
echo ""

# Verifica endpoint health per dettagli circuit breaker
log_info "Verifica health circuit breaker..."
health_url="${SERVICE_URL}/actuator/health"

if command -v jq >/dev/null 2>&1; then
    health_resp=$(curl -s --max-time "${TIMEOUT}" "${health_url}" 2>/dev/null || echo "{}")
    cb_health=$(echo "${health_resp}" | jq -r '.components.circuitBreakers // empty' 2>/dev/null || echo "")

    if [[ -n "${cb_health}" && "${cb_health}" != "null" ]]; then
        log_info "Stato circuit breakers:"
        echo "${cb_health}" | jq '.'
    else
        log_warn "Nessun dato health circuit breaker trovato"
    fi
else
    log_warn "jq non installato - impossibile parsare dettagli circuit breaker"
    echo "  Installa jq per informazioni dettagliate sul circuit breaker"
fi

echo ""
echo "================================"
echo -e "${GREEN}Test circuit breaker completato${NC}"

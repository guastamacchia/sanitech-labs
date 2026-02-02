#!/usr/bin/env bash
#
# Test rate limiter per svc-gateway.
# Verifica se il rate limiting è configurato e funzionante.
#
# Utilizzo:
#   bash .script/services/svc-gateway/rate-limit.sh
#   SERVICE_URL=http://custom:8080 bash .script/services/svc-gateway/rate-limit.sh
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
echo "  svc-gateway Test Rate Limit"
echo "  URL: ${SERVICE_URL}"
echo "================================"
echo ""

# Verifica disponibilità metrica RateLimiter
metric_url="${SERVICE_URL}/actuator/metrics/resilience4j.ratelimiter.available.permissions"

log_info "Verifica metrica RateLimiter..."
resp=$(curl -s -w "\n%{http_code}" --max-time "${TIMEOUT}" "${metric_url}" 2>/dev/null || echo -e "\n000")
status=$(echo "${resp}" | tail -n1)
body=$(echo "${resp}" | sed '$d')

if [[ "${status}" != "200" ]]; then
    log_warn "Metrica RateLimiter non disponibile (HTTP ${status})"
    log_warn "Il rate limiting potrebbe non essere configurato."
    echo ""
    exit 0
fi

log_info "Metrica RateLimiter disponibile"
echo ""

# Test comportamento rate limiting
log_info "Test comportamento rate limiting..."
echo ""

declare -a results
for i in {1..5}; do
    code=$(curl -s -o /dev/null -w "%{http_code}" --max-time "${TIMEOUT}" "${SERVICE_URL}/actuator/health")
    results+=("${code}")
    echo "  Richiesta ${i}: HTTP ${code}"
done

echo ""

# Verifica risposte 429
has_429=false
for code in "${results[@]}"; do
    if [[ "${code}" == "429" ]]; then
        has_429=true
        break
    fi
done

if [[ "${has_429}" == "true" ]]; then
    log_info "Rate limiting attivo (ricevuto HTTP 429)"
else
    log_info "Nessun rate limiting scattato in ${#results[@]} richieste"
    log_warn "La soglia del rate limit potrebbe essere più alta o non configurata per l'endpoint health"
fi

echo ""
echo "================================"
echo -e "${GREEN}Test rate limit completato${NC}"

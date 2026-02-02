#!/usr/bin/env bash
#
# Smoke test per svc-gateway.
# Verifica che gli endpoint health, metriche e OpenAPI siano accessibili.
#
# Utilizzo:
#   bash .script/services/svc-gateway/smoke.sh
#   SERVICE_URL=http://custom:8080 bash .script/services/svc-gateway/smoke.sh
#
set -euo pipefail

readonly SERVICE_URL="${SERVICE_URL:-http://localhost:8080}"
readonly TIMEOUT="${TIMEOUT:-5}"

# Colori
readonly GREEN='\033[0;32m'
readonly RED='\033[0;31m'
readonly NC='\033[0m'

log_ok() {
    echo -e "${GREEN}[OK]${NC} $*"
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $*"
}

check_endpoint() {
    local name="$1"
    local path="$2"
    local url="${SERVICE_URL}${path}"

    if curl -fsS --max-time "${TIMEOUT}" "${url}" >/dev/null 2>&1; then
        log_ok "${name}"
        return 0
    else
        log_fail "${name} - ${url}"
        return 1
    fi
}

echo "================================"
echo "  svc-gateway Smoke Test"
echo "  URL: ${SERVICE_URL}"
echo "================================"
echo ""

failed=0

check_endpoint "Health check" "/actuator/health" || failed=$((failed + 1))
check_endpoint "Endpoint metriche" "/actuator/metrics" || failed=$((failed + 1))
check_endpoint "Documentazione OpenAPI" "/v3/api-docs" || failed=$((failed + 1))
check_endpoint "Swagger UI" "/swagger-ui/index.html" || failed=$((failed + 1))

echo ""

if [[ ${failed} -gt 0 ]]; then
    echo -e "${RED}Smoke test fallito: ${failed} controllo/i fallito/i${NC}"
    exit 1
else
    echo -e "${GREEN}Tutti i controlli smoke superati!${NC}"
    exit 0
fi

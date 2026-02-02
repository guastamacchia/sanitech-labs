#!/usr/bin/env bash
#
# Esegue smoke test su tutti i servizi backend.
# Verifica che i servizi siano attivi e rispondano agli health check.
#
# Utilizzo:
#   bash .script/backend/smoke.sh
#
set -euo pipefail

# Colori per output
readonly GREEN='\033[0;32m'
readonly RED='\033[0;31m'
readonly YELLOW='\033[1;33m'
readonly NC='\033[0m'

# Configurazione servizi: nome:porta
readonly SERVICES=(
    "svc-gateway:8080"
    "svc-directory:8082"
    "svc-scheduling:8083"
    "svc-admissions:8084"
    "svc-consents:8085"
    "svc-docs:8086"
    "svc-notifications:8087"
    "svc-audit:8088"
    "svc-televisit:8089"
    "svc-payments:8090"
    "svc-prescribing:8091"
)

readonly INFRA_SERVICES=(
    "keycloak:8081:/health/ready"
    "prometheus:9090:/-/healthy"
    "grafana:3000:/api/health"
)

readonly TIMEOUT="${TIMEOUT:-5}"
readonly MAX_RETRIES="${MAX_RETRIES:-30}"
readonly RETRY_DELAY="${RETRY_DELAY:-2}"

log_ok() {
    echo -e "  ${GREEN}[OK]${NC} $*"
}

log_fail() {
    echo -e "  ${RED}[FAIL]${NC} $*"
}

log_wait() {
    echo -e "  ${YELLOW}[ATTESA]${NC} $*"
}

check_health() {
    local url="$1"
    curl -fsS --max-time "${TIMEOUT}" "${url}" >/dev/null 2>&1
}

wait_for_service() {
    local name="$1"
    local url="$2"
    local retries=0

    while ! check_health "${url}"; do
        retries=$((retries + 1))
        if [[ ${retries} -ge ${MAX_RETRIES} ]]; then
            return 1
        fi
        sleep "${RETRY_DELAY}"
    done
    return 0
}

echo "================================"
echo "  Sanitech Smoke Test"
echo "================================"
echo ""

# Verifica servizi infrastrutturali
echo "Servizi infrastrutturali:"
echo "-------------------------"

for svc in "${INFRA_SERVICES[@]}"; do
    name="${svc%%:*}"
    rest="${svc#*:}"
    port="${rest%%:*}"
    path="${rest#*:}"
    url="http://localhost:${port}${path}"

    if check_health "${url}"; then
        log_ok "${name} (porta ${port})"
    else
        log_fail "${name} (porta ${port}) - ${url}"
    fi
done

echo ""

# Attesa Keycloak (richiesto per altri servizi)
echo "Attesa Keycloak..."
if ! wait_for_service "keycloak" "http://localhost:8081/health/ready"; then
    log_fail "Keycloak non è diventato pronto in tempo"
    exit 1
fi
log_ok "Keycloak è pronto"

echo ""

# Verifica microservizi backend
echo "Microservizi backend:"
echo "---------------------"

failed=0
for svc in "${SERVICES[@]}"; do
    name="${svc%%:*}"
    port="${svc##*:}"
    url="http://localhost:${port}/actuator/health"

    if check_health "${url}"; then
        log_ok "${name} (porta ${port})"
    else
        log_fail "${name} (porta ${port})"
        failed=$((failed + 1))
    fi
done

echo ""

# Verifica frontend
echo "Frontend:"
echo "---------"

if check_health "http://localhost:4200"; then
    log_ok "sanitech-fe (porta 4200)"
else
    log_fail "sanitech-fe (porta 4200)"
    failed=$((failed + 1))
fi

echo ""
echo "================================"

if [[ ${failed} -gt 0 ]]; then
    echo -e "${RED}Smoke test falliti: ${failed} servizio/i non healthy${NC}"
    exit 1
else
    echo -e "${GREEN}Tutti gli smoke test superati!${NC}"
    exit 0
fi

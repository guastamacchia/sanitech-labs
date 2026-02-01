#!/usr/bin/env bash
# =====================================================
# Test Bulkhead (Resilience4j) – massimo N cicli
# =====================================================
# - Richiede un token OAuth2 a Keycloak
# - Verifica lo stato di salute del servizio
# - Lancia due chiamate concorrenti sullo stesso endpoint admin
# - Con maxConcurrentCalls=1 ci si aspetta:
#   - 1 richiesta OK (200)
#   - 1 richiesta rifiutata (bulkhead attivo)
# - Ripete il test per un numero finito di cicli
# =====================================================

set -euo pipefail

# =====================================================
# Service identifier (align con Makefile/application.yml)
# =====================================================
SERVICE_NAME="${SVC_NAME:-${ARTIFACT_ID:-svc-directory}}"
SERVICE_PORT="${PORT:-8082}"
DEFAULT_SERVICE_URL="${SERVICE_URL:-http://localhost:${SERVICE_PORT}}"
DEFAULT_CLIENT_ID="${CLIENT_ID:-${SERVICE_NAME}}"
DEFAULT_CLIENT_SECRET="${CLIENT_SECRET:-${SERVICE_NAME}-secret}"

# =====================================================
# Parametri configurabili (con default)
# =====================================================
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8081}"
REALM="${REALM:-sanitech}"
SERVICE_URL="${SERVICE_URL:-${DEFAULT_SERVICE_URL}}"
CLIENT_ID="${CLIENT_ID:-${DEFAULT_CLIENT_ID}}"
CLIENT_SECRET="${CLIENT_SECRET:-${DEFAULT_CLIENT_SECRET}}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-admin}"
SLEEP_SECONDS="${SLEEP_SECONDS:-2}"

# Numero massimo di cicli di test
MAX_CYCLES=5

# =====================================================
# Normalizzazione degli URL (rimozione spazi e slash finali)
# =====================================================
KEYCLOAK_URL="$(echo "${KEYCLOAK_URL}" | xargs)"
SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
KEYCLOAK_URL="${KEYCLOAK_URL%/}"
SERVICE_URL="${SERVICE_URL%/}"

# =====================================================
# Recupero del token OAuth2 da Keycloak
# =====================================================
echo ">> Richiedo token per l'utente '${USERNAME}'"

TOKEN_JSON=$(curl -fsSL -X POST \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}")

# Estrazione dell'access token dalla risposta JSON
ACCESS_TOKEN=$(echo "${TOKEN_JSON}" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')

if [ -z "${ACCESS_TOKEN}" ]; then
  echo "ERRORE: impossibile estrarre l'access_token dalla risposta:"
  echo "${TOKEN_JSON}"
  exit 1
fi

# =====================================================
# Funzione di supporto: restituisce lo status HTTP
# =====================================================
code_for() {
  local url="$1"
  curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    "${url}"
}

# =====================================================
# Loop principale: massimo N cicli
# =====================================================
echo ">> Avvio test Bulkhead (${MAX_CYCLES} cicli)"

for ((i=1; i<=MAX_CYCLES; i++)); do
  echo
  echo "=== $(date '+%Y-%m-%dT%H:%M:%S%z') — Ciclo ${i}/${MAX_CYCLES} ==="

  # ---------------------------------------------------
  # Verifica health del servizio
  # ---------------------------------------------------
  echo ">> Verifica health del servizio"
  if [ "$(code_for "${SERVICE_URL}/actuator/health")" != "200" ]; then
    echo "Health check FALLITO"
    exit 1
  fi

  # ---------------------------------------------------
  # Test bulkhead: due chiamate concorrenti
  # ---------------------------------------------------
  echo ">> Avvio due chiamate concorrenti a /api/admin/patients"

  code1_file=$(mktemp)
  code2_file=$(mktemp)

  (code_for "${SERVICE_URL}/api/admin/patients" >"${code1_file}") &
  (code_for "${SERVICE_URL}/api/admin/patients" >"${code2_file}") &
  wait

  code1=$(cat "${code1_file}")
  code2=$(cat "${code2_file}")
  rm -f "${code1_file}" "${code2_file}"

  echo "Prima chiamata  - HTTP status: ${code1}"
  echo "Seconda chiamata - HTTP status: ${code2}"

  # ---------------------------------------------------
  # Valutazione del risultato
  # ---------------------------------------------------
  if [ "${code1}" = "200" ] && [ "${code2}" = "200" ]; then
    echo "Bulkhead test FALLITO:"
    echo "entrambe le chiamate sono andate a buon fine (atteso maxConcurrentCalls=1)."
    exit 1
  fi

  if [ "${code1}" != "200" ] && [ "${code2}" != "200" ]; then
    echo "Bulkhead test ATTENZIONE:"
    echo "entrambe le chiamate sono fallite (servizio instabile o bulkhead troppo restrittivo)."
  else
    echo "Bulkhead test SUPERATO:"
    echo "la concorrenza è stata limitata correttamente (una chiamata accettata, una rifiutata)."
  fi

  # Pausa prima del ciclo successivo
  sleep "${SLEEP_SECONDS}"
done

echo
echo ">> Test Bulkhead completato: eseguiti ${MAX_CYCLES} cicli."

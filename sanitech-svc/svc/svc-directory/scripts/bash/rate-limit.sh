#!/usr/bin/env bash
# =====================================================
# Test Rate Limit (Resilience4j)
# =====================================================
# - Recupera un token OAuth2 da Keycloak
# - Esegue due chiamate immediate all’endpoint /api/doctors
# - Con configurazione di rate limit aggressiva si attende:
#   - prima chiamata: 200 (OK)
#   - seconda chiamata: 429 (Too Many Requests)
# - Ripete il test per un numero finito di cicli
# =====================================================

set -euo pipefail

# =====================================================
# Service identifier (allineato con Makefile/application.yml)
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
SLEEP_SECONDS="${SLEEP_SECONDS:-5}"

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
# Loop principale: massimo N cicli
# =====================================================
echo ">> Avvio test Rate Limit (${MAX_CYCLES} cicli)"

for ((i=1; i<=MAX_CYCLES; i++)); do
  echo
  echo "=== $(date '+%Y-%m-%dT%H:%M:%S%z') — Ciclo ${i}/${MAX_CYCLES} ==="

  # ---------------------------------------------------
  # Prima chiamata: ci si aspetta 200 OK
  # ---------------------------------------------------
  echo ">> Prima chiamata a /api/doctors (atteso 200)"
  code1=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    "${SERVICE_URL}/api/doctors")
  echo "Stato prima chiamata : ${code1}"

  # ---------------------------------------------------
  # Seconda chiamata immediata: ci si aspetta 429
  # ---------------------------------------------------
  echo ">> Seconda chiamata immediata a /api/doctors (atteso 429 se rate limit attivo)"
  code2=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    "${SERVICE_URL}/api/doctors")
  echo "Stato seconda chiamata: ${code2}"

  # ---------------------------------------------------
  # Valutazione del risultato
  # ---------------------------------------------------
  if [ "${code1}" != "200" ]; then
    echo "Rate limit test FALLITO: prima chiamata con stato inatteso ${code1}"
    exit 1
  fi

  if [ "${code2}" != "429" ]; then
    echo "Rate limit test FALLITO: seconda chiamata attesa 429 ma ricevuto ${code2}"
    exit 1
  fi

  echo "Rate limit test SUPERATO (200 → 429)."

  # Pausa prima del ciclo successivo
  sleep "${SLEEP_SECONDS}"
done

echo
echo ">> Test Rate Limit completato: eseguiti ${MAX_CYCLES} cicli."

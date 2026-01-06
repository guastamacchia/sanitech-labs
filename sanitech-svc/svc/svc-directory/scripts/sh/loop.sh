#!/usr/bin/env bash
# =====================================================
# Script di test API (smoke + rate limit + bulkhead)
# =====================================================
# - Recupera un access token da Keycloak
# - Esegue una serie di chiamate REST protette
# - Ripete i test per un numero limitato di cicli
# =====================================================

set -uo pipefail

# =====================================================
# Service identifier (allineato con Makefile/application.yml)
# =====================================================
SERVICE_NAME="${SVC_NAME:-${ARTIFACT_ID:-svc-directory}}"
SERVICE_PORT="${PORT:-8082}"
DEFAULT_SERVICE_URL="${SERVICE_URL:-http://localhost:${SERVICE_PORT}}"
DEFAULT_CLIENT_ID="${CLIENT_ID:-${SERVICE_NAME}}"
DEFAULT_CLIENT_SECRET="${CLIENT_SECRET:-${SERVICE_NAME}-secret}"

# =====================================================
# Funzione: restituisce solo lo status HTTP di una chiamata curl
# =====================================================
curl_status() {
  local url="$1"
  shift || true

  local status
  status=$(curl --silent --output /dev/null --write-out "%{http_code}" "$@" "${url}") || status="ERR"

  if [ -z "${status}" ]; then
    status="ERR"
  fi

  echo "${status}"
}

# =====================================================
# Funzione: prompt interattivo con valore di default
# =====================================================
prompt() {
  local var="$1"
  local prompt="$2"
  local default="$3"

  read -rp "${prompt} [${default}]: " input

  if [ -z "${input}" ]; then
    eval "${var}=\"${default}\""
  else
    eval "${var}=\"${input}\""
  fi
}

# =====================================================
# Parametri configurabili (con default)
# =====================================================
prompt KEYCLOAK_URL "Keycloak base URL" "${KEYCLOAK_URL:-http://localhost:8081}"
prompt REALM "Realm" "${REALM:-sanitech}"
prompt SERVICE_URL "${SERVICE_NAME} base URL" "${DEFAULT_SERVICE_URL}"
prompt CLIENT_ID "Client ID" "${DEFAULT_CLIENT_ID}"
prompt CLIENT_SECRET "Client secret" "${DEFAULT_CLIENT_SECRET}"
prompt USERNAME "Username" "${USERNAME:-admin}"
prompt PASSWORD "Password" "${PASSWORD:-admin}"
prompt SLEEP_SECONDS "Pausa tra i cicli (secondi)" "${SLEEP_SECONDS:-5}"

# Numero massimo di cicli
MAX_CYCLES=5

# =====================================================
# Normalizzazione URL (rimuove slash finali)
# =====================================================
KEYCLOAK_URL="$(echo "${KEYCLOAK_URL}" | xargs)"
SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
KEYCLOAK_URL="${KEYCLOAK_URL%/}"
SERVICE_URL="${SERVICE_URL%/}"

# =====================================================
# Recupero access token da Keycloak
# =====================================================
echo ">> Richiedo token per l'utente '${USERNAME}'"

TOKEN_JSON=$(curl -fsSL -X POST \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}")

ACCESS_TOKEN=$(echo "${TOKEN_JSON}" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')

if [ -z "${ACCESS_TOKEN}" ]; then
  echo "ERRORE: impossibile estrarre access_token dalla risposta:"
  echo "${TOKEN_JSON}"
  exit 1
fi

# =====================================================
# Loop principale: massimo N cicli
# =====================================================
echo ">> Avvio test API (${MAX_CYCLES} cicli)"

for ((i=1; i<=MAX_CYCLES; i++)); do
  echo
  echo "=== $(date '+%Y-%m-%dT%H:%M:%S%z') — Ciclo ${i}/${MAX_CYCLES} ==="

  # ---------------------------------------------------
  # Health check
  # ---------------------------------------------------
  status=$(curl_status "${SERVICE_URL}/actuator/health")
  echo "Health                    : ${status}"

  # ---------------------------------------------------
  # Ricerca medici
  # ---------------------------------------------------
  status=$(curl_status "${SERVICE_URL}/api/doctors" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "GET /api/doctors           : ${status}"

  # ---------------------------------------------------
  # Pazienti
  # ---------------------------------------------------
  status=$(curl_status "${SERVICE_URL}/api/patients" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "GET /api/patients          : ${status}"

  # ---------------------------------------------------
  # Endpoints amministrativi
  # ---------------------------------------------------
  status=$(curl_status "${SERVICE_URL}/api/admin/departments" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "Admin departments          : ${status}"

  status=$(curl_status "${SERVICE_URL}/api/admin/specializations" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "Admin specializations      : ${status}"

  status=$(curl_status "${SERVICE_URL}/api/admin/doctors" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "Admin doctors              : ${status}"

  status=$(curl_status "${SERVICE_URL}/api/admin/patients" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "Admin patients             : ${status}"

  # ---------------------------------------------------
  # Metriche
  # ---------------------------------------------------
  status=$(curl_status "${SERVICE_URL}/actuator/metrics")
  echo "Metrics list               : ${status}"

  # Pausa tra un ciclo e il successivo
  sleep "${SLEEP_SECONDS}"
done

echo
echo ">> Test completati: eseguiti ${MAX_CYCLES} cicli."
echo ">> Fine script."

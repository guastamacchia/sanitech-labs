#!/usr/bin/env bash
# =====================================================
# Smoke test completo svc-directory
# =====================================================
# - Recupera un token OAuth2 da Keycloak
# - Verifica health del servizio
# - Esegue chiamate protette agli endpoint principali
# - Valida il RateLimiter (200 → 429)
# - Verifica l’esposizione delle metriche Resilience4j
#
# Obiettivo:
# - garantire che il servizio sia operativo
# - verificare che sicurezza e resilienza siano attive
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
prompt KEYCLOAK_URL "URL base di Keycloak" "${KEYCLOAK_URL:-http://localhost:8081}"
prompt REALM "Realm" "${REALM:-sanitech}"
prompt SERVICE_URL "URL base di ${SERVICE_NAME}" "${DEFAULT_SERVICE_URL}"
prompt CLIENT_ID "Client ID" "${DEFAULT_CLIENT_ID}"
prompt CLIENT_SECRET "Client secret" "${DEFAULT_CLIENT_SECRET}"
prompt USERNAME "Username" "${USERNAME:-admin}"
prompt PASSWORD "Password" "${PASSWORD:-admin}"

# =====================================================
# Normalizzazione URL (rimozione spazi e slash finali)
# =====================================================
KEYCLOAK_URL="$(echo "${KEYCLOAK_URL}" | xargs)"
SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
KEYCLOAK_URL="${KEYCLOAK_URL%/}"
SERVICE_URL="${SERVICE_URL%/}"

# =====================================================
# Recupero token OAuth2 da Keycloak
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
# Verifica health del servizio
# =====================================================
echo ">> Verifica health del servizio su ${SERVICE_URL}/actuator/health"
curl -fsSL "${SERVICE_URL}/actuator/health" >/dev/null
echo "OK"

# =====================================================
# Prima chiamata protetta: deve andare a buon fine (200)
# =====================================================
echo ">> Chiamata a /api/doctors (prima chiamata, atteso 200)"

DOCTORS_BODY=$(curl -s \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -w "\n%{http_code}" \
  "${SERVICE_URL}/api/doctors")

DOCTORS_STATUS=$(echo "${DOCTORS_BODY}" | tail -n1)
DOCTORS_CONTENT=$(echo "${DOCTORS_BODY}" | sed '$d')

if [ "${DOCTORS_STATUS}" != "200" ]; then
  echo "ERRORE: stato inatteso ${DOCTORS_STATUS} da /api/doctors"
  echo "Corpo risposta:"
  echo "${DOCTORS_CONTENT}"
  exit 1
fi

echo "OK"

# =====================================================
# Seconda chiamata immediata: deve essere rate-limited (429)
# =====================================================
echo ">> Seconda chiamata a /api/doctors (atteso 429 – RateLimiter attivo)"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  "${SERVICE_URL}/api/doctors")

if [ "${STATUS}" != "429" ]; then
  echo "ERRORE RateLimiter: atteso 429, ricevuto ${STATUS}"
  exit 1
fi

echo "OK (429 ricevuto)"

# =====================================================
# Verifica esposizione metriche Resilience4j
# =====================================================
echo ">> Verifica metriche Resilience4j (bulkhead e rate limiter)"

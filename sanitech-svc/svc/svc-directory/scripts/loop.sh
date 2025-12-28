#!/usr/bin/env bash
set -uo pipefail

prompt() {
  local var="$1" prompt="$2" default="$3"
  read -rp "${prompt} [${default}]: " input
  if [ -z "${input}" ]; then
    eval "${var}=\"${default}\""
  else
    eval "${var}=\"${input}\""
  fi
}

prompt KEYCLOAK_URL "Keycloak base URL" "${KEYCLOAK_URL:-http://localhost:8081}"
prompt REALM "Realm" "${REALM:-sanitech}"
prompt SERVICE_URL "svc-directory base URL" "${SERVICE_URL:-http://localhost:8082}"
prompt CLIENT_ID "Client ID" "${CLIENT_ID:-svc-directory}"
prompt CLIENT_SECRET "Client secret" "${CLIENT_SECRET:-svc-directory-secret}"
prompt USERNAME "Username" "${USERNAME:-admin}"
prompt PASSWORD "Password" "${PASSWORD:-admin}"
prompt SLEEP_SECONDS "Pause between cycles (seconds)" "${SLEEP_SECONDS:-5}"

KEYCLOAK_URL="$(echo "${KEYCLOAK_URL}" | xargs)"
SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
KEYCLOAK_URL="${KEYCLOAK_URL%/}"
SERVICE_URL="${SERVICE_URL%/}"

echo ">> Requesting token for user '${USERNAME}'"
TOKEN_JSON=$(curl -fsSL -X POST \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}")

ACCESS_TOKEN=$(echo "${TOKEN_JSON}" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
if [ -z "${ACCESS_TOKEN}" ]; then
  echo "Unable to parse access token from response:"
  echo "${TOKEN_JSON}"
  exit 1
fi

echo ">> Starting infinite loop over key APIs (Ctrl+C to stop)"
while true; do
  echo "=== $(date -Is) ==="

  # Health (public)
  status=$(curl -s -o /dev/null -w "%{http_code}" "${SERVICE_URL}/actuator/health" || echo "ERR")
  echo "Health            : ${status}"

  # Public doctors search (rate-limited)
  status=$(curl -s -o /dev/null -w "%{http_code}" "${SERVICE_URL}/api/doctors" || echo "ERR")
  echo "GET /api/doctors  : ${status}"

  # Patients (requires ROLE_ADMIN / ROLE_DOCTOR)
  status=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/patients" || echo "ERR")
  echo "GET /api/patients : ${status}"

  # Departments (admin)
  status=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/admin/departments" || echo "ERR")
  echo "Admin departments : ${status}"

  # Specializations (admin)
  status=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/admin/specializations" || echo "ERR")
  echo "Admin specs       : ${status}"

  # Doctors admin search
  status=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/admin/doctors" || echo "ERR")
  echo "Admin doctors     : ${status}"

  # Patients admin search
  status=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/admin/patients" || echo "ERR")
  echo "Admin patients    : ${status}"

  # Metrics (public)
  status=$(curl -s -o /dev/null -w "%{http_code}" "${SERVICE_URL}/actuator/metrics" || echo "ERR")
  echo "Metrics list      : ${status}"

  sleep "${SLEEP_SECONDS}"
done

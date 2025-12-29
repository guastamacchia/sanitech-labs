#!/usr/bin/env bash
set -uo pipefail

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
  status=$(curl_status "${SERVICE_URL}/actuator/health")
  echo "Health            : ${status}"

  # Public doctors search (rate-limited)
  status=$(curl_status "${SERVICE_URL}/api/doctors" -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "GET /api/doctors  : ${status}"

  # Patients (requires ROLE_ADMIN / ROLE_DOCTOR)
  status=$(curl_status "${SERVICE_URL}/api/patients" -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "GET /api/patients : ${status}"

  # Departments (admin)
  status=$(curl_status "${SERVICE_URL}/api/admin/departments" -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "Admin departments : ${status}"

  # Specializations (admin)
  status=$(curl_status "${SERVICE_URL}/api/admin/specializations" -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "Admin specs       : ${status}"

  # Doctors admin search
  status=$(curl_status "${SERVICE_URL}/api/admin/doctors" -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "Admin doctors     : ${status}"

  # Patients admin search
  status=$(curl_status "${SERVICE_URL}/api/admin/patients" -H "Authorization: Bearer ${ACCESS_TOKEN}")
  echo "Admin patients    : ${status}"

  # Metrics (public)
  status=$(curl_status "${SERVICE_URL}/actuator/metrics")
  echo "Metrics list      : ${status}"

  sleep "${SLEEP_SECONDS}"
done

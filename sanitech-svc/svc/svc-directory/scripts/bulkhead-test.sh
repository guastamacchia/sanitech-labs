#!/usr/bin/env bash
set -euo pipefail

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

code_for() {
  local url="$1"
  curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${url}"
}

echo ">> Checking health"
if [ "$(code_for "${SERVICE_URL}/actuator/health")" != "200" ]; then
  echo "Health check failed"
  exit 1
fi

echo ">> Triggering two concurrent admin patient searches to test Bulkhead (directoryRead)"
code1_file=$(mktemp)
code2_file=$(mktemp)

(code_for "${SERVICE_URL}/api/admin/patients" >"${code1_file}") &
(code_for "${SERVICE_URL}/api/admin/patients" >"${code2_file}") &
wait

code1=$(cat "${code1_file}")
code2=$(cat "${code2_file}")
rm -f "${code1_file}" "${code2_file}"

echo "First call  HTTP status: ${code1}"
echo "Second call HTTP status: ${code2}"

if [ "${code1}" = "200" ] && [ "${code2}" = "200" ]; then
  echo "Bulkhead test FAILED: both concurrent calls succeeded (expected one to be rejected with directoryRead maxConcurrentCalls=1)."
  exit 1
fi

if [ "${code1}" != "200" ] && [ "${code2}" != "200" ]; then
  echo "Bulkhead test WARNING: both calls failed (service may be unhealthy or bulkhead too strict)."
else
  echo "Bulkhead test PASSED: bulkhead limited concurrency (one call succeeded, one rejected)."
fi

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

ACCESS_TOKEN=$(echo "${TOKEN_JSON}" | sed -n 's/.*\"access_token\":\"\\([^\"]*\\)\".*/\\1/p')
if [ -z "${ACCESS_TOKEN}" ]; then
  echo "Unable to parse access token from response:"
  echo "${TOKEN_JSON}"
  exit 1
fi

echo ">> First call to /api/doctors (should be 200)"
code1=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/doctors")
echo "First call status: ${code1}"

echo ">> Second immediate call to /api/doctors (should be rate-limited to 429 with aggressive local config)"
code2=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/doctors")
echo "Second call status: ${code2}"

if [ "${code1}" != "200" ]; then
  echo "Rate limit test FAILED: first call unexpected status ${code1}"
  exit 1
fi

if [ "${code2}" != "429" ]; then
  echo "Rate limit test FAILED: second call expected 429 but got ${code2}"
  exit 1
fi

echo "Rate limit test PASSED (200 then 429)."

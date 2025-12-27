#!/usr/bin/env bash
set -euo pipefail

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8081}"
REALM="${REALM:-sanitech}"
SERVICE_URL="${SERVICE_URL:-http://localhost:8082}"
CLIENT_ID="${CLIENT_ID:-svc-directory}"
CLIENT_SECRET="${CLIENT_SECRET:-svc-directory-secret}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-admin}"

echo ">> Checking Keycloak health at ${KEYCLOAK_URL}/health/ready"
curl -fsSL "${KEYCLOAK_URL}/health/ready" >/dev/null
echo "OK"

echo ">> Requesting token for user '${USERNAME}'"
TOKEN_JSON=$(curl -fsSL -X POST \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}")

ACCESS_TOKEN=$(python - <<'PY'
import json, os
data = json.loads(os.environ["TOKEN_JSON"])
print(data["access_token"])
PY
)

echo ">> Checking svc-directory health at ${SERVICE_URL}/actuator/health"
curl -fsSL "${SERVICE_URL}/actuator/health" >/dev/null
echo "OK"

echo ">> Calling secured endpoint /api/doctors"
curl -fsSL -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/doctors" >/dev/null
echo "OK"

echo "All smoke checks passed."

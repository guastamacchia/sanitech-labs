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

echo ">> Calling secured endpoint /api/doctors (first call should pass)"
curl -fsSL -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/doctors" >/dev/null
echo "OK"

echo ">> Calling /api/doctors again to validate RateLimiter (expect 429)"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${SERVICE_URL}/api/doctors")
if [ "${STATUS}" != "429" ]; then
  echo "RateLimiter check failed: expected 429, got ${STATUS}"
  exit 1
fi
echo "OK (429 received)"

echo ">> Checking Resilience4j metrics (bulkhead & ratelimiter)"
curl -fsSL "${SERVICE_URL}/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls?tag=name:directoryRead" >/tmp/bulkhead-metrics.json
curl -fsSL "${SERVICE_URL}/actuator/metrics/resilience4j.bulkhead.max.allowed.concurrent.calls?tag=name:directoryRead" >/tmp/bulkhead-max.json
curl -fsSL "${SERVICE_URL}/actuator/metrics/resilience4j.ratelimiter.available.permissions?tag=name:directoryApi" >/tmp/ratelimiter-metrics.json

python - <<'PY'
import json, sys
with open("/tmp/bulkhead-max.json") as f:
    max_data = json.load(f)
max_allowed = max_data["measurements"][0]["value"]
if max_allowed != 1:
    sys.exit(f"Unexpected bulkhead max allowed concurrent calls: {max_allowed}")
with open("/tmp/bulkhead-metrics.json") as f:
    avail = json.load(f)["measurements"][0]["value"]
with open("/tmp/ratelimiter-metrics.json") as f:
    rate = json.load(f)["measurements"][0]["value"]
print(f"Bulkhead available calls: {avail}, RateLimiter available perms: {rate}")
PY
echo "OK (Resilience4j metrics)"

echo "All smoke checks passed."

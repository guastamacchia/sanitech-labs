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

# Normalizza URL rimuovendo spazi e slash finali
KEYCLOAK_URL="$(echo "${KEYCLOAK_URL}" | xargs)"
SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
KEYCLOAK_URL="${KEYCLOAK_URL%/}"
SERVICE_URL="${SERVICE_URL%/}"

echo ">> Checking Keycloak health"
HEALTH_ENDPOINTS=(
  "${KEYCLOAK_URL}/health/ready"
  "${KEYCLOAK_URL}/auth/health/ready"
)
HEALTH_OK=""
for endpoint in "${HEALTH_ENDPOINTS[@]}"; do
  for _ in {1..30}; do
    if curl -fsSL "${endpoint}" >/dev/null 2>&1; then
      HEALTH_OK="${endpoint}"
      break
    fi
    sleep 1
  done
  [ -n "${HEALTH_OK}" ] && break
done
if [ -z "${HEALTH_OK}" ]; then
  echo "Keycloak health check failed on tested endpoints:"
  printf '  - %s\n' "${HEALTH_ENDPOINTS[@]}"
  exit 1
fi
echo "OK (${HEALTH_OK})"

echo ">> Requesting token for user '${USERNAME}'"
TOKEN_JSON=$(curl -fsSL -X POST \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}")

ACCESS_TOKEN=$(python - <<'PY'
import json, sys
data = json.loads("""${TOKEN_JSON}""")
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

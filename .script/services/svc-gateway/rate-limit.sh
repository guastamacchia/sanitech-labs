#!/usr/bin/env bash
set -euo pipefail

prompt() {
  local var="$1" label="$2" default="$3"
  read -rp "${label} [${default}]: " input || true
  if [ -z "${input}" ]; then
    eval "${var}=\"${default}\""
  else
    eval "${var}=\"${input}\""
  fi
}

prompt SERVICE_URL "svc-gateway base URL" "${SERVICE_URL:-http://localhost:8080}"

SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
SERVICE_URL="${SERVICE_URL%/}"

metric_url="${SERVICE_URL}/actuator/metrics/resilience4j.ratelimiter.available.permissions"

echo ">> Checking RateLimiter metric at ${metric_url}"
resp=$(curl -s -w "\n%{http_code}" "${metric_url}")
status=$(echo "${resp}" | tail -n1)
body=$(echo "${resp}" | sed '$d')
if [ "${status}" != "200" ]; then
  echo "RateLimiter metric unavailable (status ${status})"
  echo "${body}"
  exit 1
fi
echo "OK (metric available)"

echo ">> Hitting actuator/health twice to observe rate limiting (if configured)"
code1=$(curl -s -o /dev/null -w "%{http_code}" "${SERVICE_URL}/actuator/health")
code2=$(curl -s -o /dev/null -w "%{http_code}" "${SERVICE_URL}/actuator/health")
echo "First call  status: ${code1}"
echo "Second call status: ${code2}"
echo "Rate limit check completed."

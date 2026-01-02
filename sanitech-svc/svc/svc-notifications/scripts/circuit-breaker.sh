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

prompt SERVICE_URL "svc-notifications base URL" "${SERVICE_URL:-http://localhost:8087}"

SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
SERVICE_URL="${SERVICE_URL%/}"

metric_url="${SERVICE_URL}/actuator/metrics/resilience4j.circuitbreaker.state"

echo ">> Checking CircuitBreaker metric at ${metric_url}"
resp=$(curl -s -w "\n%{http_code}" "${metric_url}")
status=$(echo "${resp}" | tail -n1)
body=$(echo "${resp}" | sed '$d')
if [ "${status}" != "200" ]; then
  echo "CircuitBreaker metric unavailable (status ${status})"
  echo "${body}"
  exit 1
fi

echo "CircuitBreaker metric available."

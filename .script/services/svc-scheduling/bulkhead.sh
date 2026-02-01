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

prompt SERVICE_URL "svc-scheduling base URL" "${SERVICE_URL:-http://localhost:8083}"

SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
SERVICE_URL="${SERVICE_URL%/}"

metric_url="${SERVICE_URL}/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls"

echo ">> Checking Bulkhead metric at ${metric_url}"
resp=$(curl -s -w "\n%{http_code}" "${metric_url}")
status=$(echo "${resp}" | tail -n1)
body=$(echo "${resp}" | sed '$d')
if [ "${status}" != "200" ]; then
  echo "Bulkhead metric unavailable (status ${status})"
  echo "${body}"
  exit 1
fi
echo "OK (metric available)"

echo ">> Calling health concurrently to simulate bulkhead pressure"
code1_file=$(mktemp)
code2_file=$(mktemp)

(curl -s -o /dev/null -w "%{http_code}" "${SERVICE_URL}/actuator/health" >"${code1_file}") &
(curl -s -o /dev/null -w "%{http_code}" "${SERVICE_URL}/actuator/health" >"${code2_file}") &
wait

echo "First call  status: $(cat "${code1_file}")"
echo "Second call status: $(cat "${code2_file}")"
rm -f "${code1_file}" "${code2_file}"
echo "Bulkhead check completed."

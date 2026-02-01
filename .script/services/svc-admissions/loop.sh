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

prompt SERVICE_URL "svc-admissions base URL" "${SERVICE_URL:-http://localhost:8084}"
prompt SLEEP_SECONDS "Pause between cycles (seconds)" "${SLEEP_SECONDS:-5}"

SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
SERVICE_URL="${SERVICE_URL%/}"

curl_status() {
  local url="$1"
  curl --silent --output /dev/null --write-out "%{http_code}" "${url}" || echo "ERR"
}

echo ">> Starting loop (Ctrl+C to stop)"
while true; do
  echo "=== $(date '+%Y-%m-%dT%H:%M:%S%z') ==="
  echo "Health  : $(curl_status "${SERVICE_URL}/actuator/health")"
  echo "Metrics : $(curl_status "${SERVICE_URL}/actuator/metrics")"
  echo "Docs    : $(curl_status "${SERVICE_URL}/v3/api-docs")"
  sleep "${SLEEP_SECONDS}"
done

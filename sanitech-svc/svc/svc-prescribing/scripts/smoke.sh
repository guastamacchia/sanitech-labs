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

prompt SERVICE_URL "svc-prescribing base URL" "${SERVICE_URL:-http://localhost:8091}"

SERVICE_URL="$(echo "${SERVICE_URL}" | xargs)"
SERVICE_URL="${SERVICE_URL%/}"

echo ">> Checking health at ${SERVICE_URL}/actuator/health"
curl -fsSL "${SERVICE_URL}/actuator/health" >/dev/null
echo "OK"

echo ">> Checking metrics endpoint"
curl -fsSL "${SERVICE_URL}/actuator/metrics" >/dev/null
echo "OK"

echo ">> Checking OpenAPI at ${SERVICE_URL}/v3/api-docs"
curl -fsSL "${SERVICE_URL}/v3/api-docs" >/dev/null
echo "OK"

echo "All smoke checks passed."

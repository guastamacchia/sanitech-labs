#!/usr/bin/env bash
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
SERVICE_DIR="${ROOT_DIR}/sanitech-svc/svc-directory"
# ENV_FILE can be overridden with a path argument
ENV_FILE="${1:-${ROOT_DIR}/.infra/svc/${SERVICE_DIR##*/}/env/env.remote}"


if [ -f "${ENV_FILE}" ]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
  echo "Carico le variabili d'ambiente dal file ${ENV_FILE}"
else
  echo "Env file '${ENV_FILE}' non trovato."
  exit 1
fi

prompt_once() {
  local label default input
  label="$1"
  default="$2"

  read -rp "${label} [${default}]: " input
  if [ -z "${input}" ]; then
    echo "${default}"
  else
    echo "${input}" | xargs
  fi
}

KEYCLOAK_URL=$(prompt_once "URL base di Keycloak" "${KEYCLOAK_URL:-http://localhost:8081}")
REALM=$(prompt_once "Realm" "${REALM:-sanitech}")
SERVICE_URL=$(prompt_once "URL base di svc-directory" "${SERVICE_URL:-http://localhost:${PORT:-8082}}");
CLIENT_ID=$(prompt_once "Client ID" "${CLIENT_ID:-svc-directory}")
CLIENT_SECRET=$(prompt_once "Client secret" "${CLIENT_SECRET:-svc-directory-secret}")
USERNAME=$(prompt_once "Username" "${USERNAME:-admin}")
PASSWORD=$(prompt_once "Password" "${PASSWORD:-admin}")
SLEEP_SECONDS=$(prompt_once "Pausa tra i cicli (secondi)" "${SLEEP_SECONDS:-5}")

export KEYCLOAK_URL REALM SERVICE_URL CLIENT_ID CLIENT_SECRET USERNAME PASSWORD SLEEP_SECONDS

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Eseguo smoke test..."
/usr/bin/env bash "${DIR}/bash/smoke.sh"

echo "Eseguo bulkhead test..."
/usr/bin/env bash "${DIR}/bash/bulkhead.sh"

echo "Eseguo rate limit test..."
/usr/bin/env bash "${DIR}/bash/rate-limit.sh"

echo "Eseguo loop test..."
/usr/bin/env bash "${DIR}/bash/loop.sh"

echo "Tutti gli script completati."

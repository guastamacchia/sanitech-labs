#!/usr/bin/env bash
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
SERVICE_DIR="${ROOT_DIR}/sanitech-svc/svc-audit"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${1:-${ROOT_DIR}/.infra/svc/${SERVICE_DIR##*/}/env/env.remote}"



if [ -f "${ENV_FILE}" ]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
  echo "Loaded environment from ${ENV_FILE}"
else
  echo "Env file '${ENV_FILE}' not found. Proceeding with current environment."
fi

DIR="${SCRIPT_DIR}"
# exec /usr/bin/env bash "${DIR}/smoke.sh"
# exec /usr/bin/env bash "${DIR}/rate-limit.sh"
# exec /usr/bin/env bash "${DIR}/bulkhead.sh"
# exec /usr/bin/env bash "${DIR}/circuit-breaker.sh"
exec /usr/bin/env bash "${DIR}/loop.sh"

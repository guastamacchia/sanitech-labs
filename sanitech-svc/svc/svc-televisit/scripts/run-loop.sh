#!/usr/bin/env bash
set -uo pipefail

ENV_FILE="${1:-../.env.local}"

if [ -f "${ENV_FILE}" ]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
  echo "Loaded environment from ${ENV_FILE}"
else
  echo "Env file '${ENV_FILE}' not found. Proceeding with current environment."
fi

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# exec /usr/bin/env bash "${DIR}/smoke.sh"
# exec /usr/bin/env bash "${DIR}/rate-limit.sh"
# exec /usr/bin/env bash "${DIR}/bulkhead.sh"
# exec /usr/bin/env bash "${DIR}/circuit-breaker.sh"
exec /usr/bin/env bash "${DIR}/loop.sh"

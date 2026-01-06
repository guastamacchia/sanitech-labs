#!/usr/bin/env bash
set -uo pipefail

ENV_FILE="${1:-../infra/env/env.local}"
#ENV_FILE="${1:-../infra/env/env.staging}"

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

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Eseguo smoke test..."
/usr/bin/env bash "${DIR}/sh/smoke.sh"

echo "Eseguo bulkhead test..."
/usr/bin/env bash "${DIR}/sh/bulkhead.sh"

echo "Eseguo rate limit test..."
/usr/bin/env bash "${DIR}/sh/rate-limit.sh"

echo "Eseguo loop test..."
/usr/bin/env bash "${DIR}/sh/loop.sh"

echo "Tutti gli script completati."

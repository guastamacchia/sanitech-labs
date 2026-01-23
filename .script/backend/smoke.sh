#!/usr/bin/env bash
set -euo pipefail

echo "[smoke] attendo keycloak..."
until curl -fsS http://localhost:8081/health/ready >/dev/null; do sleep 2; done

echo "[smoke] attendo gateway..."
until curl -fsS http://localhost:8080/actuator/health >/dev/null; do sleep 2; done

echo "[smoke] health checks servizi principali..."
curl -fsS http://localhost:8082/actuator/health >/dev/null
curl -fsS http://localhost:8083/actuator/health >/dev/null
curl -fsS http://localhost:8085/actuator/health >/dev/null

echo "[smoke] ok"

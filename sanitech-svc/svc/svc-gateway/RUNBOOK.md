# RUNBOOK — svc-gateway (Sanitech)

## 1) Incident: downstream service non raggiungibile (503 dal gateway)

Sintomi:
- errori 503 su route di un microservizio specifico
- log del gateway con circuit breaker aperto o retry esauriti

Azioni:
1. Verificare health del microservizio downstream.
2. Verificare DNS/service discovery (se in Kubernetes) o URL configurati (se standalone).
3. Controllare timeouts `spring.cloud.gateway.httpclient.*`.
4. Se necessario, aumentare temporaneamente soglie del circuit breaker in `resilience4j.circuitbreaker.instances.*`.

## 2) Incident: Swagger UI non mostra endpoint

Verificare:
1. gli URL in `sanitech.gateway.openapi.targets.*` puntino ai `v3/api-docs/{group}` corretti.
2. i microservizi espongano OpenAPI e che l'endpoint sia raggiungibile dal gateway.
3. `GET /openapi/{service}` (response 200 e JSON valido).

## 3) Sicurezza: 401/403 inattesi

- 401: token mancante o issuer non coerente con `OAUTH2_ISSUER_URI`.
- 403: token valido ma privo dei ruoli/scope necessari (verificare mapper `JwtAuthConverter`).

## 4) Logs/Metriche

- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

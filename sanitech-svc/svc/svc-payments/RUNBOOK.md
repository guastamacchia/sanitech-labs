# RUNBOOK - svc-payments (Sanitech)

## 1) 401/403
- Verificare `OAUTH2_ISSUER_URI`
- Verificare ruolo in token (`realm_access.roles`)
- Verificare claim `pid` per i PATIENT (per listare/leggere i propri pagamenti)

## 2) Outbox cresce e non pubblica
- Verificare `KAFKA_BOOTSTRAP_SERVERS`
- Verificare log del publisher
- Verificare retry/backoff in `resilience4j.retry.instances.outboxPublish`

## 3) Webhook rifiutato (401)
- Verificare header `X-Webhook-Secret`
- Verificare env `SANITECH_PAYMENTS_WEBHOOK_SECRET`

# Sanitech — svc-admissions

Microservizio **Admissions** della piattaforma **Sanitech**.

## Scopo (bounded context)

Questo servizio gestisce:

- **Ricoveri** (ammissioni) dei pazienti in un reparto (`departmentCode`)
- **Disponibilità posti letto** per reparto (capacità configurata da profilo **Admin**)

> Nota: le **visite** (in sede o in tele-visita) sono gestite dal microservizio `svc-scheduling`.
> Questo servizio gestisce **solo** i ricoveri (inpatient/day-hospital/observation).

## Policy “Consenso”

Il **consenso** non viene gestito qui.
Il consenso viene verificato nei servizi clinici (es. cartella clinica/referti) quando un **DOCTOR** tenta di accedere ai dati di un **PAZIENTE**.

## Requisiti

- Java 21
- PostgreSQL 16+
- Kafka (solo per pubblicazione outbox events)
- Keycloak (OIDC) come Identity Provider

## Come eseguire (locale)

1) Avvia stack locale (servizio + dipendenze):
```bash
docker compose -f docker/docker-compose.yml up -d --build
```

2) Per eseguire dal sorgente (senza container), esporta le variabili minime
(o usa i default in `application.yml`), ferma il container `svc-admissions`
e avvia Spring Boot:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/sanitech_admissions
export DB_USER=sanitech
export DB_PASSWORD=sanitech
export OAUTH2_ISSUER_URI=http://localhost:8081/realms/sanitech
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
./mvnw -q spring-boot:run
```

3) Swagger:
- `http://localhost:8084/swagger-ui/index.html`

## Endpoints (high level)

- `GET /api/admissions` (ricerca paginata; rate-limited)
- `POST /api/admissions` (crea un ricovero; DOCTOR/ADMIN con controllo reparto)
- `POST /api/admissions/{id}/discharge` (dimette un paziente)
- `GET /api/departments/capacity` (lista capacità e occupazione)
- `PUT /api/admin/departments/{dept}/capacity` (ADMIN: imposta posti letto)

## Outbox pattern

Ogni operazione rilevante (create/discharge/capacity update) genera un evento nella tabella `outbox_events`.
Un job schedulato pubblica gli eventi su Kafka topic `admissions.events` e marca gli eventi come pubblicati.

## Build & test

```bash
./mvnw -q -DskipTests package
./mvnw -q test
```

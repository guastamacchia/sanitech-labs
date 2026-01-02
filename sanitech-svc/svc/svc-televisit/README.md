# Sanitech — svc-televisit

Microservizio **svc-televisit** della piattaforma **Sanitech**.

Responsabilità principali:

- Gestione delle **sessioni di video-visita** (creazione, consultazione, cambio stato).
- Generazione di **token LiveKit** (JWT) per consentire l’accesso alla room da parte dei partecipanti.
- Pubblicazione di eventi di dominio tramite **Outbox pattern + Kafka** (`televisit.events`).

> Nota consenso: questo microservizio **non** applica la verifica di “consenso clinico” (che viene gestita da `svc-consents` ed applicata nei servizi che accedono a dati clinici del paziente).

---

## Requisiti

- Java 21
- Docker / Docker Compose
- PostgreSQL 16+
- Kafka (dev: docker compose)

---

## Come eseguire (dev)

1) Avvia servizio e dipendenze (Postgres + Kafka + LiveKit):

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

> Per eseguire dal sorgente (senza container), ferma `svc-televisit`
> o avvia solo i servizi di supporto dal compose e poi:

```bash
./mvnw -q test
./mvnw -q spring-boot:run
```

3) Verifica:

- Swagger UI: `http://localhost:8089/swagger-ui/index.html`
- Health: `http://localhost:8089/actuator/health`
- Prometheus: `http://localhost:8089/actuator/prometheus`

---

## Configurazione (variabili d’ambiente principali)

- Database:
  - `DB_URL` (default: `jdbc:postgresql://localhost:5432/sanitech_televisit`)
  - `DB_USER` (default: `sanitech`)
  - `DB_PASSWORD` (default: `sanitech`)

- Keycloak OIDC (Resource Server JWT):
  - `OAUTH2_ISSUER_URI` (default: `http://localhost:8081/realms/sanitech`)

- Kafka:
  - `KAFKA_BOOTSTRAP_SERVERS` (default: `localhost:9092`)

- LiveKit:
  - `LIVEKIT_URL` (default: `http://localhost:7880`)
  - `LIVEKIT_API_KEY`
  - `LIVEKIT_API_SECRET`

---

## API principali (sintesi)

- `GET /api/televisits`  
  Lista/paginazione sessioni (protetta, con rate-limit).

- `POST /api/admin/televisits`  
  Crea una sessione di video-visita (ROLE_ADMIN).

- `POST /api/televisits/{id}/token/doctor`  
  Genera token LiveKit per il medico autenticato (ROLE_DOCTOR o ROLE_ADMIN, con controllo reparto via claim `dept`).

- `POST /api/admin/televisits/{id}/token/patient`  
  Genera token LiveKit per il paziente (ROLE_ADMIN).

---

## Outbox & Kafka

- Tabella: `outbox_events`
- Publisher schedulato: flush periodico, lock ottimistico tramite `FOR UPDATE SKIP LOCKED`
- Topic: `televisit.events`

Metriche principali:

- `outbox.events.saved.count`
- `outbox.events.published`

---

## Profilo `prod`

- `application-prod.yml` usa variabili d’ambiente obbligatorie (DB, issuer, Kafka).
- Logging: `logback-spring.xml` (JSON su stdout).

Esempio:

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://db:5432/sanitech_televisit \
DB_USER=sanitech \
DB_PASSWORD=*** \
OAUTH2_ISSUER_URI=http://keycloak:8081/realms/sanitech \
KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
LIVEKIT_URL=http://livekit:7880 \
LIVEKIT_API_KEY=... \
LIVEKIT_API_SECRET=... \
java -jar target/svc-televisit-1.0.0.jar
```

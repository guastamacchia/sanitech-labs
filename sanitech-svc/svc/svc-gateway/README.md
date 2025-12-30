# Sanitech — svc-gateway

API Gateway della piattaforma **Sanitech**, basato su **Spring Cloud Gateway** (WebFlux).
Il gateway espone:

- routing verso i microservizi backend (Directory, Scheduling, Admissions, Consents, ecc.);
- **validazione JWT** via **Keycloak OIDC** (Spring Security Resource Server);
- **Swagger UI** centralizzata che carica la specifica **OpenAPI “merged”** (unione delle specifiche downstream);
- filtri di resilienza lato gateway (Circuit Breaker + Retry) per degradare in modo controllato.

## Requisiti

- Java 21+
- Docker (opzionale, consigliato)
- Un Keycloak disponibile (dev: `http://localhost:8081/realms/sanitech`)

## Come eseguire (3 comandi)

```bash
# 1) build
./mvnw -q -DskipTests package || mvn -q -DskipTests package

# 2) run
./mvnw -q spring-boot:run || mvn -q spring-boot:run

# 3) smoke
curl -i http://localhost:8080/actuator/health
```

### Esecuzione con Docker Compose (dev)

```bash
docker compose -f docker-compose.dev.yml up --build
```

## Endpoint utili

- Health: `GET /actuator/health`
- Swagger UI “locked-down”: `GET /swagger.html`
- Swagger UI (springdoc): `GET /swagger-ui/index.html`
- OpenAPI aggregata (merged): `GET /openapi/merged`
- OpenAPI per servizio: `GET /openapi/{service}` (es. `/openapi/directory`)

## Sicurezza (Keycloak OIDC)

Il gateway è configurato come **Resource Server**:
- valida i JWT emessi da Keycloak (issuer configurabile via `OAUTH2_ISSUER_URI`);
- mappa le autorizzazioni in `GrantedAuthority` secondo le regole:

| Claim JWT | Authority risultante |
|---|---|
| `realm_access.roles` | `ROLE_*` |
| `scope` | `SCOPE_*` |
| `dept` (custom) | `DEPT_*` |

> Nota: il gateway **non** applica regole ABAC di reparto (DEPT) sui singoli endpoint: la policy
> è demandata ai microservizi di dominio (es. `svc-directory` su endpoint admin / gestione).

## OpenAPI Aggregator

Il gateway implementa un piccolo “aggregatore” che:
1. carica le specifiche OpenAPI dei microservizi da URL **configurati** (whitelist);
2. esegue una merge dei `paths` e dei `components`;
3. applica un namespacing sui component per ridurre collisioni tra servizi.

Gli URL sono definiti in `application.yml` sotto `sanitech.gateway.openapi.targets.*`
e possono essere sovrascritti via env (es. `OPENAPI_DIRECTORY_URL`).

## Configurazione

Le proprietà principali sono in `src/main/resources/application.yml`.
Per produzione, usare `application-prod.yml` con variabili d’ambiente (issuer-uri e URL servizi).

## Licenza

MIT.

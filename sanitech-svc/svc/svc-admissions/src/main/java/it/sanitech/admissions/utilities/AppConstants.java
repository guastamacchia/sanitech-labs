package it.sanitech.admissions.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Raccolta di costanti applicative del microservizio {@code svc-admissions}.
 *
 * <p>
 * Le costanti sono centralizzate qui per:
 * <ul>
 *   <li>evitare stringhe cablate nel codice;</li>
 *   <li>mantenere uniformità tra moduli (security, error handling, OpenAPI, outbox);</li>
 *   <li>facilitare refactoring e manutenzione.</li>
 * </ul>
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    /**
     * Costanti per OpenAPI / Swagger.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class OpenApi {
        public static final String GROUP = "admissions";
        public static final String PACKAGES_TO_SCAN = "it.sanitech.admissions.web";
        public static final String TITLE = "Sanitech — Admissions API";
        public static final String VERSION = "v1";
    }

    /**
     * Costanti RFC 7807 (Problem Details).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Problem {
        public static final String TYPE_BAD_REQUEST = "https://sanitech.it/problems/bad-request";
        public static final String TYPE_NOT_FOUND = "https://sanitech.it/problems/not-found";
        public static final String TYPE_CONFLICT = "https://sanitech.it/problems/conflict";
        public static final String TYPE_TOO_MANY_REQUESTS = "https://sanitech.it/problems/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = "https://sanitech.it/problems/service-unavailable";
        public static final String TYPE_INTERNAL_ERROR = "https://sanitech.it/problems/internal-error";
        public static final String TYPE_VALIDATION_ERROR = "https://sanitech.it/problems/validation-error";
        public static final String TYPE_ACCESS_DENIED = "https://sanitech.it/problems/access-denied";
        public static final String TYPE_NO_BEDS = "https://sanitech.it/problems/no-beds-available";
    }

    /**
     * Messaggi di errore (in italiano) utilizzati nelle risposte standardizzate.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorMessage {
        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_BAD_REQUEST = "Richiesta non valida";
        public static final String ERR_CONFLICT = "Conflitto";
        public static final String ERR_VALIDATION = "Errore di validazione";
        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio non disponibile";
        public static final String ERR_INTERNAL = "Errore interno";
        public static final String ERR_ACCESS_DENIED = "Accesso negato";
        public static final String ERR_NO_BEDS = "Posti letto non disponibili";

        public static final String MSG_VALIDATION_FAILED = "Uno o più campi non sono validi.";
        public static final String MSG_TOO_MANY_REQUESTS = "Limite di richieste superato. Riprovare più tardi.";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile. Riprovare più tardi.";
        public static final String MSG_INTERNAL_ERROR = "Si è verificato un errore inatteso.";
    }

    /**
     * Costanti di sicurezza (claims/authorities).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Security {
        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";
        public static final String CLAIM_DEPT = "dept";

        public static final String AUTH_PREFIX_ROLE = "ROLE_";
        public static final String AUTH_PREFIX_SCOPE = "SCOPE_";
        public static final String AUTH_PREFIX_DEPT = "DEPT_";

        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_DOCTOR = "ROLE_DOCTOR";
        public static final String ROLE_PATIENT = "ROLE_PATIENT";
    }

    /**
     * Costanti di configurazione (keys) esposte via {@code application.yml}.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ConfigKeys {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Cors {
            public static final String PREFIX = "sanitech.cors";
            public static final String PATH_PATTERNS = PREFIX + ".path-patterns";
            public static final String ALLOWED_ORIGINS = PREFIX + ".allowed-origins";
            public static final String ALLOWED_METHODS = PREFIX + ".allowed-methods";
            public static final String ALLOWED_HEADERS = PREFIX + ".allowed-headers";
            public static final String EXPOSED_HEADERS = PREFIX + ".exposed-headers";
            public static final String ALLOW_CREDENTIALS = PREFIX + ".allow-credentials";
            public static final String MAX_AGE = PREFIX + ".max-age";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Outbox {
            public static final String PUBLISHER_DELAY_MS = "sanitech.outbox.publisher.delay-ms";
        }
    }

    /**
     * Valori di default per configurazioni opzionali.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ConfigDefaultValue {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Cors {
            public static final long MAX_AGE_SECONDS = 3600;
            public static final boolean ALLOW_CREDENTIALS = false;
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Outbox {
            public static final long PUBLISHER_DELAY_MS = 1000;
        }
    }



    /**
     * Whitelist di campi ordinabili via API (protezione per sorting/paging).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SortFields {
        public static final java.util.Set<String> ADMISSIONS = java.util.Set.of(
                "id",
                "patientId",
                "departmentCode",
                "admissionType",
                "status",
                "admittedAt",
                "dischargedAt"
        );
    }

    /**
     * Keys di proprietà Spring usate frequentemente.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Spring {
        public static final String KEY_APP_NAME = "spring.application.name";
        public static final String DEFAULT_APP_NAME = "svc-admissions";

        public static final String KEY_SERVER_PORT = "server.port";
        public static final String DEFAULT_SERVER_PORT = "8084";
    }

    /**
     * Costanti Outbox/Kafka/metriche.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Outbox {
        public static final String TOPIC_ADMISSIONS_EVENTS = "admissions.events";

        public static final String METRIC_OUTBOX_SAVED = "outbox.events.saved.count";
        public static final String METRIC_OUTBOX_PUBLISHED = "outbox.events.published";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";
    }
}

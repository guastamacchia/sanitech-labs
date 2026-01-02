package it.sanitech.consents.utilities;

/**
 * Costanti applicative del microservizio {@code svc-consents}.
 * <p>
 * Convenzione: tutte le stringhe "stabili" (path, claim, nomi metriche, ecc.)
 * sono centralizzate qui per evitare duplicazioni e refusi.
 * </p>
 */
public final class AppConstants {

    private AppConstants() {
        // utility class
    }

    /**
     * Chiavi Spring/Spring Boot usate frequentemente.
     */
    public static final class Spring {
        private Spring() {}

        /** Property key: {@code spring.application.name}. */
        public static final String APP_NAME_KEY = "spring.application.name";
        /** Default: {@code svc-consents}. */
        public static final String APP_NAME_DEFAULT = "svc-consents";

        /** Property key: {@code server.port}. */
        public static final String SERVER_PORT_KEY = "server.port";
        /** Default: {@code 8085}. */
        public static final String SERVER_PORT_DEFAULT = "8085";
    }

    /**
     * Path API esposti dal servizio.
     */
    public static final class ApiPath {
        private ApiPath() {}

        public static final String API_BASE = "/api";
        public static final String CONSENTS = API_BASE + "/consents";
        public static final String CONSENTS_ME = CONSENTS + "/me";
        public static final String CONSENTS_CHECK = CONSENTS + "/check";

        public static final String ADMIN_BASE = API_BASE + "/admin";
        public static final String ADMIN_CONSENTS = ADMIN_BASE + "/consents";
    }

    /**
     * Claim JWT e prefissi authority.
     */
    public static final class Security {
        private Security() {}

        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";
        public static final String CLAIM_DEPT = "dept";

        /** Claim custom: patient id (se presente nel token). */
        public static final String CLAIM_PATIENT_ID = "pid";
        /** Claim custom: doctor id (se presente nel token). */
        public static final String CLAIM_DOCTOR_ID = "did";

        public static final String PREFIX_ROLE = "ROLE_";
        public static final String PREFIX_SCOPE = "SCOPE_";
        public static final String PREFIX_DEPT = "DEPT_";

        /**
         * Endpoint pubblici (non autenticati), tipicamente health + Swagger.
         */
        public static final String[] PUBLIC_ENDPOINTS = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/health/**"
        };
    }


    /**
     * OpenAPI/Swagger.
     */
    public static final class OpenApi {
        private OpenApi() {}

        public static final String GROUP_DIRECTORY = "consents";
        public static final String PACKAGES_TO_SCAN = "it.sanitech.consents.web";
        public static final String TITLE = "Sanitech — Consents API";
        public static final String VERSION = "v1";
    }

    /**
     * Outbox & Kafka.
     */
    public static final class Outbox {
        private Outbox() {}

        public static final String TOPIC_CONSENTS_EVENTS = "consents.events";

        /** Micrometer: contatore eventi outbox salvati. */
        public static final String METRIC_OUTBOX_SAVED = "outbox.events.saved.count";
        /** Micrometer: contatore eventi outbox pubblicati. */
        public static final String METRIC_OUTBOX_PUBLISHED = "outbox.events.published.count";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";
    }

    /**
     * RFC 7807 "Problem Details": valori type e messaggi.
     */
    public static final class Problem {
        private Problem() {}

        public static final String TYPE_BAD_REQUEST = "https://sanitech.it/problems/bad-request";
        public static final String TYPE_NOT_FOUND = "https://sanitech.it/problems/not-found";
        public static final String TYPE_CONFLICT = "https://sanitech.it/problems/conflict";
        public static final String TYPE_FORBIDDEN = "https://sanitech.it/problems/forbidden";
        public static final String TYPE_TOO_MANY_REQUESTS = "https://sanitech.it/problems/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = "https://sanitech.it/problems/service-unavailable";
        public static final String TYPE_INTERNAL_ERROR = "https://sanitech.it/problems/internal-error";
        public static final String TYPE_VALIDATION_ERROR = "https://sanitech.it/problems/validation-error";
    }

    public static final class ErrorMessage {
        private ErrorMessage() {}

        public static final String ERR_VALIDATION = "Richiesta non valida";
        public static final String MSG_VALIDATION_FAILED = "La validazione dei campi non è andata a buon fine.";

        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_CONFLICT = "Conflitto sullo stato della risorsa";
        public static final String ERR_FORBIDDEN = "Operazione non consentita";

        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste consentite. Riprova più tardi.";

        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio momentaneamente non disponibile. Riprova più tardi.";

        public static final String ERR_INTERNAL = "Errore interno";
        public static final String MSG_INTERNAL = "Si è verificato un errore interno inatteso.";
    }
}

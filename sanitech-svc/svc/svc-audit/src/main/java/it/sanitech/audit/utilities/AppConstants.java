package it.sanitech.audit.utilities;

/**
 * Costanti applicative del microservizio {@code svc-audit}.
 */
public final class AppConstants {

    private AppConstants() {}

    public static final class Spring {
        private Spring() {}

        public static final String APP_NAME_KEY = "spring.application.name";
        public static final String APP_NAME_DEFAULT = "svc-audit";

        public static final String SERVER_PORT_KEY = "server.port";
        public static final String SERVER_PORT_DEFAULT = "8085";
    }

    public static final class ApiPath {
        private ApiPath() {}

        public static final String API_BASE = "/api";
        public static final String AUDIT_EVENTS = API_BASE + "/audit/events";
    }

    public static final class Security {
        private Security() {}

        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";

        public static final String PREFIX_ROLE = "ROLE_";
        public static final String PREFIX_SCOPE = "SCOPE_";

        public static final String[] PUBLIC_ENDPOINTS = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/health/**"
        };
    }

    public static final class OpenApi {
        private OpenApi() {}

        public static final String GROUP = "audit";
        public static final String PACKAGES_TO_SCAN = "it.sanitech.audit.web";
        public static final String TITLE = "Sanitech — Audit API";
        public static final String VERSION = "v1";
    }

    public static final class Outbox {
        private Outbox() {}

        public static final String TOPIC_AUDIT_EVENTS = "audit.events";

        public static final String METRIC_OUTBOX_SAVED = "outbox.events.saved.count";
        public static final String METRIC_OUTBOX_PUBLISHED = "outbox.events.published.count";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";
    }

    public static final class Audit {
        private Audit() {}

        /** Metric: eventi audit salvati. */
        public static final String METRIC_AUDIT_EVENTS_SAVED = "audit.events.saved.count";

        public static final String SOURCE_API = "api";
        public static final String SOURCE_KAFKA = "kafka";

        public static final String OUTCOME_SUCCESS = "SUCCESS";
        public static final String OUTCOME_DENIED = "DENIED";
        public static final String OUTCOME_FAILURE = "FAILURE";
    }

    public static final class Problem {
        private Problem() {}

        public static final String TYPE_BAD_REQUEST = "https://sanitech.it/problems/bad-request";
        public static final String TYPE_NOT_FOUND = "https://sanitech.it/problems/not-found";
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
        public static final String ERR_FORBIDDEN = "Operazione non consentita";

        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste consentite. Riprova più tardi.";

        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio momentaneamente non disponibile. Riprova più tardi.";

        public static final String ERR_INTERNAL = "Errore interno";
        public static final String MSG_INTERNAL = "Si è verificato un errore interno inatteso.";
    }
}

package it.sanitech.televisit.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Costanti applicative centralizzate del microservizio.
 *
 * <p>Convenzioni:
 * <ul>
 *   <li>Stringhe “di protocollo” (path, claim, topic, metriche) in un solo punto;</li>
 *   <li>Nessun valore “cablato” in controller/service (salvo default sensati lato config).</li>
 * </ul>
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class OpenApi {
        public static final String GROUP = "televisit";
        public static final String PACKAGES_TO_SCAN = "it.sanitech.televisit.web";
        public static final String TITLE = "Sanitech — Televisit API";
        public static final String VERSION = "v1";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Security {
        public static final String[] PUBLIC_MATCHERS = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/health/**"
        };

        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";
        public static final String CLAIM_DEPT = "dept";

        public static final String ROLE_PREFIX = "ROLE_";
        public static final String SCOPE_PREFIX = "SCOPE_";
        public static final String DEPT_PREFIX = "DEPT_";

        public static final String ROLE_ADMIN = "ROLE_ADMIN";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Outbox {
        public static final String TOPIC_TELEVISIT_EVENTS = "televisit.events";

        public static final String METRIC_EVENTS_SAVED = "outbox.events.saved.count";
        public static final String METRIC_EVENTS_PUBLISHED = "outbox.events.published";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Problem {
        public static final String TYPE_NOT_FOUND = "https://sanitech.it/problems/not-found";
        public static final String TYPE_VALIDATION_ERROR = "https://sanitech.it/problems/validation-error";
        public static final String TYPE_FORBIDDEN = "https://sanitech.it/problems/forbidden";
        public static final String TYPE_TOO_MANY_REQUESTS = "https://sanitech.it/problems/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = "https://sanitech.it/problems/service-unavailable";
        public static final String TYPE_INTERNAL_ERROR = "https://sanitech.it/problems/internal-error";
        public static final String TYPE_BAD_REQUEST = "https://sanitech.it/problems/bad-request";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorMessage {
        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_VALIDATION = "Validazione non riuscita";
        public static final String ERR_FORBIDDEN = "Operazione non consentita";
        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio non disponibile";
        public static final String ERR_INTERNAL = "Errore interno";
        public static final String ERR_BAD_REQUEST = "Richiesta non valida";

        public static final String MSG_VALIDATION_FAILED = "La richiesta contiene campi non validi.";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste consentite. Riprova più tardi.";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile. Riprova più tardi.";
        public static final String MSG_INTERNAL_ERROR = "Si è verificato un errore inatteso.";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Sort {
        /**
         * Campo di ordinamento di default quando il client richiede un sort non permesso.
         */
        public static final String DEFAULT_FIELD = "id";

        /**
         * Whitelist di campi ordinabili per le sessioni televisit.
         */
        public static final java.util.Set<String> TELEVISIT_SESSION_FIELDS = java.util.Set.of(
                "id", "roomName", "department", "scheduledAt", "status", "createdAt"
        );
    }

}

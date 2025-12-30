package it.sanitech.payments.utilities;

import lombok.experimental.UtilityClass;

/**
 * Costanti applicative del microservizio {@code svc-payments}.
 *
 * <p>
 * Convenzione: tutte le stringhe “di sistema” (path, topic, claim, metriche, ecc.)
 * sono centralizzate qui per evitare duplicazioni e cablaggi sparsi nel codice.
 * </p>
 */
@UtilityClass
public class AppConstants {

    @UtilityClass
    public static class ConfigKeys {

        @UtilityClass
        public static class Spring {
            /** Property key: spring.application.name */
            public static final String APPLICATION_NAME = "spring.application.name";
        }

        @UtilityClass
        public static class Server {
            /** Property key: server.port */
            public static final String PORT = "server.port";
        }

        @UtilityClass
        public static class Headers {
            /** Header di correlazione richieste. */
            public static final String X_REQUEST_ID = "X-Request-Id";

            /** Header per idempotency: evita doppie creazioni in caso di retry client. */
            public static final String X_IDEMPOTENCY_KEY = "X-Idempotency-Key";

            /** Header per endpoint webhook provider. */
            public static final String X_WEBHOOK_SECRET = "X-Webhook-Secret";
        }
    }

    @UtilityClass
    public static class ConfigDefaults {
        @UtilityClass
        public static class Spring {
            public static final String APPLICATION_NAME = "svc-payments";
        }

        @UtilityClass
        public static class Server {
            public static final String PORT = "8091";
        }
    }

    @UtilityClass
    public static class Api {
        public static final String API_BASE = "/api";
        public static final String ADMIN_BASE = "/api/admin";
        public static final String WEBHOOK_BASE = "/api/webhooks";

        public static final String PAYMENTS = "/payments";
    }

    @UtilityClass
    public static class OpenApi {
        public static final String GROUP = "payments";
        public static final String PACKAGES_TO_SCAN = "it.sanitech.payments.web";
        public static final String TITLE = "Sanitech — Payments API";
        public static final String VERSION = "v1";
    }

    @UtilityClass
    public static class Security {
        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";
        public static final String CLAIM_DEPT = "dept";

        /** Claim per identificare il paziente (Long o String numerico). */
        public static final String CLAIM_PATIENT_ID = "pid";

        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_PATIENT = "ROLE_PATIENT";

        public static final String AUTHORITY_PREFIX_ROLE = "ROLE_";
        public static final String AUTHORITY_PREFIX_SCOPE = "SCOPE_";
        public static final String AUTHORITY_PREFIX_DEPT = "DEPT_";

        /**
         * Endpoint pubblici (no auth) – tipicamente documentazione e health probe.
         * Nota: gli altri actuator restano protetti.
         */
        public static final String[] PUBLIC_ENDPOINTS = new String[] {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/health/**"
        };
    }

    @UtilityClass
    public static class Payments {
        public static final String PROVIDER_MANUAL = "MANUAL";
    }

    @UtilityClass
    public static class Outbox {
        public static final String TOPIC_PAYMENTS_EVENTS = "payments.events";

        public static final String OUTBOX_EVENTS_SAVED_COUNT = "outbox.events.saved.count";
        public static final String OUTBOX_EVENTS_PUBLISHED = "outbox.events.published";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";

        public static final String AGGREGATE_TYPE_PAYMENT = "PAYMENT_ORDER";

        public static final String EVT_CREATED = "PAYMENT_CREATED";
        public static final String EVT_STATUS_CHANGED = "PAYMENT_STATUS_CHANGED";
    }

    @UtilityClass
    public static class Problem {
        public static final String TYPE_NOT_FOUND = "https://sanitech.it/problems/not-found";
        public static final String TYPE_VALIDATION_ERROR = "https://sanitech.it/problems/validation-error";
        public static final String TYPE_BAD_REQUEST = "https://sanitech.it/problems/bad-request";
        public static final String TYPE_TOO_MANY_REQUESTS = "https://sanitech.it/problems/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = "https://sanitech.it/problems/service-unavailable";
        public static final String TYPE_FORBIDDEN = "https://sanitech.it/problems/forbidden";
        public static final String TYPE_UNAUTHORIZED = "https://sanitech.it/problems/unauthorized";
        public static final String TYPE_INTERNAL_ERROR = "https://sanitech.it/problems/internal-error";
    }

    @UtilityClass
    public static class ErrorMessage {
        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_VALIDATION = "Richiesta non valida";
        public static final String ERR_BAD_REQUEST = "Parametri non validi";
        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio non disponibile";
        public static final String ERR_FORBIDDEN = "Operazione non consentita";
        public static final String ERR_UNAUTHORIZED = "Non autenticato";
        public static final String ERR_INTERNAL = "Errore interno";

        public static final String MSG_VALIDATION_FAILED = "La richiesta contiene campi non validi. Verificare i dettagli.";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste. Riprova più tardi.";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile. Riprova più tardi.";
        public static final String MSG_INTERNAL_ERROR = "Si è verificato un errore imprevisto.";
        public static final String MSG_ACCESS_DENIED = "Non sei autorizzato ad accedere a questo pagamento.";
        public static final String MSG_WEBHOOK_UNAUTHORIZED = "Webhook non autorizzato: secret non valido.";
    }

    @UtilityClass
    public static class Sort {
        public static final String DEFAULT_FIELD = "createdAt";
    }
}

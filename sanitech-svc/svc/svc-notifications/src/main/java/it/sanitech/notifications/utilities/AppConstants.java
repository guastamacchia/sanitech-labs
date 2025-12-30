package it.sanitech.notifications.utilities;

/**
 * Costanti applicative centralizzate del microservizio.
 *
 * <p>
 * Obiettivi:
 * <ul>
 *   <li>evitare stringhe “magiche” sparse nel codice;</li>
 *   <li>garantire coerenza di naming (topic Kafka, metriche, tipi di errore);</li>
 *   <li>rendere più semplice la manutenzione e la revisione (audit) del sorgente.</li>
 * </ul>
 * </p>
 */
public final class AppConstants {

    private AppConstants() {
        // utility class
    }

    /**
     * Costanti generali applicative.
     */
    public static final class Application {
        public static final String PROP_SPRING_APP_NAME = "spring.application.name";
        public static final String PROP_SERVER_PORT = "server.port";

        public static final String DEFAULT_APP_NAME = "svc-notifications";
        public static final String DEFAULT_SERVER_PORT = "8087";

        private Application() { }
    }

    /**
     * OpenAPI / Swagger.
     */
    public static final class OpenApi {
        public static final String GROUP = "notifications";
        public static final String PACKAGES_TO_SCAN = "it.sanitech.notifications.web";
        public static final String TITLE = "Sanitech — Notifications API";
        public static final String VERSION = "v1";

        private OpenApi() { }
    }

    /**
     * Sicurezza e autorizzazioni.
     */
    public static final class Security {
        public static final String ROLE_ADMIN = "ROLE_ADMIN";

        // Endpoint pubblici (documentazione e health)
        public static final String[] PUBLIC_MATCHERS = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/health/**"
        };

        private Security() { }
    }

    /**
     * Namespace del vendor per Problem Details RFC 7807.
     */
    public static final class Problem {
        public static final String TYPE_BAD_REQUEST = "https://sanitech.example/problems/bad-request";
        public static final String TYPE_VALIDATION = "https://sanitech.example/problems/validation-error";
        public static final String TYPE_NOT_FOUND = "https://sanitech.example/problems/not-found";
        public static final String TYPE_FORBIDDEN = "https://sanitech.example/problems/forbidden";
        public static final String TYPE_TOO_MANY_REQUESTS = "https://sanitech.example/problems/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = "https://sanitech.example/problems/service-unavailable";
        public static final String TYPE_INTERNAL_ERROR = "https://sanitech.example/problems/internal-error";

        private Problem() { }
    }

    /**
     * Messaggi di errore (italiano) da esporre verso i client.
     */
    public static final class ErrorMessage {
        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_VALIDATION = "Richiesta non valida";
        public static final String ERR_BAD_REQUEST = "Richiesta non valida";
        public static final String ERR_FORBIDDEN = "Operazione non consentita";
        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile";
        public static final String ERR_INTERNAL = "Errore interno";

        public static final String MSG_VALIDATION_FAILED = "La validazione della richiesta non è andata a buon fine.";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste consentite. Riprova più tardi.";
        public static final String MSG_SERVICE_UNAVAILABLE = "Il servizio è temporaneamente non disponibile. Riprova più tardi.";
        public static final String MSG_INTERNAL_ERROR = "Si è verificato un errore imprevisto.";

        private ErrorMessage() { }
    }

    /**
     * Costanti per la funzionalità Outbox.
     */
    public static final class Outbox {
        // Kafka
        public static final String TOPIC_NOTIFICATIONS_EVENTS = "notifications.events";

        // Metriche Micrometer
        public static final String METRIC_OUTBOX_SAVED = "outbox.events.saved.count";
        public static final String METRIC_OUTBOX_PUBLISHED = "outbox.events.published.count";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";

        private Outbox() { }
    }

    /**
     * Costanti del dominio Notifications.
     */
    public static final class Notifications {
        // Whitelist dei campi ordinabili esposti via API (protezione da sort injection)
        public static final String SORT_ID = "id";
        public static final String SORT_CREATED_AT = "createdAt";
        public static final String SORT_STATUS = "status";

        private Notifications() { }
    }
}

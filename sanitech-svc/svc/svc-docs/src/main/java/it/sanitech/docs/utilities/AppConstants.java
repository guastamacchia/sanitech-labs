package it.sanitech.docs.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Raccolta centralizzata di costanti applicative del microservizio <strong>svc-docs</strong>.
 *
 * <p>
 * Linea guida: evitare stringhe "magic" disperse nel codice e mantenere
 * titoli, path, type RFC 7807, metriche e topic in un unico punto.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    /** Costanti di dominio/servizio (nome gruppo OpenAPI, titolo, ecc.). */
    public static final class Service {
        public static final String OPENAPI_GROUP = "docs";
        public static final String OPENAPI_TITLE = "Sanitech — Docs API";
        public static final String OPENAPI_VERSION = "v1";

        private Service() {}
    }

    /** Path HTTP esposti dal servizio. */
    public static final class Api {
        public static final String BASE = "/api";
        public static final String DOCS = BASE + "/docs";
        public static final String ADMIN_DOCS = BASE + "/admin/docs";

        private Api() {}
    }

    /** Costanti security (claim e prefissi authorities). */
    public static final class Security {
        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";
        public static final String CLAIM_DEPT = "dept";
        public static final String CLAIM_PATIENT_ID = "pid";

        public static final String AUTH_ROLE_PREFIX = "ROLE_";
        public static final String AUTH_SCOPE_PREFIX = "SCOPE_";
        public static final String AUTH_DEPT_PREFIX = "DEPT_";

        /** Endpoint pubblici (Swagger/Actuator) accessibili senza autenticazione. */
        public static final String[] PUBLIC_ENDPOINTS = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/health/**",
                "/actuator/info"
        };

        private Security() {}
    }

    /** Costanti per configurazioni esterne. */
    public static final class ConfigKeys {

        /** Chiavi configurazione CORS (sezione {@code sanitech.cors.*}). */
        public static final class Cors {
            public static final String PREFIX = "sanitech.cors";
            private Cors() {}
        }

        /** Chiavi configurazione Outbox (sezione {@code sanitech.outbox.*}). */
        public static final class Outbox {
            public static final String PREFIX = "sanitech.outbox";
            public static final String PUBLISHER_DELAY = PREFIX + ".publisher.delay-ms";
            private Outbox() {}
        }

        /** Chiavi configurazione S3/MinIO (sezione {@code sanitech.docs.s3.*}). */
        public static final class S3 {
            public static final String PREFIX = "sanitech.docs.s3";
            private S3() {}
        }

        /** Chiavi configurazione integrazioni (es. svc-consents). */
        public static final class Integrations {
            public static final String CONSENTS_BASE_URL = "sanitech.consents.base-url";
            private Integrations() {}
        }

        private ConfigKeys() {}
    }

    /** Valori di default per le configurazioni (usati come fallback). */
    public static final class ConfigDefaultValues {

        public static final class Outbox {
            public static final String PUBLISHER_DELAY_MS = "1000";
            private Outbox() {}
        }

        public static final class Cors {
            public static final boolean ALLOW_CREDENTIALS = false;
            public static final long MAX_AGE_SECONDS = 3600L;
            private Cors() {}
        }

        private ConfigDefaultValues() {}
    }

    /** RFC 7807: "type" e titoli (in italiano) per Problem Details. */
    public static final class Problem {
        public static final String TYPE_NOT_FOUND = "https://sanitech.it/problems/not-found";
        public static final String TYPE_VALIDATION_ERROR = "https://sanitech.it/problems/validation-error";
        public static final String TYPE_BAD_REQUEST = "https://sanitech.it/problems/bad-request";
        public static final String TYPE_INTERNAL_ERROR = "https://sanitech.it/problems/internal-error";
        public static final String TYPE_TOO_MANY_REQUESTS = "https://sanitech.it/problems/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = "https://sanitech.it/problems/service-unavailable";
        public static final String TYPE_ACCESS_DENIED = "https://sanitech.it/problems/access-denied";
        public static final String TYPE_CONSENT_REQUIRED = "https://sanitech.it/problems/consent-required";

        private Problem() {}
    }

    /** Messaggi di errore user-friendly (in italiano). */
    public static final class ErrorMessage {
        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_VALIDATION = "Richiesta non valida";
        public static final String ERR_BAD_REQUEST = "Parametri non validi";
        public static final String ERR_INTERNAL = "Errore interno";

        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String MSG_TOO_MANY_REQUESTS = "Limite di richieste superato. Riprova tra poco.";

        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile";
        public static final String MSG_SERVICE_UNAVAILABLE = "Il servizio è momentaneamente non disponibile. Riprova più tardi.";

        public static final String ERR_ACCESS_DENIED = "Accesso negato";
        public static final String MSG_ACCESS_DENIED = "Non sei autorizzato ad eseguire questa operazione.";

        public static final String ERR_CONSENT_REQUIRED = "Consenso mancante";
        public static final String MSG_CONSENT_REQUIRED = "Impossibile accedere ai documenti: consenso del paziente non presente o non valido.";

        private ErrorMessage() {}
    }


    /** Costanti specifiche del dominio documentale. */
    public static final class Docs {
        /** Campi ammessi per l'ordinamento in query (whitelist). */
        public static final String[] ALLOWED_SORT_FIELDS = {"id", "createdAt", "fileName", "sizeBytes", "documentType", "departmentCode"};
        /** Campo di default se il client richiede un ordinamento non ammesso. */
        public static final String DEFAULT_SORT_FIELD = "createdAt";
        private Docs() {}
    }

    /** Metriche e topic relativi al pattern Outbox. */
    public static final class Outbox {
        public static final String TOPIC_DOCS_EVENTS = "docs.events";

        public static final String OUTBOX_EVENTS_SAVED = "outbox.events.saved.count";
        public static final String OUTBOX_EVENTS_PUBLISHED = "outbox.events.published";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";

        private Outbox() {}
    }
}

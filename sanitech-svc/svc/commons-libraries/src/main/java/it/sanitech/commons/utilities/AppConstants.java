package it.sanitech.commons.utilities;

import lombok.experimental.UtilityClass;

/**
 * Collezione di costanti applicative del microservizio <b>Sanitech — svc-directory</b>.
 *
 * <p>
 * Le costanti sono raggruppate per ambito (configurazione, sicurezza, OpenAPI, Outbox, ecc.)
 * per ridurre le stringhe "cablata" nel codice e mantenere coerenza tra i layer.
 * </p>
 */
@UtilityClass
public class AppConstants {

    /**
     * Costanti generali dell'applicazione (chiavi environment/property e default).
     */
    @UtilityClass
    public static class App {
        /** Chiave property Spring per il nome applicativo. */
        public static final String SPRING_APPLICATION_NAME_KEY = "spring.application.name";

        /** Chiave property Spring per la porta HTTP. */
        public static final String SERVER_PORT_KEY = "server.port";

        /** Default applicativo usato se {@code spring.application.name} non è definito. */
        public static final String DEFAULT_APP_NAME = "svc-directory";

        /** Default porta usata se {@code server.port} non è definito. */
        public static final String DEFAULT_SERVER_PORT = "8080";
    }

    /**
     * Chiavi di configurazione (custom namespace {@code sanitech.*}) utilizzate nei file YAML.
     */
    @UtilityClass
    public static class ConfigKeys {

        @UtilityClass
        public static class Cors {
            public static final String PREFIX = "sanitech.cors";
        }

        @UtilityClass
        public static class Outbox {
            public static final String PREFIX = "sanitech.outbox";
            public static final String PUBLISHER_DELAY_MS = PREFIX + ".publisher.delay-ms";
        }
    }

    /**
     * Valori di default per configurazioni applicative (usati in assenza di property esplicite).
     */
    @UtilityClass
    public static class ConfigDefaultValue {

        @UtilityClass
        public static class Cors {
            /** Default: non inviare cookie/credenziali nelle richieste CORS. */
            public static final boolean ALLOW_CREDENTIALS = false;

            /** Default: cache preflight per 1h. */
            public static final long MAX_AGE_SECONDS = 3600;
        }

        @UtilityClass
        public static class Outbox {
            /** Default: job Outbox ogni 1000ms. */
            public static final long PUBLISHER_DELAY_MS = 1000;
        }
    }

    /**
     * Path REST e prefissi comuni.
     */
    @UtilityClass
    public static class ApiPath {
        public static final String API = "/api";
        public static final String ADMIN = API + "/admin";

        public static final String DOCTORS = API + "/doctors";
        public static final String PATIENTS = API + "/patients";
        public static final String DEPARTMENTS = API + "/departments";
        public static final String SPECIALIZATIONS = API + "/specializations";

        public static final String ADMIN_DOCTORS = ADMIN + "/doctors";
        public static final String ADMIN_PATIENTS = ADMIN + "/patients";
        public static final String ADMIN_DEPARTMENTS = ADMIN + "/departments";
        public static final String ADMIN_SPECIALIZATIONS = ADMIN + "/specializations";

        public static final String BULK = "/_bulk";
        public static final String EXPORT = "/_export";
    }

    /**
     * Sicurezza: endpoint pubblici (es. Swagger/Actuator) e claim/authority.
     */
    @UtilityClass
    public static class Security {
        /** Prefisso authority per i ruoli applicativi derivati da {@code realm_access.roles}. */
        public static final String ROLE_PREFIX = "ROLE_";

        /** Prefisso authority per gli scope OAuth2 derivati da {@code scope}. */
        public static final String SCOPE_PREFIX = "SCOPE_";

        /** Prefisso authority per ABAC di reparto derivato dal claim custom {@code dept}. */
        public static final String DEPT_PREFIX = "DEPT_";

        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";
        public static final String CLAIM_DEPT = "dept";
    }

    /**
     * Sorting: whitelist dei campi ordinabili esposti via API.
     */
    @UtilityClass
    public static class SortField {
        // NB: evitare di esporre sorting su campi "tecnici" o non indicizzati.
        public static final java.util.Set<String> DOCTOR_ALLOWED = java.util.Set.of("id", "firstName", "lastName", "email");
        public static final java.util.Set<String> PATIENT_ALLOWED = java.util.Set.of("id", "firstName", "lastName", "email");
    }

    /**
     * RFC 7807: URI "type" standardizzati per le principali classi di errore.
     */
    @UtilityClass
    public static class Problem {
        public static final String TYPE_NOT_FOUND = "https://sanitech.it/problems/not-found";
        public static final String TYPE_VALIDATION_ERROR = "https://sanitech.it/problems/validation-error";
        public static final String TYPE_BAD_REQUEST = "https://sanitech.it/problems/bad-request";
        public static final String TYPE_FORBIDDEN = "https://sanitech.it/problems/forbidden";
        public static final String TYPE_TOO_MANY_REQUESTS = "https://sanitech.it/problems/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = "https://sanitech.it/problems/service-unavailable";
        public static final String TYPE_INTERNAL_ERROR = "https://sanitech.it/problems/internal-error";
    }

    /**
     * Messaggi (in italiano) usati nei Problem Details.
     */
    @UtilityClass
    public static class ErrorMessage {
        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_VALIDATION = "Errore di validazione";
        public static final String ERR_BAD_REQUEST = "Richiesta non valida";
        public static final String ERR_FORBIDDEN = "Accesso negato";
        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio non disponibile";
        public static final String ERR_INTERNAL = "Errore interno";

        public static final String MSG_VALIDATION_FAILED = "La richiesta contiene campi non validi.";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste consentite. Riprova più tardi.";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile. Riprova più tardi.";
        public static final String MSG_INTERNAL_ERROR = "Si è verificato un errore inatteso.";
    }

    /**
     * Outbox/Kafka: topic e metriche.
     */
    @UtilityClass
    public static class Outbox {
        public static final String TOPIC_DIRECTORY_EVENTS = "directory.events";

        public static final String OUTBOX_EVENTS_SAVED_COUNT = "outbox.events.saved.count";
        public static final String OUTBOX_EVENTS_PUBLISHED = "outbox.events.published";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";

        /** Tipi aggregato standard per eventi Outbox. */
        @lombok.experimental.UtilityClass
        public static class AggregateType {
            public static final String DOCTOR = "DOCTOR";
            public static final String PATIENT = "PATIENT";
        }

        /** Tipi evento standard per Outbox (Directory). */
        @lombok.experimental.UtilityClass
        public static class EventType {
            public static final String DOCTOR_CREATED = "DOCTOR_CREATED";
            public static final String DOCTOR_UPDATED = "DOCTOR_UPDATED";
            public static final String DOCTOR_DELETED = "DOCTOR_DELETED";

            public static final String PATIENT_CREATED = "PATIENT_CREATED";
            public static final String PATIENT_UPDATED = "PATIENT_UPDATED";
            public static final String PATIENT_DELETED = "PATIENT_DELETED";
        }

    }
}

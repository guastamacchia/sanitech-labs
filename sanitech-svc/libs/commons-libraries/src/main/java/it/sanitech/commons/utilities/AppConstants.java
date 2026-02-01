package it.sanitech.commons.utilities;

import it.sanitech.commons.boot.EnableSanitechPlatform;
import lombok.experimental.UtilityClass;

/**
 * Collezione di costanti applicative Sanitech.
 *
 * <p>
 * Le costanti sono raggruppate per ambito (configurazione, sicurezza, OpenAPI, Problem Details, Outbox, ecc.)
 * per ridurre le stringhe hard-coded nel codice e mantenere coerenza tra i layer.
 * </p>
 */
@UtilityClass
public class AppConstants {

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
        public static class OpenApi {
            public static final String PREFIX = "sanitech.openapi";
        }

        @UtilityClass
        public static class Security {
            public static final String PREFIX = "sanitech.security";
        }

        @UtilityClass
        public static class Outbox {
            public static final String PREFIX = "sanitech.outbox";
            public static final String PUBLISHER_PREFIX = PREFIX + ".publisher";
            /**
             * Nome dell'attributo dell'annotation che abilita/disabilita l'outbox.
             * È un contratto tra {@link EnableSanitechPlatform} e questo selector.
             *
             * <p>
             * Per chi è alle prime armi: il valore di questo attributo viene letto dal
             * {@code SanitechPlatformImportSelector} per decidere se importare (o meno)
             * i componenti outbox nel contesto Spring dell'applicazione.
             * </p>
             */
            public static final String ATTR_ENABLE_OUTBOX = "enableOutbox";

            /**
             * Fully Qualified Class Name della configurazione di scan Outbox.
             * Usata come marker per verificare la presenza della libreria outbox nel classpath.
             *
             * <p>
             * In pratica: se questa classe è presente, sappiamo che la libreria outbox
             * è disponibile e possiamo abilitarla senza rompere l'applicazione.
             * </p>
             */
            public static final String OUTBOX_SCAN_CONFIGURATION_FQCN =
                    "it.sanitech.outbox.boot.SanitechOutboxComponentScanConfiguration";
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
            public static final int DEFAULT_BATCH_SIZE = 100;
        }
    }

    /**
     * Sicurezza: endpoint pubblici (es. Swagger/Actuator) e claim/authority.
     */
    @UtilityClass
    public static class Security {
        /** Prefisso authority per i ruoli applicativi derivati da {@code realm_access.roles}. */
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String ROLE_ADMIN = ROLE_PREFIX + "ADMIN";
        public static final String ROLE_DOCTOR = ROLE_PREFIX + "DOCTOR";
        public static final String ROLE_PATIENT = ROLE_PREFIX + "PATIENT";

        /** Prefisso authority per gli scope OAuth2 derivati da {@code scope}. */
        public static final String SCOPE_PREFIX = "SCOPE_";

        /** Prefisso authority per ABAC di reparto derivato dal claim custom {@code dept}. */
        public static final String DEPT_PREFIX = "DEPT_";

        /** Claim standard Keycloak. */
        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";
        public static final String CLAIM_DEPT = "dept";

        /** Claim custom applicativi per identificativi utente. */
        public static final String CLAIM_PATIENT_ID = "pid";
        public static final String CLAIM_DOCTOR_ID = "did";
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
        public static final String TYPE_CONFLICT = "https://sanitech.it/problems/conflict";
    }

    /**
     * Messaggi usati nei Problem Details.
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
        public static final String ERR_CONFLICT = "Conflitto sui dati";

        // fallback standard
        public static final String FALLBACK_VALUE = "N/D";

        public static final String MSG_NOT_FOUND = "%s non trovato.";
        public static final String MSG_NOT_FOUND_BY_ID = "%s con id %s non trovato.";

        public static final String MSG_VALIDATION_FAILED = "La richiesta contiene campi non validi.";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste consentite. Riprova più tardi.";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile. Riprova più tardi.";
        public static final String MSG_INTERNAL_ERROR = "Si è verificato un errore inatteso.";

        public static final String MSG_CONFLICT = "Operazione non eseguibile: violazione di vincoli sui dati.";
    }

    /**
     * RFC 7807 - "extra": chiavi e default utilizzati nella sezione extra dei ProblemDetails.
     *
     * <p>
     * Serve a evitare stringhe hard-coded come "campo", "messaggio" o "Valore non valido"
     * nei mapper degli errori di validazione.
     * </p>
     */
    @UtilityClass
    public static class ProblemExtra {
        public static final String FIELD = "campo";
        public static final String MESSAGE = "messaggio";
        public static final String DEFAULT_FIELD_ERROR = "Valore non valido";
    }

    /**
     * Outbox/Kafka: topic e metriche.
     */
    @UtilityClass
    public static class Outbox {
        public static final String OUTBOX_EVENTS_SAVED_COUNT = "outbox.events.saved.count";
        public static final String OUTBOX_EVENTS_PUBLISHED = "outbox.events.published";

        /**
         * Tag metriche: usati per differenziare aggregateType/eventType.
         * Un junior può pensare ai tag come "etichette" che permettono di filtrare
         * le metriche (es. per vedere solo gli eventi di un certo aggregate).
         */
        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";
    }
}

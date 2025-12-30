package it.sanitech.scheduling.utilities;

import java.util.Set;

/**
 * Collezione centralizzata di costanti applicative.
 *
 * <p>
 * Obiettivi principali:
 * <ul>
 *   <li>evitare stringhe "cablata" sparse nel codice;</li>
 *   <li>rendere più semplice la manutenzione (claim JWT, metriche, path, ecc.);</li>
 *   <li>favorire coerenza tra moduli (web, security, outbox, config).</li>
 * </ul>
 * </p>
 */
public final class AppConstants {

    private AppConstants() { }

    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Chiavi e default di runtime (property Spring “standard”).
     */
    public static final class Runtime {
        private Runtime() { }

        /** Property Spring per il nome applicazione. */
        public static final String SPRING_APP_NAME_KEY = "spring.application.name";
        /** Default nome applicazione se non configurato. */
        public static final String DEFAULT_APP_NAME = "svc-scheduling";

        /** Property Spring per la porta HTTP. */
        public static final String SERVER_PORT_KEY = "server.port";
        /** Default porta se non configurata. */
        public static final String DEFAULT_SERVER_PORT = "8083";
    }

    /**
     * Costanti per OpenAPI (springdoc).
     */
    public static final class OpenApi {
        private OpenApi() { }

        public static final String GROUP = "scheduling";
        public static final String TITLE = "Sanitech — Scheduling API";
        public static final String VERSION = "v1";
        public static final String PACKAGES_TO_SCAN = "it.sanitech.scheduling.web";
    }

    /**
     * Chiavi di configurazione (custom namespace).
     */
    public static final class ConfigKeys {
        private ConfigKeys() { }

        public static final class Cors {
            private Cors() { }
            public static final String PREFIX = "sanitech.cors";
        }

        public static final class Outbox {
            private Outbox() { }
            public static final String PUBLISHER_DELAY_MS = "sanitech.outbox.publisher.delay-ms";
        }
    }

    /**
     * Default value per la configurazione (usati come fallback).
     */
    public static final class ConfigDefaultValue {
        private ConfigDefaultValue() { }

        public static final class Cors {
            private Cors() { }

            /** Default allow-credentials. */
            public static final boolean ALLOW_CREDENTIALS = false;

            /** Default max-age preflight (seconds). */
            public static final long MAX_AGE_SECONDS = 3600L;
        }

        public static final class Outbox {
            private Outbox() { }
            public static final String PUBLISHER_DELAY_MS = "1000";
        }
    }

    /**
     * Costanti legate al mapping JWT/authorities.
     */
    public static final class Security {
        private Security() { }

        // Claim standard Keycloak
        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";

        // Claim custom per ABAC reparto
        public static final String CLAIM_DEPT = "dept";

        // Claim opzionali (utile per operazioni “self” senza passare ID in chiaro)
        public static final String CLAIM_PATIENT_ID = "pid";
        public static final String CLAIM_DOCTOR_ID = "did";

        public static final String PREFIX_ROLE = "ROLE_";
        public static final String PREFIX_SCOPE = "SCOPE_";
        public static final String PREFIX_DEPT = "DEPT_";

        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_PATIENT = "ROLE_PATIENT";
        public static final String ROLE_DOCTOR = "ROLE_DOCTOR";
    }

    /**
     * Valori per Problem Details (RFC 7807).
     */
    public static final class Problem {
        private Problem() { }

        public static final String TYPE_NOT_FOUND = "https://sanitech.example/problems/not-found";
        public static final String TYPE_VALIDATION_ERROR = "https://sanitech.example/problems/validation-error";
        public static final String TYPE_BAD_REQUEST = "https://sanitech.example/problems/bad-request";
        public static final String TYPE_TOO_MANY_REQUESTS = "https://sanitech.example/problems/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = "https://sanitech.example/problems/service-unavailable";
        public static final String TYPE_INTERNAL_ERROR = "https://sanitech.example/problems/internal-error";
        public static final String TYPE_ACCESS_DENIED = "https://sanitech.example/problems/access-denied";
        public static final String TYPE_CONFLICT = "https://sanitech.example/problems/conflict";
    }

    /**
     * Messaggi di errore in italiano (titoli o dettagli standardizzati).
     */
    public static final class ErrorMessage {
        private ErrorMessage() { }

        // Titoli/label standard
        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_VALIDATION = "Richiesta non valida";
        public static final String ERR_BAD_REQUEST = "Parametri non validi";
        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile";
        public static final String ERR_INTERNAL = "Errore interno";
        public static final String ERR_ACCESS_DENIED = "Accesso negato";
        public static final String ERR_CONFLICT = "Conflitto sui dati";

        // Messaggi standard
        public static final String MSG_VALIDATION_FAILED = "La richiesta contiene campi non validi.";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste. Riprova più tardi.";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio non disponibile. Riprova più tardi.";
        public static final String MSG_INTERNAL_ERROR = "Si è verificato un errore imprevisto.";
        public static final String MSG_ACCESS_DENIED = "Accesso negato per il reparto richiesto.";
        public static final String MSG_CONFLICT = "Operazione non eseguibile: violazione di vincoli sui dati.";

        /** Messaggio fallback quando {@code defaultMessage} è assente in validazione. */
        public static final String MSG_VALIDATION_DEFAULT = "Valore non valido";

        // --- Scheduling domain messages (ritornati come 'detail' RFC 7807) ---
        public static final String MSG_INVALID_TIME_RANGE = "Intervallo orario non valido: startAt deve essere < endAt.";
        public static final String MSG_EMPTY_SLOT_LIST = "Lista slot vuota.";
        public static final String MSG_SLOT_ALREADY_BOOKED = "Lo slot è già prenotato: annullare prima l'appuntamento.";
        public static final String MSG_SLOT_NOT_AVAILABLE = "Slot non disponibile.";
        public static final String MSG_ROLE_NOT_ALLOWED_APPOINTMENT_SEARCH = "Ruolo non autorizzato per la consultazione appuntamenti.";
        public static final String MSG_APPOINTMENT_CANCEL_NOT_AUTHORIZED = "Non sei autorizzato a cancellare questo appuntamento.";
        public static final String MSG_PATIENT_ID_REQUIRED_FOR_ADMIN = "patientId è obbligatorio per operazioni ADMIN.";
        public static final String MSG_PATIENT_ID_MISMATCH = "patientId non coerente con l'utente autenticato.";
        public static final String MSG_BOOKING_ROLE_NOT_ALLOWED = "Solo ADMIN o PATIENT possono prenotare un appuntamento.";

        // Claim parsing
        public static final String MSG_JWT_CLAIM_MISSING_OR_INVALID_PREFIX = "Claim JWT mancante o non valido: ";
    }

    /**
     * Outbox/Kafka: topic e metriche.
     */
    public static final class Outbox {
        private Outbox() { }

        /** Topic Kafka su cui pubblicare gli eventi del dominio Scheduling. */
        public static final String TOPIC_SCHEDULING_EVENTS = "scheduling.events";

        /** Metrica: numero eventi salvati in outbox. */
        public static final String OUTBOX_EVENTS_SAVED = "outbox.events.saved.count";

        /** Alias esplicito (per leggibilità nel codice). */
        public static final String OUTBOX_EVENTS_SAVED_COUNT = OUTBOX_EVENTS_SAVED;

        /** Metrica: numero eventi pubblicati su Kafka. */
        public static final String OUTBOX_EVENTS_PUBLISHED = "outbox.events.published";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";
    }

    /**
     * Whitelist dei campi ordinabili esposti in API (evita sorting arbitrario).
     */
    public static final class Sorting {
        private Sorting() { }

        // Slot
        public static final Set<String> ALLOWED_APPOINTMENT_SORT_FIELDS =
            Set.of("startAt", "endAt", "doctorId", "departmentCode");

        // Appointment
        public static final Set<String> ALLOWED_SLOT_SORT_FIELDS =
            Set.of("startAt", "endAt", "doctorId", "departmentCode", "mode");
    }
}

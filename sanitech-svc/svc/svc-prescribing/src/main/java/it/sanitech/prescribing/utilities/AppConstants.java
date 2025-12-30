package it.sanitech.prescribing.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Costanti applicative centralizzate del microservizio {@code svc-prescribing}.
 *
 * <p>
 * Convenzione: tutte le stringhe "di dominio" (path, nomi metriche, topic Kafka, chiavi di config,
 * messaggi standard) vivono qui per evitare duplicazioni e valori cablati sparsi nel codice.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    /** Path REST comuni. */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ApiPath {
        public static final String API_BASE = "/api";
        public static final String PRESCRIPTIONS = API_BASE + "/prescriptions";
        public static final String DOCTOR_PRESCRIPTIONS = API_BASE + "/doctor/prescriptions";
        public static final String ADMIN_PRESCRIPTIONS = API_BASE + "/admin/prescriptions";
    }

    /** Costanti OpenAPI / Swagger. */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class OpenApi {
        public static final String GROUP = "prescribing";
        public static final String TITLE = "Sanitech — Prescribing API";
        public static final String VERSION = "v1";
        public static final String PACKAGES_TO_SCAN = "it.sanitech.prescribing.web";
    }

    /** Endpoint che possono essere esposti senza autenticazione. */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Security {
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String SCOPE_PREFIX = "SCOPE_";
        public static final String DEPT_PREFIX = "DEPT_";

        public static final String[] PUBLIC_ENDPOINTS = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/health/**"
        };
    }

    /** Costanti relative a RFC 7807 (Problem Details). */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Problem {
        public static final String TYPE_BASE = "https://sanitech.example/problems";
        public static final String TYPE_NOT_FOUND = TYPE_BASE + "/not-found";
        public static final String TYPE_VALIDATION = TYPE_BASE + "/validation-error";
        public static final String TYPE_BAD_REQUEST = TYPE_BASE + "/bad-request";
        public static final String TYPE_FORBIDDEN = TYPE_BASE + "/forbidden";
        public static final String TYPE_TOO_MANY_REQUESTS = TYPE_BASE + "/too-many-requests";
        public static final String TYPE_SERVICE_UNAVAILABLE = TYPE_BASE + "/service-unavailable";
        public static final String TYPE_INTERNAL_ERROR = TYPE_BASE + "/internal-error";
    }

    /** Messaggi di errore standard (in italiano). */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorMessage {
        public static final String ERR_NOT_FOUND = "Risorsa non trovata";
        public static final String ERR_VALIDATION = "Errore di validazione";
        public static final String ERR_BAD_REQUEST = "Richiesta non valida";
        public static final String ERR_FORBIDDEN = "Operazione non autorizzata";
        public static final String ERR_TOO_MANY_REQUESTS = "Troppe richieste";
        public static final String ERR_SERVICE_UNAVAILABLE = "Servizio non disponibile";
        public static final String ERR_INTERNAL = "Errore interno";

        public static final String MSG_VALIDATION_FAILED = "Uno o più campi non sono validi.";
        public static final String MSG_TOO_MANY_REQUESTS = "Hai superato il limite di richieste consentite. Riprova più tardi.";
        public static final String MSG_SERVICE_UNAVAILABLE = "Servizio temporaneamente non disponibile. Riprova più tardi.";
        public static final String MSG_INTERNAL_ERROR = "Si è verificato un errore inatteso.";
        public static final String MSG_CONSENT_REQUIRED = "Consenso non presente per operare sui dati del paziente.";
        public static final String MSG_DEPARTMENT_FORBIDDEN = "Non hai i permessi per operare sul reparto richiesto.";
    }

    /** Outbox + Kafka. */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Outbox {
        public static final String TOPIC_PRESCRIBING_EVENTS = "prescribing.events";

        public static final String AGGREGATE_PRESCRIPTION = "PRESCRIPTION";
        public static final String EVT_PRESCRIPTION_CREATED = "PRESCRIPTION_CREATED";
        public static final String EVT_PRESCRIPTION_UPDATED = "PRESCRIPTION_UPDATED";
        public static final String EVT_PRESCRIPTION_CANCELLED = "PRESCRIPTION_CANCELLED";

        public static final String OUTBOX_EVENTS_SAVED = "outbox.events.saved.count";
        public static final String OUTBOX_EVENTS_PUBLISHED = "outbox.events.published.count";

        public static final String TAG_AGGREGATE_TYPE = "aggregateType";
        public static final String TAG_EVENT_TYPE = "eventType";
    }

    /** Claim attesi nel JWT (iniettati da Keycloak / gateway). */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class JwtClaim {
        public static final String REALM_ACCESS = "realm_access";
        public static final String ROLES = "roles";
        public static final String SCOPE = "scope";
        public static final String DEPT = "dept";

        /** Identificativo applicativo del paziente. */
        public static final String PATIENT_ID = "pid";
        /** Identificativo applicativo del medico. */
        public static final String DOCTOR_ID = "did";
    }

    
    /** Chiavi JSON ricorrenti nei payload di errore. */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class JsonKeys {
        public static final String ERRORS = "errors";
        public static final String FIELD = "field";
        public static final String MESSAGE = "message";
    }

    /** Chiavi di configurazione (application.yml). */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ConfigKeys {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Cors {
            public static final String PREFIX = "sanitech.cors";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class OutboxPublisher {
            public static final String DELAY_MS = "sanitech.outbox.publisher.delay-ms";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Integrations {
            public static final String CONSENTS_BASE_URL = "sanitech.integrations.consents.base-url";
        }
    }

    /** Valori di default usati quando le property non sono presenti. */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class DefaultValues {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class OutboxPublisher {
            public static final String DELAY_MS = "1000";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class App {
            public static final String DEFAULT_APP_NAME = "svc-prescribing";
            public static final String DEFAULT_PORT = "8091";
        }
    }
}

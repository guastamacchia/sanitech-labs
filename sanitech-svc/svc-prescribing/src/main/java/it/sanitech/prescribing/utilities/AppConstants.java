package it.sanitech.prescribing.utilities;

import lombok.experimental.UtilityClass;

/**
 * Costanti applicative specifiche del microservizio {@code svc-prescribing}.
 */
@UtilityClass
public class AppConstants {

    /**
     * Path REST comuni.
     */
    @UtilityClass
    public static class ApiPath {
        public static final String API_BASE = "/api";
        public static final String PRESCRIPTIONS = API_BASE + "/prescriptions";
        public static final String DOCTOR_PRESCRIPTIONS = API_BASE + "/doctor/prescriptions";
        public static final String ADMIN_PRESCRIPTIONS = API_BASE + "/admin/prescriptions";
    }

    /**
     * Outbox: tipi evento specifici del dominio Prescribing.
     */
    @UtilityClass
    public static class Outbox {
        public static final String AGGREGATE_PRESCRIPTION = "PRESCRIPTION";
        public static final String EVT_PRESCRIPTION_CREATED = "PRESCRIPTION_CREATED";
        public static final String EVT_PRESCRIPTION_UPDATED = "PRESCRIPTION_UPDATED";
        public static final String EVT_PRESCRIPTION_CANCELLED = "PRESCRIPTION_CANCELLED";
        /** Topic per eventi di auditing */
        public static final String TOPIC_AUDITS_EVENTS = "audits.events";
    }

    /**
     * Claim custom attesi nel JWT (iniettati da Keycloak / gateway).
     */
    @UtilityClass
    public static class JwtClaim {
        /** Identificativo applicativo del paziente. */
        public static final String PATIENT_ID = "pid";
        /** Identificativo applicativo del medico. */
        public static final String DOCTOR_ID = "did";
    }

    /**
     * Messaggi di errore specifici del dominio Prescribing.
     */
    @UtilityClass
    public static class ErrorMessage {
        public static final String MSG_CONSENT_REQUIRED = "Consenso non presente per operare sui dati del paziente.";
    }

    /**
     * Valori di default usati quando le property non sono presenti.
     */
    @UtilityClass
    public static class DefaultValues {

        @UtilityClass
        public static class App {
            public static final String DEFAULT_APP_NAME = "svc-prescribing";
            public static final String DEFAULT_PORT = "8091";
        }
    }
}

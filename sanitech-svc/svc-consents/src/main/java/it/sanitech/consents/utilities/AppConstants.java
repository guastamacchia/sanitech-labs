package it.sanitech.consents.utilities;

/**
 * Costanti applicative del microservizio {@code svc-consents}.
 * <p>
 * Convenzione: tutte le stringhe "stabili" (path, claim, nomi metriche, ecc.)
 * sono centralizzate qui per evitare duplicazioni e refusi.
 * </p>
 */
public final class AppConstants {

    private AppConstants() {
        // utility class
    }

    /**
     * Chiavi Spring/Spring Boot usate frequentemente.
     */
    public static final class Spring {
        private Spring() {}

        /** Property key: {@code spring.application.name}. */
        public static final String APP_NAME_KEY = "spring.application.name";
        /** Default: {@code svc-consents}. */
        public static final String APP_NAME_DEFAULT = "svc-consents";

        /** Property key: {@code server.port}. */
        public static final String SERVER_PORT_KEY = "server.port";
        /** Default: {@code 8085}. */
        public static final String SERVER_PORT_DEFAULT = "8085";
    }

    /**
     * Path API esposti dal servizio.
     */
    public static final class ApiPath {
        private ApiPath() {}

        public static final String API_BASE = "/api";
        public static final String CONSENTS = API_BASE + "/consents";
        public static final String CONSENTS_ME = CONSENTS + "/me";
        public static final String CONSENTS_CHECK = CONSENTS + "/check";

        public static final String ADMIN_BASE = API_BASE + "/admin";
        public static final String ADMIN_CONSENTS = ADMIN_BASE + "/consents";
    }

    /**
     * Claim JWT custom del dominio Consents.
     */
    public static final class Security {
        private Security() {}

        /** Claim custom: patient id (se presente nel token). */
        public static final String CLAIM_PATIENT_ID = "pid";
        /** Claim custom: doctor id (se presente nel token). */
        public static final String CLAIM_DOCTOR_ID = "did";
    }

    /**
     * Costanti per eventi Outbox.
     */
    public static final class Outbox {
        private Outbox() {}

        /** Topic per eventi di auditing */
        public static final String TOPIC_AUDITS_EVENTS = "audits.events";
    }
}

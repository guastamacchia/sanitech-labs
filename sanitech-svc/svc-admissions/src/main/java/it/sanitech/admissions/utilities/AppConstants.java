package it.sanitech.admissions.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Raccolta di costanti applicative del microservizio {@code svc-admissions}.
 *
 * <p>
 * Le costanti sono centralizzate qui per:
 * <ul>
 *   <li>evitare stringhe cablate nel codice;</li>
 *   <li>mantenere uniformità tra moduli (security, error handling, OpenAPI, outbox);</li>
 *   <li>facilitare refactoring e manutenzione.</li>
 * </ul>
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    /**
     * Costanti RFC 7807 (Problem Details) specifiche del microservizio.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Problem {
        public static final String TYPE_NO_BEDS = "https://sanitech.it/problems/no-beds-available";
    }

    /**
     * Messaggi di errore specifici del microservizio.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorMessage {
        public static final String ERR_NO_BEDS = "Posti letto non disponibili";
    }


    /**
     * Whitelist di campi ordinabili via API (protezione per sorting/paging).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SortFields {
        public static final java.util.Set<String> ADMISSIONS = java.util.Set.of(
                "id",
                "patientId",
                "departmentCode",
                "admissionType",
                "status",
                "admittedAt",
                "dischargedAt"
        );
    }

    /**
     * Keys di proprietà Spring usate frequentemente.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Spring {
        public static final String KEY_APP_NAME = "spring.application.name";
        public static final String DEFAULT_APP_NAME = "svc-admissions";

        public static final String KEY_SERVER_PORT = "server.port";
        public static final String DEFAULT_SERVER_PORT = "8084";
    }

    /**
     * Costanti per eventi Outbox.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Outbox {
        /** Topic per eventi di auditing */
        public static final String TOPIC_AUDITS_EVENTS = "audits.events";
        /** Topic per eventi di fatturazione (svc-payments) */
        public static final String TOPIC_PAYMENTS_EVENTS = "payments.events";
        /** Topic per eventi di notifica (svc-notifications) */
        public static final String TOPIC_NOTIFICATIONS_EVENTS = "notifications.events";
    }

}

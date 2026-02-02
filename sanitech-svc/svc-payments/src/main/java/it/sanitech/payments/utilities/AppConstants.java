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
    public static class Api {
        public static final String API_BASE = "/api";
        public static final String ADMIN_BASE = "/api/admin";
        public static final String WEBHOOK_BASE = "/api/webhooks";

        public static final String PAYMENTS = "/payments";
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

    @UtilityClass
    public static class Claims {
        /** Claim per identificare il paziente (Long o String numerico). */
        public static final String PATIENT_ID = "pid";
    }

    @UtilityClass
    public static class Security {
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_PATIENT = "ROLE_PATIENT";
    }

    @UtilityClass
    public static class Payments {
        public static final String PROVIDER_MANUAL = "MANUAL";
    }

    @UtilityClass
    public static class Outbox {
        public static final String AGGREGATE_TYPE_PAYMENT = "PAYMENT_ORDER";
        public static final String EVT_CREATED = "PAYMENT_CREATED";
        public static final String EVT_STATUS_CHANGED = "PAYMENT_STATUS_CHANGED";
        public static final String EVT_REMINDER_REQUESTED = "PAYMENT_REMINDER_REQUESTED";

        /** Topic per notifiche (solleciti, conferme, etc.) */
        public static final String TOPIC_NOTIFICATIONS_EVENTS = "notifications.events";
        /** Topic per eventi di auditing */
        public static final String TOPIC_AUDITS_EVENTS = "audits.events";
    }

    @UtilityClass
    public static class Sort {
        public static final String DEFAULT_FIELD = "createdAt";
    }
}

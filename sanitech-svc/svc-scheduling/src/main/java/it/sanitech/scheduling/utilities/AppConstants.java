package it.sanitech.scheduling.utilities;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Collezione di costanti applicative specifiche del microservizio Scheduling.
 */
@UtilityClass
public class AppConstants {

    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Costanti runtime legate alla configurazione Spring Boot.
     */
    @UtilityClass
    public static class Spring {
        public static final String APP_NAME_KEY = "spring.application.name";
        public static final String DEFAULT_APP_NAME = "svc-scheduling";

        public static final String SERVER_PORT_KEY = "server.port";
        public static final String DEFAULT_SERVER_PORT = "8083";
    }

    /**
     * Claim JWT specifici del dominio Scheduling.
     */
    @UtilityClass
    public static class JwtClaims {
        public static final String PATIENT_ID = "pid";
        public static final String DOCTOR_ID = "did";
    }

    /**
     * Messaggi di errore specifici del dominio Scheduling.
     */
    @UtilityClass
    public static class ErrorMessage {
        public static final String MSG_INVALID_TIME_RANGE = "Intervallo orario non valido: startAt deve essere < endAt.";
        public static final String MSG_EMPTY_SLOT_LIST = "Lista slot vuota.";
        public static final String MSG_SLOT_ALREADY_BOOKED = "Lo slot è già prenotato: annullare prima l'appuntamento.";
        public static final String MSG_SLOT_NOT_AVAILABLE = "Slot non disponibile.";
        public static final String MSG_ROLE_NOT_ALLOWED_APPOINTMENT_SEARCH = "Ruolo non autorizzato per la consultazione appuntamenti.";
        public static final String MSG_APPOINTMENT_CANCEL_NOT_AUTHORIZED = "Non sei autorizzato a cancellare questo appuntamento.";
        public static final String MSG_PATIENT_ID_REQUIRED_FOR_ADMIN = "patientId è obbligatorio per operazioni ADMIN.";
        public static final String MSG_PATIENT_ID_MISMATCH = "patientId non coerente con l'utente autenticato.";
        public static final String MSG_BOOKING_ROLE_NOT_ALLOWED = "Solo ADMIN o PATIENT possono prenotare un appuntamento.";
        public static final String MSG_JWT_CLAIM_MISSING_OR_INVALID_PREFIX = "Claim JWT mancante o non valido: ";
        public static final String MSG_APPOINTMENT_NOT_BOOKED = "L'appuntamento deve essere in stato BOOKED per questa operazione.";
        public static final String MSG_SLOT_DOCTOR_MISMATCH = "Lo slot selezionato non appartiene al medico dell'appuntamento.";
        public static final String MSG_SLOT_MODE_MISMATCH = "Lo slot selezionato non ha la stessa modalità dell'appuntamento.";
        public static final String MSG_NEW_DOCTOR_SLOT_MISMATCH = "Lo slot selezionato non appartiene al medico indicato.";
        public static final String MSG_SAME_SLOT = "Il nuovo slot è identico a quello attuale.";
    }

    /**
     * Whitelist dei campi ordinabili esposti in API (evita sorting arbitrario).
     */
    @UtilityClass
    public static class Sorting {
        public static final Set<String> ALLOWED_APPOINTMENT_SORT_FIELDS =
                Set.of("startAt", "endAt", "doctorId", "departmentCode");

        public static final Set<String> ALLOWED_SLOT_SORT_FIELDS =
                Set.of("startAt", "endAt", "doctorId", "departmentCode", "mode");
    }

    /**
     * Costanti per eventi Outbox.
     */
    @UtilityClass
    public static class Outbox {
        /** Topic per eventi di auditing */
        public static final String TOPIC_AUDITS_EVENTS = "audits.events";
        /** Topic per eventi di fatturazione (svc-payments) */
        public static final String TOPIC_PAYMENTS_EVENTS = "payments.events";
        /** Topic per eventi di notifica (svc-notifications) */
        public static final String TOPIC_NOTIFICATIONS_EVENTS = "notifications.events";
    }
}

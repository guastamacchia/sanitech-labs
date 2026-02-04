package it.sanitech.directory.utilities;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Collezione di costanti applicative specifiche del microservizio Directory.
 *
 * <p>
 * Contiene path API, campi ordinabili e metadata per eventi Outbox legati al dominio Directory.
 * Le costanti sono mantenute localmente al servizio per evitare dipendenze improprie su "commons".
 * </p>
 */
@UtilityClass
public class AppConstants {

    /**
     * Costanti runtime legate alla configurazione Spring Boot.
     */
    @UtilityClass
    public static class Spring {
        public static final String APP_NAME_KEY = "spring.application.name";
        public static final String DEFAULT_APP_NAME = "svc-directory";

        public static final String SERVER_PORT_KEY = "server.port";
        public static final String DEFAULT_SERVER_PORT = "8082";
    }

    /**
     * Path REST e prefissi comuni del microservizio.
     */
    @UtilityClass
    public static class ApiPath {
        public static final String API = "/api";
        public static final String ADMIN = API + "/admin";
        public static final String PUBLIC = API + "/public";

        public static final String DOCTORS = API + "/doctors";
        public static final String PATIENTS = API + "/patients";
        public static final String DEPARTMENTS = API + "/departments";
        public static final String FACILITIES = API + "/facilities";

        public static final String ADMIN_DOCTORS = ADMIN + "/doctors";
        public static final String ADMIN_PATIENTS = ADMIN + "/patients";
        public static final String ADMIN_DEPARTMENTS = ADMIN + "/departments";
        public static final String ADMIN_FACILITIES = ADMIN + "/facilities";

        public static final String PUBLIC_PATIENTS = PUBLIC + "/patients";

        public static final String PATIENT_ME = API + "/patient/me";
        public static final String DOCTOR_ME = API + "/doctor/me";
    }

    /**
     * Sorting: whitelist dei campi ordinabili esposti via API.
     */
    @UtilityClass
    public static class SortField {
        public static final Set<String> DOCTOR_ALLOWED = Set.of("id", "firstName", "lastName", "email");
        public static final Set<String> PATIENT_ALLOWED = Set.of("id", "firstName", "lastName", "email");
    }

    /**
     * Outbox: topic e tipi evento specifici del dominio Directory.
     */
    @UtilityClass
    public static class Outbox {
        public static final String TOPIC_DIRECTORY_EVENTS = "audits.events";
        public static final String TOPIC_NOTIFICATIONS_EVENTS = "notifications.events";
        public static final String TOPIC_AUDITS_EVENTS = "audits.events";

        /** Tipi evento standard per Outbox (Directory). */
        @UtilityClass
        public static class EventType {
            public static final String DOCTOR_CREATED = "DOCTOR_CREATED";
            public static final String DOCTOR_UPDATED = "DOCTOR_UPDATED";
            public static final String DOCTOR_DELETED = "DOCTOR_DELETED";

            public static final String PATIENT_CREATED = "PATIENT_CREATED";
            public static final String PATIENT_UPDATED = "PATIENT_UPDATED";
            public static final String PATIENT_DELETED = "PATIENT_DELETED";

            public static final String ACTIVATION_EMAIL_REQUESTED = "ACTIVATION_EMAIL_REQUESTED";
            public static final String ACCOUNT_ENABLED_EMAIL_REQUESTED = "ACCOUNT_ENABLED_EMAIL_REQUESTED";
            public static final String ACCOUNT_DISABLED_EMAIL_REQUESTED = "ACCOUNT_DISABLED_EMAIL_REQUESTED";
        }

        /** Tipi aggregato standard per eventi Outbox. */
        @UtilityClass
        public static class AggregateType {
            public static final String DOCTOR = "DOCTOR";
            public static final String PATIENT = "PATIENT";
        }
    }
}

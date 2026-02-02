package it.sanitech.televisit.utilities;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Collezione di costanti specifiche del microservizio Televisit.
 */
@UtilityClass
public class AppConstants {

    /**
     * Path REST e prefissi comuni del microservizio.
     */
    @UtilityClass
    public static class ApiPath {
        public static final String API = "/api";
        public static final String TELEVISITS = API + "/televisits";
    }

    /**
     * Sorting: whitelist dei campi ordinabili esposti via API.
     */
    @UtilityClass
    public static class SortField {
        public static final String DEFAULT_FIELD = "id";
        public static final Set<String> TELEVISIT_SESSION_ALLOWED = Set.of(
                "id", "roomName", "department", "scheduledAt", "status", "createdAt"
        );
    }

    /**
     * Outbox: metadati eventi specifici del dominio Televisit.
     */
    @UtilityClass
    public static class Outbox {
        public static final String AGGREGATE_TELEVISIT_SESSION = "TELEVISIT_SESSION";
        /** Topic per eventi di auditing */
        public static final String TOPIC_AUDITS_EVENTS = "audits.events";

        @UtilityClass
        public static class EventType {
            public static final String CREATED = "CREATED";
            public static final String STARTED = "STARTED";
            public static final String ENDED = "ENDED";
            public static final String CANCELED = "CANCELED";
        }
    }
}

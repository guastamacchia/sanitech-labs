package it.sanitech.audit.utilities;

import lombok.experimental.UtilityClass;

/**
 * Costanti applicative del microservizio {@code svc-audit}.
 */
@UtilityClass
public class AppConstants {

    @UtilityClass
    public static class Spring {
        public static final String APP_NAME_KEY = "spring.application.name";
        public static final String APP_NAME_DEFAULT = "svc-audit";

        public static final String SERVER_PORT_KEY = "server.port";
        public static final String SERVER_PORT_DEFAULT = "8088";
    }

    @UtilityClass
    public static class ApiPath {
        public static final String API_BASE = "/api";
        public static final String AUDIT_EVENTS = "/audit/events";
    }

    @UtilityClass
    public static class Audit {

        /** Metric: eventi audit salvati. */
        public static final String METRIC_AUDIT_EVENTS_SAVED = "audit.events.saved.count";

        public static final String SOURCE_API = "api";
        public static final String SOURCE_KAFKA = "kafka";

        public static final String OUTCOME_SUCCESS = "SUCCESS";
        public static final String OUTCOME_DENIED = "DENIED";
        public static final String OUTCOME_FAILURE = "FAILURE";
    }
}

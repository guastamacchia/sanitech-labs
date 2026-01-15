package it.sanitech.notifications.utilities;

/**
 * Costanti applicative specifiche del microservizio Notifications.
 */
public final class AppConstants {

    private AppConstants() {
        // utility class
    }

    /**
     * Costanti del dominio Notifications.
     */
    public static final class Notifications {
        // Whitelist dei campi ordinabili esposti via API (protezione da sort injection)
        public static final String SORT_ID = "id";
        public static final String SORT_CREATED_AT = "createdAt";
        public static final String SORT_STATUS = "status";

        private Notifications() { }
    }
}

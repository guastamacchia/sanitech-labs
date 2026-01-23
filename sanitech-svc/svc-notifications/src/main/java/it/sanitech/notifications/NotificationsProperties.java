package it.sanitech.notifications;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà applicative del microservizio Notifications.
 *
 * <p>Caricate dal prefisso {@code sanitech.notifications}.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "sanitech.notifications")
public class NotificationsProperties {

    private Mail mail = new Mail();
    private Dispatcher dispatcher = new Dispatcher();

    @Getter
    @Setter
    public static class Mail {
        /**
         * Mittente email (header From) usato per le notifiche EMAIL.
         */
        private String from = "no-reply@sanitech.example";
    }

    @Getter
    @Setter
    public static class Dispatcher {
        /**
         * Delay (ms) tra un ciclo e l'altro del dispatcher di notifiche.
         */
        private long delayMs = 2000;

        /**
         * Numero massimo di notifiche EMAIL PENDING processate per ciclo.
         */
        private int batchSize = 50;
    }
}

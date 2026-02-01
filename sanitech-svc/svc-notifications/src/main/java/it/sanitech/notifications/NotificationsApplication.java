package it.sanitech.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio Sanitech Notifications.
 *
 * <p>Abilita lo scheduling per:
 * <ul>
 *   <li>dispatch delle notifiche PENDING (invio email);</li>
 *   <li>publish della Outbox verso Kafka.</li>
 * </ul>
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"it.sanitech.notifications", "it.sanitech.commons", "it.sanitech.outbox"})
@ConfigurationPropertiesScan
@EnableScheduling
public class NotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationsApplication.class, args);
    }
}

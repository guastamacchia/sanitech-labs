package it.sanitech.notifications;

import it.sanitech.commons.boot.EnableSanitechPlatform;
import it.sanitech.outbox.persistence.OutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
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
 *
 * <p>
 * {@link EntityScan} include sia le entity locali del microservizio che quelle
 * del modulo outbox, necessarie per il pattern Transactional Outbox.
 * </p>
 */
@SpringBootApplication
@EnableSanitechPlatform
@ConfigurationPropertiesScan
@EnableScheduling
@EntityScan(basePackages = {
        "it.sanitech.notifications.repositories.entities",
        OutboxEvent.ENTITY_PACKAGE
})
public class NotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationsApplication.class, args);
    }
}

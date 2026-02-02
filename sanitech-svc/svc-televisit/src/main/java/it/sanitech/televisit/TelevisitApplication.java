package it.sanitech.televisit;

import it.sanitech.commons.boot.EnableSanitechPlatform;
import it.sanitech.outbox.persistence.OutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio <strong>svc-televisit</strong> (Sanitech).
 *
 * <p>Abilita {@link EnableScheduling} per il publisher Outbox (flush periodico verso Kafka).</p>
 *
 * <p>
 * {@link EntityScan} include sia le entity locali del microservizio che quelle
 * del modulo outbox, necessarie per il pattern Transactional Outbox.
 * </p>
 */
@SpringBootApplication
@EnableSanitechPlatform
@EnableScheduling
@EntityScan(basePackages = {
        "it.sanitech.televisit.repositories.entities",
        OutboxEvent.ENTITY_PACKAGE
})
public class TelevisitApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelevisitApplication.class, args);
    }
}

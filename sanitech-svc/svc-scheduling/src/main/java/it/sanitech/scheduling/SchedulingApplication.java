package it.sanitech.scheduling;

import it.sanitech.commons.boot.EnableSanitechPlatform;
import it.sanitech.outbox.persistence.OutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio {@code svc-scheduling} di Sanitech.
 * <p>
 * Abilita lo scheduling per il publisher Outbox verso Kafka.
 * </p>
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
        "it.sanitech.scheduling.repositories.entities",
        OutboxEvent.ENTITY_PACKAGE
})
public class SchedulingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingApplication.class, args);
    }
}

package it.sanitech.payments;

import it.sanitech.commons.boot.EnableSanitechPlatform;
import it.sanitech.outbox.persistence.OutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point del microservizio {@code svc-payments}.
 *
 * <p>
 * Abilita lo scheduling per il publisher dell'Outbox.
 * Le {@code @ConfigurationProperties} vengono scansionate automaticamente.
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
@ConfigurationPropertiesScan
@EntityScan(basePackages = {
        "it.sanitech.payments.repositories.entities",
        OutboxEvent.ENTITY_PACKAGE
})
public class PaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentsApplication.class, args);
    }
}

package it.sanitech.admissions;

import it.sanitech.commons.boot.EnableSanitechPlatform;
import it.sanitech.outbox.persistence.OutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio {@code svc-admissions}.
 *
 * <p>
 * {@link EnableScheduling} abilita i job schedulati (es. publisher outbox).
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
        "it.sanitech.admissions.repositories.entities",
        OutboxEvent.ENTITY_PACKAGE
})
public class AdmissionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmissionsApplication.class, args);
    }
}

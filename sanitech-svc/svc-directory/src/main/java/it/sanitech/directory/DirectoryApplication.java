package it.sanitech.directory;

import it.sanitech.commons.boot.EnableSanitechPlatform;
import it.sanitech.outbox.persistence.OutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point del microservizio "svc-directory" di Sanitech.
 *
 * <p>
 * {@link EnableScheduling} abilita l'esecuzione dei job schedulati, in particolare
 * il publisher Outbox → Kafka.
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
        "it.sanitech.directory.repositories.entities",
        OutboxEvent.ENTITY_PACKAGE
})
public class DirectoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DirectoryApplication.class, args);
    }
}

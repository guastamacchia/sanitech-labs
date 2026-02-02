package it.sanitech.docs;

import it.sanitech.commons.boot.EnableSanitechPlatform;
import it.sanitech.outbox.persistence.OutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio <strong>Sanitech</strong> per la gestione documentale (svc-docs).
 *
 * <p>
 * Il servizio espone API REST per:
 * <ul>
 *   <li>caricare documenti clinici su storage S3/MinIO;</li>
 *   <li>consultare e scaricare i documenti secondo le regole di sicurezza;</li>
 *   <li>pubblicare eventi applicativi via Outbox → Kafka.</li>
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
@EnableScheduling
@EntityScan(basePackages = {
        "it.sanitech.docs.repositories.entities",
        OutboxEvent.ENTITY_PACKAGE
})
public class DocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocsApplication.class, args);
    }
}

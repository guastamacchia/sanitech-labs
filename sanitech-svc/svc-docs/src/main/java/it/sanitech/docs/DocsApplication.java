package it.sanitech.docs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
 */
@SpringBootApplication(scanBasePackages = {"it.sanitech.docs", "it.sanitech.commons", "it.sanitech.outbox"})
@EnableScheduling
public class DocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocsApplication.class, args);
    }
}

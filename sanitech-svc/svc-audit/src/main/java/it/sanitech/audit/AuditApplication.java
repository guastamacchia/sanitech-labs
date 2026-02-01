package it.sanitech.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import it.sanitech.audit.ingestion.AuditIngestionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio {@code svc-audit} per Sanitech.
 */
@SpringBootApplication(scanBasePackages = {"it.sanitech.audit", "it.sanitech.commons", "it.sanitech.outbox"})
@EnableConfigurationProperties(AuditIngestionProperties.class)
@EnableScheduling
public class AuditApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditApplication.class, args);
    }
}

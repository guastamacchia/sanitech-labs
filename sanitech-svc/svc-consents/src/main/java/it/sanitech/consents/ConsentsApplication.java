package it.sanitech.consents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio {@code svc-consents} per Sanitech.
 */
@SpringBootApplication(scanBasePackages = {"it.sanitech.consents", "it.sanitech.commons", "it.sanitech.outbox"})
@EnableScheduling
public class ConsentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsentsApplication.class, args);
    }
}

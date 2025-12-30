package it.sanitech.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point del microservizio {@code svc-payments}.
 *
 * <p>
 * Abilita lo scheduling per il publisher dell'Outbox.
 * Le {@code @ConfigurationProperties} vengono scansionate automaticamente.
 * </p>
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class PaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentsApplication.class, args);
    }
}

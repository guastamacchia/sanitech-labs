package it.sanitech.prescribing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio {@code svc-prescribing}.
 *
 * <p>
 * Abilita {@link EnableScheduling} per il publisher Outbox → Kafka.
 * </p>
 */
@SpringBootApplication
@EnableScheduling
public class PrescribingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrescribingApplication.class, args);
    }
}

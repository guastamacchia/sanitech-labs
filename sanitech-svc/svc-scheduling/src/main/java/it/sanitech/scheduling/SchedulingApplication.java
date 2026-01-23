package it.sanitech.scheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio {@code svc-scheduling} di Sanitech.
 * <p>
 * Abilita lo scheduling per il publisher Outbox verso Kafka.
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"it.sanitech.scheduling", "it.sanitech.commons", "it.sanitech.outbox"})
@EnableScheduling
public class SchedulingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingApplication.class, args);
    }
}

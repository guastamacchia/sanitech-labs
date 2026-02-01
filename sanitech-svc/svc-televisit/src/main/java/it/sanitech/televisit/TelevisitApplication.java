package it.sanitech.televisit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio <strong>svc-televisit</strong> (Sanitech).
 *
 * <p>Abilita {@link EnableScheduling} per il publisher Outbox (flush periodico verso Kafka).</p>
 */
@SpringBootApplication(scanBasePackages = {"it.sanitech.televisit", "it.sanitech.commons", "it.sanitech.outbox"})
@EnableScheduling
public class TelevisitApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelevisitApplication.class, args);
    }
}

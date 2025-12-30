package it.sanitech.directory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point del microservizio "svc-directory" di Sanitech.
 *
 * <p>
 * {@link EnableScheduling} abilita l'esecuzione dei job schedulati, in particolare
 * il publisher Outbox → Kafka.
 * </p>
 */
@SpringBootApplication
@EnableScheduling
public class DirectoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DirectoryApplication.class, args);
    }
}

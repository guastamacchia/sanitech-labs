package it.sanitech.admissions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del microservizio {@code svc-admissions}.
 *
 * <p>
 * {@link EnableScheduling} abilita i job schedulati (es. publisher outbox).
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"it.sanitech.admissions", "it.sanitech.commons", "it.sanitech.outbox"})
@EnableScheduling
public class AdmissionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmissionsApplication.class, args);
    }
}

package it.sanitech.payments;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class per test con Postgres Testcontainers.
 */
@Testcontainers
public abstract class BasePostgresTest {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("sanitech_payments_test")
                    .withUsername("sanitech")
                    .withPassword("sanitech");

    @BeforeAll
    static void startContainer() {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        reg.add("spring.datasource.username", POSTGRES::getUsername);
        reg.add("spring.datasource.password", POSTGRES::getPassword);

        reg.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        reg.add("spring.flyway.enabled", () -> "true");

        // Kafka non serve nei test: settiamo un bootstrap fittizio.
        reg.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");

        // issuer-uri non viene risolto se non arrivano richieste con jwt() nei test.
        reg.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost:8081/realms/sanitech");

        // scheduler disabilitato in application-test.yml
        reg.add("spring.task.scheduling.enabled", () -> "false");
    }
}

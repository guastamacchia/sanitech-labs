package it.sanitech.directory.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.directory.TestJwtDecoderConfig;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di integrazione su {@link OutboxRepository} con PostgreSQL Testcontainers.
 */
@Import(TestJwtDecoderConfig.class)
@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class OutboxRepositoryIT {

    private static final String REMOTE_DOCKER_HOST = System.getenv("TESTCONTAINERS_DOCKER_HOST");

    static {
        if (System.getProperty("DOCKER_HOST") == null
                && REMOTE_DOCKER_HOST != null
                && !REMOTE_DOCKER_HOST.isBlank()) {
            System.setProperty("DOCKER_HOST", REMOTE_DOCKER_HOST.trim());
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("sanitech_directory_test")
            .withUsername("sanitech")
            .withPassword("sanitech");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OutboxRepository outboxRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void assumeDockerAvailable() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker non disponibile: il test di integrazione Outbox viene saltato.");
    }

    @Test
    void lockBatch_returns_only_unpublished_events() throws Exception {
        outboxRepository.deleteAll();

        OutboxEvent e1 = OutboxEvent.builder()
                .aggregateType("DOCTOR")
                .aggregateId("1")
                .eventType("DOCTOR_CREATED")
                .payload(objectMapper.readTree("{\"id\":1}"))
                .occurredAt(Instant.now().minusSeconds(10))
                .published(false)
                .build();

        OutboxEvent e2 = OutboxEvent.builder()
                .aggregateType("PATIENT")
                .aggregateId("10")
                .eventType("PATIENT_CREATED")
                .payload(objectMapper.readTree("{\"id\":10}"))
                .occurredAt(Instant.now().minusSeconds(5))
                .published(false)
                .build();

        OutboxEvent published = OutboxEvent.builder()
                .aggregateType("DOCTOR")
                .aggregateId("2")
                .eventType("DOCTOR_CREATED")
                .payload(objectMapper.readTree("{\"id\":2}"))
                .published(true)
                .build();

        outboxRepository.saveAll(List.of(e1, e2, published));

        List<OutboxEvent> batch = outboxRepository.lockBatch(100);

        assertThat(batch).hasSize(2);
        assertThat(batch).allMatch(e -> !e.isPublished());
    }
}

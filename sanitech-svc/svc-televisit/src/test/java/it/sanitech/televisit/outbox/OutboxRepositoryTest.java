package it.sanitech.televisit.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di integrazione per {@link OutboxRepository} (PostgreSQL + JSONB).
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        // Flyway in test: creiamo lo schema outbox.
        r.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    private OutboxRepository repo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void lockBatchReturnsOnlyUnpublishedInOrder() {
        repo.save(OutboxEvent.builder()
                .aggregateType("A")
                .aggregateId("1")
                .eventType("E1")
                .payload(objectMapper.createObjectNode().put("k", "v1"))
                .occurredAt(Instant.now().minusSeconds(10))
                .published(false)
                .build());

        repo.save(OutboxEvent.builder()
                .aggregateType("A")
                .aggregateId("2")
                .eventType("E2")
                .payload(objectMapper.createObjectNode().put("k", "v2"))
                .occurredAt(Instant.now().minusSeconds(5))
                .published(true)
                .build());

        repo.save(OutboxEvent.builder()
                .aggregateType("B")
                .aggregateId("3")
                .eventType("E3")
                .payload(objectMapper.createObjectNode().put("k", "v3"))
                .occurredAt(Instant.now().minusSeconds(1))
                .published(false)
                .build());

        List<OutboxEvent> batch = repo.lockBatch(10);

        assertThat(batch).hasSize(2);
        assertThat(batch.get(0).isPublished()).isFalse();
        assertThat(batch.get(1).isPublished()).isFalse();
        assertThat(batch.get(0).getOccurredAt()).isBefore(batch.get(1).getOccurredAt());
    }
}

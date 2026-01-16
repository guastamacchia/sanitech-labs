package it.sanitech.scheduling.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import it.sanitech.scheduling.TestJwtDecoderConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
@Testcontainers
class OutboxRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("sanitech_scheduling_test")
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

    @Test
    void lockBatch_returns_only_unpublished_events() throws Exception {
        outboxRepository.deleteAll();

        OutboxEvent e1 = OutboxEvent.builder()
                .aggregateType("SLOT")
                .aggregateId("1")
                .eventType("SLOT_CREATED")
                .payload(objectMapper.readTree("{\"id\":1}"))
                .occurredAt(Instant.now().minusSeconds(10))
                .published(false)
                .build();

        OutboxEvent e2 = OutboxEvent.builder()
                .aggregateType("APPOINTMENT")
                .aggregateId("10")
                .eventType("APPOINTMENT_BOOKED")
                .payload(objectMapper.readTree("{\"id\":10}"))
                .occurredAt(Instant.now().minusSeconds(5))
                .published(false)
                .build();

        OutboxEvent published = OutboxEvent.builder()
                .aggregateType("SLOT")
                .aggregateId("2")
                .eventType("SLOT_CREATED")
                .payload(objectMapper.readTree("{\"id\":2}"))
                .published(true)
                .build();

        outboxRepository.saveAll(List.of(e1, e2, published));

        List<OutboxEvent> batch = outboxRepository.lockBatch(100);

        assertThat(batch).hasSize(2);
        assertThat(batch).allMatch(e -> !e.isPublished());
    }
}

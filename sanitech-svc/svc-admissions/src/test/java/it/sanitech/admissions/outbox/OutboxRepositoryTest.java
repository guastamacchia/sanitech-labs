package it.sanitech.admissions.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test repository Outbox con PostgreSQL reale (Testcontainers).
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    OutboxRepository outboxRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void saveAndLockBatch_returnsUnpublishedEvent() {
        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType("ADMISSION")
                .aggregateId("1")
                .eventType("ADMISSION_CREATED")
                .payload(objectMapper.createObjectNode().put("hello", "world"))
                .published(false)
                .build();

        outboxRepository.save(evt);

        List<OutboxEvent> locked = outboxRepository.lockBatch(10);
        assertThat(locked).hasSize(1);
        assertThat(locked.get(0).isPublished()).isFalse();
        assertThat(locked.get(0).getAggregateType()).isEqualTo("ADMISSION");
    }
}

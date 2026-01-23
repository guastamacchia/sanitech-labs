package it.sanitech.notifications.outbox;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Test di integrazione repository outbox con PostgreSQL reale (Testcontainers).
 */
@DataJpaTest
@Testcontainers
class OutboxRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("sanitech_notifications_test")
            .withUsername("sanitech")
            .withPassword("sanitech");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    OutboxRepository repository;

    @Test
    void lockBatch_returns_unpublished_events_in_order() {
        OutboxEvent e1 = OutboxEvent.builder()
                .aggregateType("A")
                .aggregateId("1")
                .eventType("CREATED")
                .payload(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode().put("k", "v1"))
                .published(false)
                .build();
        OutboxEvent e2 = OutboxEvent.builder()
                .aggregateType("A")
                .aggregateId("2")
                .eventType("CREATED")
                .payload(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode().put("k", "v2"))
                .published(false)
                .build();

        repository.saveAll(List.of(e1, e2));

        List<OutboxEvent> batch = repository.lockBatch(10);

        assertThat(batch).hasSize(2);
        assertThat(batch.get(0).getAggregateId()).isEqualTo("1");
        assertThat(batch.get(1).getAggregateId()).isEqualTo("2");
    }
}

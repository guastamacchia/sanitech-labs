package it.sanitech.docs.outbox;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di integrazione repository Outbox su Postgres reale (Testcontainers).
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sanitech_docs_test")
            .withUsername("sanitech")
            .withPassword("sanitech");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Flyway abilito esplicitamente per applicare V1..V3 su container.
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    OutboxRepository outbox;

    @Test
    void saveAndLockBatch_shouldReturnUnpublishedEvent() {

        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType("DOCUMENT")
                .aggregateId("123")
                .eventType("DOCUMENT_UPLOADED")
                .payload(new ObjectMapper().createObjectNode().put("hello", "world"))
                .build();

        outbox.saveAndFlush(evt);

        var batch = outbox.lockBatch(10);

        assertThat(batch).hasSize(1);
        assertThat(batch.getFirst().isPublished()).isFalse();
        assertThat(batch.getFirst().getAggregateType()).isEqualTo("DOCUMENT");
    }
}

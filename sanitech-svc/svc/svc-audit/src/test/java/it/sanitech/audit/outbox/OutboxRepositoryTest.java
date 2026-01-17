package it.sanitech.audit.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di integrazione su {@link OutboxRepository} con Postgres reale (Testcontainers).
 */
@Testcontainers
@SpringBootTest
class OutboxRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sanitech_audit_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    OutboxRepository outboxRepository;

    @Test
    void saveAndLockBatch_shouldReturnUnpublishedEvents() {
        OutboxEvent e = OutboxEvent.builder()
                .aggregateType("AUDIT_EVENT")
                .aggregateId("1")
                .eventType("AUDIT_RECORDED")
                .payload(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode().put("k", "v"))
                .build();

        outboxRepository.save(e);

        var batch = outboxRepository.lockBatch(10);
        assertThat(batch).hasSize(1);
        assertThat(batch.get(0).isPublished()).isFalse();
        assertThat(batch.get(0).getAggregateType()).isEqualTo("AUDIT_EVENT");
    }
}

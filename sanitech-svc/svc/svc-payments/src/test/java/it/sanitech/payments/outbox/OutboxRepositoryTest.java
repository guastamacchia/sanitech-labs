package it.sanitech.payments.outbox;

import it.sanitech.payments.BasePostgresTest;
import it.sanitech.payments.utilities.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test repository Outbox su Postgres (Testcontainers) per verificare query con JSONB e lockBatch.
 */
@SpringBootTest
class OutboxRepositoryTest extends BasePostgresTest {

    @Autowired
    private OutboxRepository repository;

    @Test
    void saveAndLockBatch_shouldReturnUnpublishedEvents() {
        repository.save(OutboxEvent.newUnpublished(
                AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT, "1", AppConstants.Outbox.EVT_CREATED, "{\"hello\":\"world\"}"
        ));
        repository.save(OutboxEvent.newUnpublished(
                AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT, "2", AppConstants.Outbox.EVT_STATUS_CHANGED, "{\"k\":\"v\"}"
        ));

        List<OutboxEvent> batch = repository.lockBatch(10);

        assertThat(batch).hasSizeGreaterThanOrEqualTo(2);
        assertThat(batch).allMatch(e -> !e.isPublished());
    }
}

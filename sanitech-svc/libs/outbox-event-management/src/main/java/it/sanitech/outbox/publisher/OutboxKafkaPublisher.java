package it.sanitech.outbox.publisher;

import io.micrometer.core.instrument.MeterRegistry;
import it.sanitech.outbox.autoconfigure.OutboxProperties;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Publisher Outbox.
 *
 * Regola chiave: la SELECT ... FOR UPDATE SKIP LOCKED deve avvenire DENTRO transazione.
 */
@Slf4j
public class OutboxKafkaPublisher {

    private static final String METRICA_PUBBLICATI = "sanitech.outbox.published";
    private static final String METRICA_FALLITI = "sanitech.outbox.failed";

    private final TransactionTemplate tx;
    private final OutboxRepository outboxRepository;
    private final OutboxKafkaSender sender;
    private final OutboxProperties props;
    private final MeterRegistry meterRegistry; // opzionale

    public OutboxKafkaPublisher(TransactionTemplate tx,
                                OutboxRepository outboxRepository,
                                OutboxKafkaSender sender,
                                OutboxProperties props,
                                MeterRegistry meterRegistry) {
        this.tx = Objects.requireNonNull(tx, "TransactionTemplate obbligatorio");
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "OutboxRepository obbligatorio");
        this.sender = Objects.requireNonNull(sender, "OutboxKafkaSender obbligatorio");
        this.props = Objects.requireNonNull(props, "OutboxProperties obbligatorio");
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelayString = "${sanitech.outbox.publisher.fixed-delay-ms:2000}")
    public void publishBatch() {
        if (!props.isEnabled() || !props.getPublisher().isEnabled()) {
            log.trace("Outbox: publisher disabilitato da configurazione, salto esecuzione.");
            return;
        }

        final int batchSize = props.getPublisher().getBatchSize();
        final long timeoutMs = props.getPublisher().getSendTimeoutMs();
        final String topic = props.getPublisher().getTopic();

        try {
            tx.executeWithoutResult(status -> {
                // 1) LOCK + lettura batch (SKIP LOCKED efficace perché siamo in transazione)
                List<OutboxEvent> batch = outboxRepository.lockBatch(batchSize);

                if (batch.isEmpty()) {
                    log.trace("Outbox: nessun evento da pubblicare.");
                    return;
                }

                log.debug("Outbox: prelevati {} eventi da pubblicare (batchSize={}, timeoutMs={}, topic='{}').",
                        batch.size(), batchSize, timeoutMs, topic);

                // 2) Invio Kafka sincrono con timeout. Se fallisce -> eccezione -> rollback.
                for (OutboxEvent e : batch) {
                    sender.sendSync(topic, e, timeoutMs);
                }

                // 3) Marca come pubblicati solo dopo ACK
                outboxRepository.markPublished(
                        batch.stream().map(OutboxEvent::getId).toList(),
                        Instant.now()
                );

                if (meterRegistry != null) {
                    meterRegistry.counter(METRICA_PUBBLICATI).increment(batch.size());
                }

                log.debug("Outbox: pubblicazione completata. Eventi marcati come pubblicati: {}.", batch.size());
            });
        } catch (Exception ex) {
            if (meterRegistry != null) {
                meterRegistry.counter(METRICA_FALLITI).increment();
            }
            log.error("Outbox: errore durante la pubblicazione del batch. La transazione è stata annullata. Causa: {}",
                    ex.getMessage(), ex);
        }
    }
}

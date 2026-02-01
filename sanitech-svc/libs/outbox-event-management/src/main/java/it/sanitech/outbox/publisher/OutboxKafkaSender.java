package it.sanitech.outbox.publisher;

import it.sanitech.outbox.persistence.OutboxEvent;

/**
 * Astrazione di invio Kafka per rendere testabile il publisher.
 */
public interface OutboxKafkaSender {
    void sendSync(String topic, OutboxEvent event, long timeoutMs);
}

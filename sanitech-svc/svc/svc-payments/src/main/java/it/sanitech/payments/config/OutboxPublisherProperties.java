package it.sanitech.payments.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà dedicate al publisher Outbox.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "sanitech.outbox.publisher")
public class OutboxPublisherProperties {

    /** Delay tra un batch e il successivo (millisecondi). */
    private long delayMs = 1000;

    /** Numero massimo eventi per batch. */
    private int batchSize = 100;
}

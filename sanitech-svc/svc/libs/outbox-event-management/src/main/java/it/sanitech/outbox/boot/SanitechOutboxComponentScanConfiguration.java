package it.sanitech.outbox.boot;

import it.sanitech.outbox.core.DomainEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import esplicito dei componenti core dell'outbox.
 */
@Configuration
@Import(DomainEventPublisher.class)
public class SanitechOutboxComponentScanConfiguration {
}

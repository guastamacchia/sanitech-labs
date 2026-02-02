package it.sanitech.outbox.boot;

import org.springframework.context.annotation.Configuration;

/**
 * Configurazione marker per il modulo outbox.
 *
 * <p>
 * Questa configurazione viene importata tramite {@code @EnableSanitechPlatform}
 * come marker per indicare che il modulo outbox è abilitato.
 * </p>
 *
 * <p>
 * <strong>Nota:</strong> I bean e i componenti outbox sono gestiti dalle auto-configuration:
 * <ul>
 *   <li>{@link SanitechOutboxJpaConfiguration}: repository e entity JPA</li>
 *   <li>{@link it.sanitech.outbox.autoconfigure.OutboxAutoConfiguration}: DomainEventPublisher</li>
 *   <li>{@link it.sanitech.outbox.autoconfigure.OutboxPublisherAutoConfiguration}: Kafka publisher</li>
 * </ul>
 * </p>
 */
@Configuration
public class SanitechOutboxComponentScanConfiguration {
}

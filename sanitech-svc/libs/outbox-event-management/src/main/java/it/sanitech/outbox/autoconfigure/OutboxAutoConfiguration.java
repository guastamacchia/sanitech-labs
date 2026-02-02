package it.sanitech.outbox.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.outbox.boot.SanitechOutboxJpaConfiguration;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configurazione base Outbox.
 *
 * <p>
 * Espone {@link DomainEventPublisher} come bean, utilizzabile dai service
 * dei microservizi per pubblicare eventi domain nel pattern Transactional Outbox.
 * </p>
 *
 * <p>
 * L'ordine di esecuzione è garantito da {@code after = SanitechOutboxJpaConfiguration.class},
 * che assicura che {@link OutboxRepository} sia già disponibile quando questa
 * configurazione viene processata.
 * </p>
 */
@Slf4j
@AutoConfiguration(after = SanitechOutboxJpaConfiguration.class)
@ConditionalOnProperty(prefix = "sanitech.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnClass({OutboxRepository.class, OutboxEvent.class})
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DomainEventPublisher.class)
    public DomainEventPublisher domainEventPublisher(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        log.debug("Outbox: registrazione DomainEventPublisher.");
        return new DomainEventPublisher(outboxRepository, objectMapper);
    }
}

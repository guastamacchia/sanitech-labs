package it.sanitech.commons.autoconfigure;

import it.sanitech.commons.audit.AuditAspect;
import it.sanitech.outbox.core.DomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Auto-configurazione per l'AuditAspect.
 * <p>
 * L'aspect viene registrato automaticamente se:
 * <ul>
 *   <li>Spring AOP è nel classpath ({@link Aspect})</li>
 *   <li>{@link DomainEventPublisher} è disponibile come bean</li>
 *   <li>La property {@code sanitech.audit.aspect.enabled} è true (default)</li>
 * </ul>
 * </p>
 * <p>
 * Per disabilitare l'aspect, impostare:
 * <pre>
 * sanitech.audit.aspect.enabled=false
 * </pre>
 * </p>
 */
@Slf4j
@AutoConfiguration(afterName = "it.sanitech.outbox.autoconfigure.OutboxAutoConfiguration")
@EnableAspectJAutoProxy
@ConditionalOnClass({Aspect.class, DomainEventPublisher.class})
@ConditionalOnProperty(
        prefix = "sanitech.audit.aspect",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AuditAspectAutoConfiguration {

    public AuditAspectAutoConfiguration() {
        log.info("Audit: AuditAspectAutoConfiguration caricata - configurazione aspect auditing attiva");
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DomainEventPublisher.class)
    public AuditAspect auditAspect(DomainEventPublisher eventPublisher) {
        log.info("Audit: creazione bean AuditAspect per auditing automatico delle API");
        return new AuditAspect(eventPublisher);
    }
}

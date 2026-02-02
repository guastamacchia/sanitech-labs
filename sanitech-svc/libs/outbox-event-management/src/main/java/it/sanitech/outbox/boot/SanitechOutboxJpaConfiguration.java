package it.sanitech.outbox.boot;

import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auto-configurazione JPA per il modulo outbox.
 *
 * <p>
 * Registra {@link OutboxRepository} come repository JPA e {@link OutboxEvent}
 * come entity gestita da Hibernate.
 * </p>
 *
 * <p>
 * <strong>Importante:</strong> Questa configurazione viene eseguita <em>dopo</em>
 * le auto-configuration JPA di Spring Boot per garantire che i repository e le entity
 * dell'outbox vengano aggiunti a quelli già configurati dal microservizio.
 * </p>
 *
 * <p>
 * <strong>Nota per i microservizi:</strong> Le entity locali del microservizio devono
 * essere incluse esplicitamente con {@code @EntityScan}:
 * <pre>{@code
 * @EntityScan(basePackages = {
 *     "it.sanitech.tuomicroservizio.repositories.entities",
 *     OutboxEvent.ENTITY_PACKAGE
 * })
 * }</pre>
 * </p>
 *
 * @see OutboxRepository
 * @see OutboxEvent
 */
@AutoConfiguration(after = {
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@ConditionalOnClass(name = "jakarta.persistence.EntityManager")
@EnableJpaRepositories(basePackageClasses = OutboxRepository.class)
@EntityScan(basePackageClasses = OutboxEvent.class)
public class SanitechOutboxJpaConfiguration {
}

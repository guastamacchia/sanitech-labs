package it.sanitech.notifications.services;

import it.sanitech.notifications.NotificationsProperties;
import it.sanitech.notifications.repositories.NotificationRepository;
import it.sanitech.notifications.repositories.entities.Notification;
import it.sanitech.notifications.repositories.entities.NotificationStatus;
import it.sanitech.notifications.utilities.AppConstants;
import it.sanitech.outbox.core.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Dispatcher schedulato per l'invio delle notifiche EMAIL.
 *
 * <p>
 * Legge le notifiche {@link NotificationStatus#PENDING} e le invia via SMTP.
 * La selezione avviene con lock pessimista (SKIP LOCKED) per supportare più repliche in parallelo.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class NotificationDispatcher {

    private static final String AGGREGATE_TYPE = "NOTIFICATION";

    private final NotificationRepository repository;
    private final NotificationsProperties properties;
    private final SmtpEmailSender emailSender;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Esegue periodicamente il dispatch delle notifiche email PENDING.
     */
    @Scheduled(fixedDelayString = "${sanitech.notifications.dispatcher.delay-ms:2000}")
    @Transactional
    public void dispatchPendingEmails() {
        List<Notification> batch = repository.lockPendingEmailBatch(properties.getDispatcher().getBatchSize());

        for (Notification n : batch) {
            try {
                emailSender.send(
                        properties.getMail().getFrom(),
                        n.getToAddress(),
                        n.getSubject(),
                        n.getBody()
                );

                n.markSent(Instant.now());

                domainEventPublisher.publish(
                        AGGREGATE_TYPE,
                        String.valueOf(n.getId()),
                        "NOTIFICATION_SENT",
                        Map.of(
                                "id", n.getId(),
                                "channel", n.getChannel().name(),
                                "sentAt", String.valueOf(n.getSentAt())
                        ),
                        AppConstants.Outbox.TOPIC_AUDITS_EVENTS
                );

            } catch (Exception ex) {
                // Dopo i retry configurati, se fallisce marchiamo FAILED (no loop infinito).
                n.markFailed(ex.getMessage());

                domainEventPublisher.publish(
                        AGGREGATE_TYPE,
                        String.valueOf(n.getId()),
                        "NOTIFICATION_FAILED",
                        Map.of(
                                "id", n.getId(),
                                "reason", ex.getClass().getSimpleName()
                        ),
                        AppConstants.Outbox.TOPIC_AUDITS_EVENTS
                );
            }
        }
        // Le modifiche (SENT/FAILED) vengono persistite al commit della transazione.
    }
}

package it.sanitech.notifications.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.notifications.repositories.NotificationRepository;
import it.sanitech.notifications.repositories.entities.*;
import it.sanitech.notifications.services.dto.NotificationDto;
import it.sanitech.notifications.services.dto.create.NotificationCreateDto;
import it.sanitech.notifications.services.mapper.NotificationMapper;
import it.sanitech.outbox.core.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service layer del bounded context Notifications.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String AGGREGATE_TYPE = "NOTIFICATION";

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Crea una notifica.
     *
     * <p>
     * Per il canale EMAIL la notifica viene salvata in stato {@link NotificationStatus#PENDING}
     * e verrà inviata dal dispatcher schedulato. Per IN_APP viene marcata {@link NotificationStatus#SENT}
     * immediatamente.
     * </p>
     */
    @Transactional
    public NotificationDto create(NotificationCreateDto dto) {
        Notification entity = mapper.toEntity(dto);

        // Validazione di difesa (oltre alla bean validation)
        if (entity.getChannel() == NotificationChannel.EMAIL) {
            if (entity.getToAddress() == null || entity.getToAddress().isBlank()) {
                throw new IllegalArgumentException("Per il canale EMAIL è obbligatorio l'indirizzo email del destinatario.");
            }
        }

        // Stato iniziale coerente con il canale
        if (entity.getChannel() == NotificationChannel.IN_APP) {
            entity.setStatus(NotificationStatus.SENT);
            entity.setSentAt(Instant.now());
        } else {
            entity.setStatus(NotificationStatus.PENDING);
        }

        Notification saved = repository.save(entity);

        domainEventPublisher.publish(
                AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                "NOTIFICATION_CREATED",
                Map.of(
                        "id", saved.getId(),
                        "recipientType", saved.getRecipientType().name(),
                        "recipientId", saved.getRecipientId(),
                        "channel", saved.getChannel().name(),
                        "status", saved.getStatus().name(),
                        "createdAt", String.valueOf(saved.getCreatedAt())
                )
        );

        return mapper.toDto(saved);
    }

    @Transactional
    public List<NotificationDto> bulkCreate(List<NotificationCreateDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }
        return dtos.stream().map(this::create).toList();
    }

    @Transactional(readOnly = true)
    public NotificationDto get(Long id) {
        Notification entity = repository.findById(id).orElseThrow(() -> NotFoundException.of("Notifica", id));
        return mapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> listForRecipient(RecipientType recipientType, String recipientId, Pageable pageable) {
        return repository.findByRecipientTypeAndRecipientId(recipientType, recipientId, pageable).map(mapper::toDto);
    }

    @Transactional
    public void delete(Long id) {
        Notification entity = repository.findById(id).orElseThrow(() -> NotFoundException.of("Notifica", id));
        repository.delete(entity);

        domainEventPublisher.publish(
                AGGREGATE_TYPE,
                String.valueOf(id),
                "NOTIFICATION_DELETED",
                Map.of("id", id)
        );
    }
}

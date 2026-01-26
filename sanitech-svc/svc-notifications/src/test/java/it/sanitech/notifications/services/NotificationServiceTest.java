package it.sanitech.notifications.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.notifications.repositories.NotificationRepository;
import it.sanitech.notifications.repositories.entities.Notification;
import it.sanitech.notifications.repositories.entities.NotificationChannel;
import it.sanitech.notifications.repositories.entities.NotificationStatus;
import it.sanitech.notifications.repositories.entities.RecipientType;
import it.sanitech.notifications.services.dto.NotificationDto;
import it.sanitech.notifications.services.dto.create.NotificationCreateDto;
import it.sanitech.notifications.services.mapper.NotificationMapper;
import it.sanitech.outbox.core.DomainEventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class NotificationServiceTest {

    @Test
    void createMarksInAppAsSent() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        NotificationMapper mapper = Mockito.mock(NotificationMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        NotificationService service = new NotificationService(repository, mapper, publisher);

        NotificationCreateDto dto = new NotificationCreateDto(
                RecipientType.PATIENT,
                "p1",
                NotificationChannel.IN_APP,
                null,
                "Subject",
                "Body"
        );

        Notification entity = Notification.builder()
                .recipientType(RecipientType.PATIENT)
                .recipientId("p1")
                .channel(NotificationChannel.IN_APP)
                .subject("Subject")
                .body("Body")
                .build();

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            saved.setId(10L);
            saved.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
            return saved;
        });
        when(mapper.toDto(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            return new NotificationDto(
                    saved.getId(),
                    saved.getRecipientType(),
                    saved.getRecipientId(),
                    saved.getChannel(),
                    saved.getToAddress(),
                    saved.getSubject(),
                    saved.getBody(),
                    saved.getStatus(),
                    saved.getCreatedAt(),
                    saved.getSentAt(),
                    saved.getErrorMessage()
            );
        });

        NotificationDto result = service.create(dto);

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.sentAt()).isNotNull();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);

        verify(publisher).publish(eq("NOTIFICATION"), eq("10"), eq("NOTIFICATION_CREATED"), any());
    }

    @Test
    void createEmailRequiresAddress() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        NotificationMapper mapper = Mockito.mock(NotificationMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        NotificationService service = new NotificationService(repository, mapper, publisher);

        NotificationCreateDto dto = new NotificationCreateDto(
                RecipientType.DOCTOR,
                "d1",
                NotificationChannel.EMAIL,
                "",
                "Subject",
                "Body"
        );

        Notification entity = Notification.builder()
                .recipientType(RecipientType.DOCTOR)
                .recipientId("d1")
                .channel(NotificationChannel.EMAIL)
                .toAddress("")
                .subject("Subject")
                .body("Body")
                .build();

        when(mapper.toEntity(dto)).thenReturn(entity);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listForRecipientMapsPage() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        NotificationMapper mapper = Mockito.mock(NotificationMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        NotificationService service = new NotificationService(repository, mapper, publisher);

        Notification notification = Notification.builder()
                .id(5L)
                .recipientType(RecipientType.PATIENT)
                .recipientId("p1")
                .channel(NotificationChannel.IN_APP)
                .subject("Subject")
                .body("Body")
                .status(NotificationStatus.SENT)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .sentAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        Page<Notification> page = new PageImpl<>(List.of(notification), PageRequest.of(0, 5), 1);
        when(repository.findByRecipientTypeAndRecipientId(eq(RecipientType.PATIENT), eq("p1"), any(Pageable.class)))
                .thenReturn(page);
        when(mapper.toDto(notification)).thenReturn(new NotificationDto(
                notification.getId(),
                notification.getRecipientType(),
                notification.getRecipientId(),
                notification.getChannel(),
                notification.getToAddress(),
                notification.getSubject(),
                notification.getBody(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getErrorMessage()
        ));

        Page<NotificationDto> result = service.listForRecipient(RecipientType.PATIENT, "p1", PageRequest.of(0, 5));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(5L);
    }

    @Test
    void deleteRemovesNotificationAndPublishesEvent() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        NotificationMapper mapper = Mockito.mock(NotificationMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        NotificationService service = new NotificationService(repository, mapper, publisher);

        Notification notification = Notification.builder()
                .id(7L)
                .recipientType(RecipientType.ADMIN)
                .recipientId("admin")
                .channel(NotificationChannel.IN_APP)
                .subject("Subject")
                .body("Body")
                .status(NotificationStatus.SENT)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        when(repository.findById(7L)).thenReturn(Optional.of(notification));

        service.delete(7L);

        verify(repository).delete(notification);
        verify(publisher).publish(eq("NOTIFICATION"), eq("7"), eq("NOTIFICATION_DELETED"), any());
    }

    @Test
    void getThrowsWhenMissing() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        NotificationMapper mapper = Mockito.mock(NotificationMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        NotificationService service = new NotificationService(repository, mapper, publisher);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class);
    }
}

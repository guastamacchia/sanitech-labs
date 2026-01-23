package it.sanitech.notifications.services.mapper;

import it.sanitech.notifications.repositories.entities.Notification;
import it.sanitech.notifications.services.dto.NotificationDto;
import it.sanitech.notifications.services.dto.create.NotificationCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct per convertire tra DTO e entity.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(Notification entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    Notification toEntity(NotificationCreateDto dto);
}

package it.sanitech.televisit.services.mapper;

import it.sanitech.televisit.repositories.entities.TelevisitSession;
import it.sanitech.televisit.services.dto.TelevisitDto;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct per convertire {@link TelevisitSession} ↔ DTO.
 */
@Mapper(componentModel = "spring")
public interface TelevisitMapper {

    TelevisitDto toDto(TelevisitSession entity);
}

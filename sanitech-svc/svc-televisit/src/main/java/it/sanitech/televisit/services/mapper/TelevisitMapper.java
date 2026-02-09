package it.sanitech.televisit.services.mapper;

import it.sanitech.televisit.repositories.entities.TelevisitSession;
import it.sanitech.televisit.services.dto.TelevisitDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct per convertire {@link TelevisitSession} ↔ DTO.
 */
@Mapper(componentModel = "spring")
public interface TelevisitMapper {

    @Mapping(target = "patientName", ignore = true)
    @Mapping(target = "doctorName", ignore = true)
    TelevisitDto toDto(TelevisitSession entity);
}

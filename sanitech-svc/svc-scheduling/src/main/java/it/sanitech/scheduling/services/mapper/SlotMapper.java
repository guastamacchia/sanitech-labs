package it.sanitech.scheduling.services.mapper;

import it.sanitech.scheduling.repositories.entities.Slot;
import it.sanitech.scheduling.services.dto.SlotDto;
import it.sanitech.scheduling.services.dto.create.SlotCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct per {@link Slot}.
 */
@Mapper(componentModel = "spring")
public interface SlotMapper {

    SlotDto toDto(Slot entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "createdAt", ignore = true)
    Slot fromCreateDto(SlotCreateDto dto);
}

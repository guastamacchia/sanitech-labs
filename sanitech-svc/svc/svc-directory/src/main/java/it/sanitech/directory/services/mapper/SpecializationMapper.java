package it.sanitech.directory.services.mapper;

import it.sanitech.directory.repositories.entities.Specialization;
import it.sanitech.directory.services.dto.SpecializationDto;
import it.sanitech.directory.services.dto.create.SpecializationCreateDto;
import it.sanitech.directory.services.dto.update.SpecializationUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per {@link Specialization}.
 *
 * <p>
 * Supporta conversioni tra entità e DTO e l'aggiornamento parziale dei campi,
 * evitando la sovrascrittura dei valori non presenti nel payload.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpecializationMapper {

    SpecializationDto toDto(Specialization entity);

    @Mapping(target = "id", ignore = true)
    Specialization toEntity(SpecializationCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SpecializationUpdateDto dto, @MappingTarget Specialization entity);
}

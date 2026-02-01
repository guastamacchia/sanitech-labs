package it.sanitech.directory.services.mapper;

import it.sanitech.directory.repositories.entities.Facility;
import it.sanitech.directory.services.dto.FacilityDto;
import it.sanitech.directory.services.dto.create.FacilityCreateDto;
import it.sanitech.directory.services.dto.update.FacilityUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per {@link Facility}.
 *
 * <p>
 * Supporta conversioni tra entità e DTO e l'aggiornamento parziale dei campi,
 * evitando la sovrascrittura dei valori non presenti nel payload.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FacilityMapper {

    FacilityDto toDto(Facility entity);

    @Mapping(target = "id", ignore = true)
    Facility toEntity(FacilityCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(FacilityUpdateDto dto, @MappingTarget Facility entity);
}

package it.sanitech.consents.services.mapper;

import it.sanitech.consents.repositories.entities.Consent;
import it.sanitech.consents.services.dto.ConsentDto;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct per convertire {@link Consent} ↔ DTO.
 */
@Mapper(componentModel = "spring")
public interface ConsentMapper {

    ConsentDto toDto(Consent entity);
}

package it.sanitech.docs.services.mappers;

import it.sanitech.docs.repositories.entities.Document;
import it.sanitech.docs.services.dto.DocumentDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper MapStruct per convertire {@link Document} ↔ DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DocumentMapper {

    DocumentDto toDto(Document entity);
}

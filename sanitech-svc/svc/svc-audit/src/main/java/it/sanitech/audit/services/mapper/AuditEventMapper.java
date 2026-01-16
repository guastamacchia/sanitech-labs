package it.sanitech.audit.services.mapper;

import it.sanitech.audit.repositories.entities.AuditEvent;
import it.sanitech.audit.services.dto.AuditEventDto;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct per convertire {@link AuditEvent} ↔ DTO.
 */
@Mapper(componentModel = "spring")
public interface AuditEventMapper {

    AuditEventDto toDto(AuditEvent entity);
}

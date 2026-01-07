package it.sanitech.admissions.services.mapper;

import it.sanitech.admissions.repositories.entities.Admission;
import it.sanitech.admissions.services.dto.AdmissionDto;
import it.sanitech.admissions.services.dto.create.AdmissionCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct tra entità {@link Admission} e DTO.
 */
@Mapper(componentModel = "spring")
public interface AdmissionMapper {

    AdmissionDto toDto(Admission entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "admittedAt", ignore = true)
    @Mapping(target = "dischargedAt", ignore = true)
    Admission toEntity(AdmissionCreateDto dto);
}

package it.sanitech.directory.services.mapper;

import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per {@link Doctor}.
 *
 * <p>
 * Le relazioni con reparto vengono gestite nel Service:
 * in patch/update si evita di farle modificare automaticamente.
 * Il facilityCode è derivato dalla gerarchia Reparto -> Struttura.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorMapper {

    @Mapping(target = "departmentCode", source = "department.code")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "facilityCode", source = "department.facility.code")
    @Mapping(target = "facilityName", source = "department.facility.name")
    DoctorDto toDto(Doctor entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "activatedAt", ignore = true)
    void updateEntity(DoctorUpdateDto dto, @MappingTarget Doctor entity);
}

package it.sanitech.directory.services.mapper;

import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per {@link Doctor}.
 *
 * <p>
 * Le relazioni con reparto/specializzazione vengono gestite nel Service:
 * in patch/update si evita di farle modificare automaticamente.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorMapper {

    @Mapping(target = "departmentCode", source = "department.code")
    @Mapping(target = "specializationCode", source = "specialization.code")
    DoctorDto toDto(Doctor entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    void updateEntity(DoctorUpdateDto dto, @MappingTarget Doctor entity);
}

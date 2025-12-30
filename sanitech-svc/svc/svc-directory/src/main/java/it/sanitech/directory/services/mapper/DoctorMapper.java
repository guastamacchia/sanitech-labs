package it.sanitech.directory.services.mapper;

import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per {@link Doctor}.
 *
 * <p>
 * Le collezioni (reparti/specializzazioni) vengono gestite nel Service:
 * in patch/update si evita di farle modificare automaticamente per non introdurre
 * semantiche ambigue (merge vs replace).
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {DepartmentMapper.class, SpecializationMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorMapper {

    DoctorDto toDto(Doctor entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "departments", ignore = true)
    @Mapping(target = "specializations", ignore = true)
    void updateEntity(DoctorUpdateDto dto, @MappingTarget Doctor entity);
}

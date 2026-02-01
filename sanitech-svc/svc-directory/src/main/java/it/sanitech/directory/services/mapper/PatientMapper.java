package it.sanitech.directory.services.mapper;

import it.sanitech.directory.repositories.entities.Patient;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.update.PatientUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per {@link Patient}.
 *
 * <p>
 * La collezione reparti viene gestita nel Service (replace esplicito se presente nel DTO).
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {DepartmentMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PatientMapper {

    PatientDto toDto(Patient entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "departments", ignore = true)
    void updateEntity(PatientUpdateDto dto, @MappingTarget Patient entity);
}

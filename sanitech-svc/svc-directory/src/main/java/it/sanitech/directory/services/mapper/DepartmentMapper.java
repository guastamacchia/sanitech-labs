package it.sanitech.directory.services.mapper;

import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.directory.services.dto.create.DepartmentCreateDto;
import it.sanitech.directory.services.dto.update.DepartmentUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per {@link Department}.
 *
 * <p>
 * Gestisce la conversione tra entità e DTO per l'anagrafica reparti, applicando
 * la strategia di update "null-ignore" per supportare patch parziali.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    DepartmentDto toDto(Department entity);

    @Mapping(target = "id", ignore = true)
    Department toEntity(DepartmentCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(DepartmentUpdateDto dto, @MappingTarget Department entity);
}

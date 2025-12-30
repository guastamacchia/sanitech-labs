package it.sanitech.directory.services.mapper;

import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.directory.services.dto.create.DepartmentCreateDto;
import it.sanitech.directory.services.dto.update.DepartmentUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per {@link Department}.
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

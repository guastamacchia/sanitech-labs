package it.sanitech.payments.services.mapper;

import it.sanitech.payments.repositories.entities.ServicePerformed;
import it.sanitech.payments.services.dto.ServicePerformedDto;
import it.sanitech.payments.services.dto.update.ServicePerformedUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per conversione Entity/DTO delle prestazioni sanitarie.
 */
@Mapper(componentModel = "spring")
public interface ServicePerformedMapper {

    ServicePerformedDto toDto(ServicePerformed entity);

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        ignoreByDefault = true
    )
    @Mapping(target = "amountCents", source = "amountCents")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentType", source = "paymentType")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "patientName", source = "patientName")
    @Mapping(target = "patientEmail", source = "patientEmail")
    void patch(ServicePerformedUpdateDto dto, @MappingTarget ServicePerformed entity);
}

package it.sanitech.payments.services.mapper;

import it.sanitech.payments.repositories.entities.PaymentOrder;
import it.sanitech.payments.services.dto.PaymentOrderDto;
import it.sanitech.payments.services.dto.create.PaymentAdminCreateDto;
import it.sanitech.payments.services.dto.create.PaymentCreateDto;
import it.sanitech.payments.services.dto.update.PaymentUpdateDto;
import org.mapstruct.*;

/**
 * Mapper MapStruct per conversione Entity/DTO.
 */
@Mapper(componentModel = "spring")
public interface PaymentOrderMapper {

    PaymentOrderDto toDto(PaymentOrder entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerReference", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    PaymentOrder toEntity(PaymentCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerReference", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    PaymentOrder toEntity(PaymentAdminCreateDto dto);

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        ignoreByDefault = true
    )
    @Mapping(target = "status", source = "status")
    @Mapping(target = "providerReference", source = "providerReference")
    @Mapping(target = "description", source = "description")
    void patch(PaymentUpdateDto dto, @MappingTarget PaymentOrder entity);
}

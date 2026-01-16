package it.sanitech.prescribing.services.mappers;

import it.sanitech.prescribing.repositories.entities.Prescription;
import it.sanitech.prescribing.repositories.entities.PrescriptionItem;
import it.sanitech.prescribing.services.dto.PrescriptionDto;
import it.sanitech.prescribing.services.dto.PrescriptionItemDto;
import it.sanitech.prescribing.services.dto.create.PrescriptionCreateDto;
import it.sanitech.prescribing.services.dto.create.PrescriptionItemCreateDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionItemUpdateDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionPatchDto;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct per convertire tra entità JPA e DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PrescriptionMapper {

    PrescriptionDto toDto(Prescription entity);

    PrescriptionItemDto toDto(PrescriptionItem entity);

    List<PrescriptionItemDto> toItemDtos(List<PrescriptionItem> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctorId", ignore = true) // impostato dal service in base al token
    @Mapping(target = "status", ignore = true)   // impostato dal service (DRAFT/ISSUED)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "issuedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "items", ignore = true)    // gestito dal service per impostare la back-reference
    Prescription toEntity(PrescriptionCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prescription", ignore = true) // la back-reference viene impostata dal service
    PrescriptionItem toEntity(PrescriptionItemCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prescription", ignore = true)
    PrescriptionItem toEntity(PrescriptionItemUpdateDto dto);

    List<PrescriptionItem> toItemEntities(List<PrescriptionItemCreateDto> dtos);

    List<PrescriptionItem> toItemUpdateEntities(List<PrescriptionItemUpdateDto> dtos);

    /**
     * Patch parziale: i null vengono ignorati.
     */
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        ignoreByDefault = true
    )
    @Mapping(target = "notes", source = "notes")
    void patch(@MappingTarget Prescription target, PrescriptionPatchDto dto);
}

package it.sanitech.consents.services.mapper;

import it.sanitech.consents.repositories.entities.PrivacyConsent;
import it.sanitech.consents.services.dto.PrivacyConsentDto;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct per convertire {@link PrivacyConsent} ↔ DTO.
 */
@Mapper(componentModel = "spring")
public interface PrivacyConsentMapper {

    PrivacyConsentDto toDto(PrivacyConsent entity);
}

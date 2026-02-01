package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per creare una nuova struttura.
 *
 * <p>
 * Richiede codice e nome della struttura sanitaria.
 * </p>
 */
public record FacilityCreateDto(

        /** Codice struttura (univoco). */
        @NotBlank
        String code,

        /** Nome leggibile struttura. */
        @NotBlank
        String name
) {}

package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per aggiornare una struttura esistente.
 *
 * <p>
 * Il codice non è modificabile; solo il nome può essere aggiornato.
 * </p>
 */
public record FacilityUpdateDto(

        /** Nuovo nome leggibile struttura. */
        @NotBlank
        String name
) {}

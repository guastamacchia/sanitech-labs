package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO per aggiornare un reparto.
 *
 * <p>
 * Per semplicità, il {@code code} non viene modificato: si aggiorna solo il {@code name}.
 * La validazione garantisce un valore non vuoto prima della persistenza.
 * </p>
 */
public record DepartmentUpdateDto(

        /** Nome leggibile del reparto. */
        @NotBlank
        String name
) {}

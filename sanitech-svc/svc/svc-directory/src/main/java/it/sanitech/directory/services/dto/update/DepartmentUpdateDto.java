package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO per aggiornare un reparto.
 *
 * <p>
 * Per semplicità, il {@code code} non viene modificato: si aggiorna solo il {@code name}.
 * </p>
 */
public record DepartmentUpdateDto(

        /** Nome leggibile del reparto. */
        @NotBlank
        String name
) {}

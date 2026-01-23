package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO per aggiornare una specializzazione.
 *
 * <p>
 * Per semplicità, il {@code code} non viene modificato: si aggiorna solo il {@code name}.
 * La validazione assicura che il nuovo nome sia valorizzato.
 * </p>
 */
public record SpecializationUpdateDto(

        /** Nome leggibile della specializzazione. */
        @NotBlank
        String name
) {}

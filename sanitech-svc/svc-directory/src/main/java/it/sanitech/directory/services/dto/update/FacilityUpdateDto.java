package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per aggiornare una struttura esistente.
 *
 * <p>
 * Il codice non è modificabile; nome, indirizzo e telefono possono essere aggiornati.
 * </p>
 */
public record FacilityUpdateDto(

        /** Nuovo nome leggibile struttura. */
        @NotBlank
        String name,

        /** Indirizzo della struttura (opzionale). */
        String address,

        /** Numero di telefono della struttura (opzionale). */
        String phone
) {}

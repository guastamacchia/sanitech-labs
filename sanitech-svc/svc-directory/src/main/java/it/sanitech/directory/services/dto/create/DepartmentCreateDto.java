package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per creare un nuovo reparto.
 *
 * <p>
 * Richiede codice, nome e codice struttura valorizzati; il codice viene normalizzato e validato
 * dal service prima della persistenza.
 * </p>
 */
public record DepartmentCreateDto(

        /** Codice reparto (univoco). */
        @NotBlank
        String code,

        /** Nome leggibile del reparto. */
        @NotBlank
        String name,

        /** Capacità posti letto (opzionale, default 0). */
        Integer capacity,

        /** Codice della struttura di appartenenza. */
        @NotBlank
        String facilityCode
) {}

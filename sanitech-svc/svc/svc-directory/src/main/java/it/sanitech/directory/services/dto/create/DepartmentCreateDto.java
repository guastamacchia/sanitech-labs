package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per creare un nuovo reparto.
 */
public record DepartmentCreateDto(

        /** Codice reparto (univoco). */
        @NotBlank
        String code,

        /** Nome leggibile del reparto. */
        @NotBlank
        String name
) {}

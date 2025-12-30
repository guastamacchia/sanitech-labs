package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per creare una nuova specializzazione.
 */
public record SpecializationCreateDto(

        /** Codice specializzazione (univoco). */
        @NotBlank
        String code,

        /** Nome leggibile della specializzazione. */
        @NotBlank
        String name
) {}

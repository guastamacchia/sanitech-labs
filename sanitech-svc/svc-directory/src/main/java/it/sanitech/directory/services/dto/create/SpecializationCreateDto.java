package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per creare una nuova specializzazione.
 *
 * <p>
 * Richiede codice e nome, che verranno normalizzati e validati
 * dal service prima della persistenza.
 * </p>
 */
public record SpecializationCreateDto(

        /** Codice specializzazione (univoco). */
        @NotBlank
        String code,

        /** Nome leggibile della specializzazione. */
        @NotBlank
        String name
) {}

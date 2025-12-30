package it.sanitech.directory.services.dto;

/**
 * DTO di lettura per Specializzazione.
 */
public record SpecializationDto(

        /** Identificatore della specializzazione. */
        Long id,

        /** Codice specializzazione (univoco). */
        String code,

        /** Nome leggibile specializzazione. */
        String name
) {}

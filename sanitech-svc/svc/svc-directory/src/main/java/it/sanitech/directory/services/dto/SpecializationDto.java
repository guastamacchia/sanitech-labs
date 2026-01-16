package it.sanitech.directory.services.dto;

/**
 * DTO di lettura per Specializzazione.
 *
 * <p>
 * Espone i dati anagrafici della specializzazione per le API di consultazione.
 * </p>
 */
public record SpecializationDto(

        /** Identificatore della specializzazione. */
        Long id,

        /** Codice specializzazione (univoco). */
        String code,

        /** Nome leggibile specializzazione. */
        String name
) {}

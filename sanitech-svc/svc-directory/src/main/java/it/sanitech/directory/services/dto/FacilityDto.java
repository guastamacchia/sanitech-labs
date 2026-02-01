package it.sanitech.directory.services.dto;

/**
 * DTO di lettura per Struttura (Facility).
 *
 * <p>
 * Espone i dati anagrafici della struttura sanitaria per le API di consultazione.
 * </p>
 */
public record FacilityDto(

        /** Identificatore della struttura. */
        Long id,

        /** Codice struttura (univoco). */
        String code,

        /** Nome leggibile struttura. */
        String name
) {}

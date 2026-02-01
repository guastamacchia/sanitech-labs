package it.sanitech.directory.services.dto;

/**
 * DTO di lettura per Reparto.
 *
 * <p>
 * Veicola i dati essenziali dell'anagrafica reparto verso i controller REST
 * e verso i client di consultazione, incluso il riferimento alla struttura di appartenenza.
 * </p>
 */
public record DepartmentDto(

        /** Identificatore del reparto. */
        Long id,

        /** Codice reparto (univoco). */
        String code,

        /** Nome leggibile reparto. */
        String name,

        /** Codice della struttura di appartenenza. */
        String facilityCode
) {}

package it.sanitech.directory.services.dto;

/**
 * DTO di lettura per Reparto.
 */
public record DepartmentDto(

        /** Identificatore del reparto. */
        Long id,

        /** Codice reparto (univoco). */
        String code,

        /** Nome leggibile reparto. */
        String name
) {}

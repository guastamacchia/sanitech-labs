package it.sanitech.directory.services.dto;

import java.util.Set;

/**
 * DTO utilizzato per esporre i dati anagrafici del paziente verso i controller REST.
 *
 * <p>
 * Include i reparti di appartenenza per supportare la visualizzazione e i filtri lato client.
 * </p>
 */
public record PatientDto(

        /** Identificatore univoco del paziente. */
        Long id,

        /** Nome del paziente. */
        String firstName,

        /** Cognome del paziente. */
        String lastName,

        /** Indirizzo email univoco del paziente. */
        String email,

        /** Numero di telefono del paziente (opzionale). */
        String phone,

        /** Reparti associati al paziente. */
        Set<DepartmentDto> departments

) {}

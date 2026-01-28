package it.sanitech.directory.services.dto;

/**
 * DTO utilizzato per esporre i dati anagrafici del medico verso i controller REST.
 *
 * <p>
 * Include i riferimenti al reparto e alla specializzazione associati (codici).
 * </p>
 */
public record DoctorDto(

        /** Identificatore univoco del medico. */
        Long id,

        /** Nome del medico. */
        String firstName,

        /** Cognome del medico. */
        String lastName,

        /** Indirizzo email univoco del medico. */
        String email,

        /** Codice del reparto associato al medico. */
        String departmentCode,

        /** Codice della specializzazione associata al medico. */
        String specializationCode

) {}

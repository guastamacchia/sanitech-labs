package it.sanitech.directory.services.dto;

import java.util.Set;

/**
 * DTO utilizzato per esporre i dati anagrafici del medico verso i controller REST.
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

        /** Reparti associati al medico. */
        Set<DepartmentDto> departments,

        /** Specializzazioni associate al medico. */
        Set<SpecializationDto> specializations

) {}

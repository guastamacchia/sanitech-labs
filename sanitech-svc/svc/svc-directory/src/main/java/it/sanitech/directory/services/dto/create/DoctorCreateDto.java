package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * DTO utilizzato per creare un nuovo medico.
 *
 * <p>
 * Richiede dati anagrafici e l'associazione ad almeno un reparto e una specializzazione,
 * che verranno validati dal service prima della creazione.
 * </p>
 */
public record DoctorCreateDto(

        /** Nome del medico. */
        @NotBlank
        String firstName,

        /** Cognome del medico. */
        @NotBlank
        String lastName,

        /** Email del medico (univoca). */
        @Email
        @NotBlank
        String email,

        /** Codici dei reparti associati al medico (almeno uno). */
        @NotEmpty
        Set<String> departmentCodes,

        /** Codici delle specializzazioni associate al medico (almeno una). */
        @NotEmpty
        Set<String> specializationCodes

) {}

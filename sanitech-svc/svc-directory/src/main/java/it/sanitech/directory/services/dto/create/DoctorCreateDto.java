package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per creare un nuovo medico.
 *
 * <p>
 * Richiede dati anagrafici e l'associazione a un reparto e una specializzazione,
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

        /** Codice del reparto associato al medico. */
        @NotBlank
        String departmentCode,

        /** Codice della specializzazione associata al medico. */
        @NotBlank
        String specializationCode

) {}

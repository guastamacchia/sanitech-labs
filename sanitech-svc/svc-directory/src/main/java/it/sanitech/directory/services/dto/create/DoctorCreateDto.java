package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizzato per creare un nuovo medico.
 *
 * <p>
 * Richiede dati anagrafici e l'associazione a un reparto,
 * che verrà validato dal service prima della creazione.
 * Il reparto determina implicitamente la struttura di appartenenza.
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

        /** Numero di telefono del medico (opzionale). */
        String phone,

        /** Specializzazione del medico (opzionale). */
        String specialization,

        /** Codice del reparto associato al medico. */
        @NotBlank
        String departmentCode

) {}

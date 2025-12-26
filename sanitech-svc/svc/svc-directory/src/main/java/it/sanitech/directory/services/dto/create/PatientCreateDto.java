package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

/**
 * DTO utilizzato per creare un nuovo paziente.
 */
public record PatientCreateDto(

        /** Nome del paziente. */
        @NotBlank
        String firstName,

        /** Cognome del paziente. */
        @NotBlank
        String lastName,

        /** Email del paziente (univoca). */
        @Email
        @NotBlank
        String email,

        /** Numero di telefono (opzionale). */
        String phone,

        /** Codici dei reparti associati al paziente (opzionale). */
        Set<String> departmentCodes

) {}

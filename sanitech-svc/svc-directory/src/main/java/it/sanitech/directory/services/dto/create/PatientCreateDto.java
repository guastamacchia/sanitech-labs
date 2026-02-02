package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO utilizzato per creare un nuovo paziente.
 *
 * <p>
 * Contiene i dati anagrafici minimi e un elenco opzionale di reparti,
 * soggetto a validazione ABAC nel service.
 * </p>
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

        /** Codice fiscale (opzionale). */
        String fiscalCode,

        /** Data di nascita (opzionale). */
        LocalDate birthDate,

        /** Indirizzo di residenza (opzionale). */
        String address,

        /** Codici dei reparti associati al paziente (opzionale). */
        Set<String> departmentCodes

) {}

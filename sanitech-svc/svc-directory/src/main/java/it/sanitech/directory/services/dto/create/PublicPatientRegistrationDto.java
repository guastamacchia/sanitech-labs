package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * DTO per la registrazione pubblica di un paziente.
 *
 * <p>
 * Utilizzato dall'endpoint pubblico per raccogliere i dati anagrafici di base.
 * </p>
 */
public record PublicPatientRegistrationDto(

        /** Nome del paziente. */
        @NotBlank
        String firstName,

        /** Cognome del paziente. */
        @NotBlank
        String lastName,

        /** Email del paziente. */
        @Email
        @NotBlank
        String email,

        /** Numero di telefono. */
        String phone,

        /** Codice fiscale. */
        @NotBlank
        String fiscalCode,

        /** Data di nascita (opzionale). */
        LocalDate birthDate,

        /** Indirizzo di residenza (opzionale). */
        String address,

        /** Token reCAPTCHA per la verifica. */
        String captchaToken

) {}

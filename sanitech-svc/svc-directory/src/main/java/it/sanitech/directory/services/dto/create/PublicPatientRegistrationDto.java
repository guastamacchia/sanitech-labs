package it.sanitech.directory.services.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO per la registrazione pubblica di un paziente.
 *
 * <p>
 * Utilizzato dall'endpoint pubblico per raccogliere i dati anagrafici di base.
 * Il campo {@code notes} è informativo e non viene persistito nella Directory.
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

        /** Numero di telefono (opzionale). */
        String phone,

        /** Note opzionali inserite dal paziente. */
        String notes,

        /** Token reCAPTCHA per la verifica. */
        String captchaToken

) {}

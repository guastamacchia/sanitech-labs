package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.Email;

import java.util.Set;

/**
 * DTO utilizzato per aggiornare parzialmente i dati anagrafici di un paziente.
 *
 * <p>
 * Tutti i campi sono opzionali: i valori {@code null} vengono ignorati dal mapper MapStruct
 * (mapping con {@code nullValuePropertyMappingStrategy = IGNORE}). I codici reparto, se presenti,
 * sostituiscono l'insieme corrente dopo validazione ABAC.
 * </p>
 */
public record PatientUpdateDto(

        /** Nome del paziente (opzionale). */
        String firstName,

        /** Cognome del paziente (opzionale). */
        String lastName,

        /** Email del paziente. Se presente, deve essere in formato valido. */
        @Email
        String email,

        /** Numero di telefono (opzionale). */
        String phone,

        /** Codici reparto (opzionale). Se presente, sostituisce l'insieme corrente. */
        Set<String> departmentCodes

) { }

package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.Email;

import java.util.Set;

/**
 * DTO utilizzato per aggiornare parzialmente i dati anagrafici di un medico.
 *
 * <p>
 * Tutti i campi sono opzionali: i valori {@code null} vengono ignorati dal mapper MapStruct
 * (mapping con {@code nullValuePropertyMappingStrategy = IGNORE}).
 * </p>
 */
public record DoctorUpdateDto(

        /** Nome del medico (opzionale). */
        String firstName,

        /** Cognome del medico (opzionale). */
        String lastName,

        /** Email del medico. Se presente, deve essere in formato valido. */
        @Email
        String email,

        /** Codici reparto (opzionale). Se presente, sostituisce l'insieme corrente. */
        Set<String> departmentCodes,

        /** Codici specializzazioni (opzionale). Se presente, sostituisce l'insieme corrente. */
        Set<String> specializationCodes

) { }

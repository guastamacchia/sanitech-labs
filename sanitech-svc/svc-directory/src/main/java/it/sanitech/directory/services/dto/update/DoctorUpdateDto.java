package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.Email;

/**
 * DTO utilizzato per aggiornare parzialmente i dati anagrafici di un medico.
 *
 * <p>
 * Tutti i campi sono opzionali: i valori {@code null} vengono ignorati dal mapper MapStruct
 * (mapping con {@code nullValuePropertyMappingStrategy = IGNORE}). I codici reparto e
 * specializzazione, se presenti, vengono validati e sostituiscono i valori correnti.
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

        /** Numero di telefono del medico (opzionale). */
        String phone,

        /** Codice reparto (opzionale). Se presente, sostituisce il valore corrente. */
        String departmentCode,

        /** Codice specializzazione (opzionale). Se presente, sostituisce il valore corrente. */
        String specializationCode

) { }

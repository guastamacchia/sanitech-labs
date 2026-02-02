package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.Email;

/**
 * DTO utilizzato per aggiornare parzialmente i dati anagrafici di un medico.
 *
 * <p>
 * Tutti i campi sono opzionali: i valori {@code null} vengono ignorati dal mapper MapStruct
 * (mapping con {@code nullValuePropertyMappingStrategy = IGNORE}). Il codice reparto,
 * se presente, viene validato e sostituisce il valore corrente.
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

        /** Specializzazione del medico (opzionale). */
        String specialization,

        /** Codice reparto (opzionale). Se presente, sostituisce il valore corrente. */
        String departmentCode

) { }

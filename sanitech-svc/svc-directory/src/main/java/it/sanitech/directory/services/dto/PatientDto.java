package it.sanitech.directory.services.dto;

import it.sanitech.directory.repositories.entities.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * DTO utilizzato per esporre i dati anagrafici del paziente verso i controller REST.
 *
 * <p>
 * Include i reparti di appartenenza per supportare la visualizzazione e i filtri lato client.
 * </p>
 */
public record PatientDto(

        /** Identificatore univoco del paziente. */
        Long id,

        /** Nome del paziente. */
        String firstName,

        /** Cognome del paziente. */
        String lastName,

        /** Indirizzo email univoco del paziente. */
        String email,

        /** Numero di telefono del paziente (opzionale). */
        String phone,

        /** Codice fiscale del paziente. */
        String fiscalCode,

        /** Data di nascita. */
        LocalDate birthDate,

        /** Indirizzo di residenza. */
        String address,

        /** Stato dell'account (PENDING, ACTIVE, DISABLED). */
        UserStatus status,

        /** Data/ora di registrazione. */
        Instant registeredAt,

        /** Data/ora di attivazione dell'account. */
        Instant activatedAt,

        /** Reparti associati al paziente. */
        Set<DepartmentDto> departments

) {}

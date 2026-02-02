package it.sanitech.directory.services.dto;

import it.sanitech.directory.repositories.entities.UserStatus;

import java.time.Instant;

/**
 * DTO utilizzato per esporre i dati anagrafici del medico verso i controller REST.
 *
 * <p>
 * Include i riferimenti al reparto e alla struttura di appartenenza (codici).
 * Gerarchia: Struttura -> Reparto -> Medico.
 * </p>
 */
public record DoctorDto(

        /** Identificatore univoco del medico. */
        Long id,

        /** Nome del medico. */
        String firstName,

        /** Cognome del medico. */
        String lastName,

        /** Indirizzo email univoco del medico. */
        String email,

        /** Numero di telefono del medico (opzionale). */
        String phone,

        /** Specializzazione del medico (opzionale). */
        String specialization,

        /** Stato dell'account (PENDING, ACTIVE, DISABLED). */
        UserStatus status,

        /** Data/ora di creazione dell'account. */
        Instant createdAt,

        /** Data/ora di attivazione dell'account. */
        Instant activatedAt,

        /** Codice del reparto associato al medico. */
        String departmentCode,

        /** Nome del reparto (es. Cardiologia). */
        String departmentName,

        /** Codice della struttura di appartenenza (derivato dal reparto). */
        String facilityCode,

        /** Nome della struttura (es. Ospedale San Giovanni). */
        String facilityName

) {}

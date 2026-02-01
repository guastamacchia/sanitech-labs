package it.sanitech.directory.services.dto.update;

/**
 * DTO utilizzato per aggiornare esclusivamente il numero di telefono di un paziente.
 *
 * <p>
 * Utilizzato dall'endpoint "me" per consentire ai pazienti di modificare
 * il proprio numero di telefono senza poter modificare l'email (username).
 * </p>
 */
public record PatientPhoneUpdateDto(

        /** Numero di telefono (opzionale, può essere null o vuoto). */
        String phone

) { }

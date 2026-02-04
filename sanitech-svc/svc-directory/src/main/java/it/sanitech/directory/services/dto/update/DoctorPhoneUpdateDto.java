package it.sanitech.directory.services.dto.update;

/**
 * DTO utilizzato per aggiornare esclusivamente il numero di telefono di un medico.
 *
 * <p>
 * Utilizzato dall'endpoint "me" per consentire ai medici di modificare
 * il proprio numero di telefono senza poter modificare l'email (username).
 * </p>
 */
public record DoctorPhoneUpdateDto(

        /** Numero di telefono (opzionale, può essere null o vuoto). */
        String phone

) { }

package it.sanitech.directory.services.dto.update;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizzato per aggiornare esclusivamente il numero di telefono di un medico.
 *
 * <p>
 * Utilizzato dall'endpoint "me" per consentire ai medici di modificare
 * il proprio numero di telefono senza poter modificare l'email (username).
 * </p>
 */
public record DoctorPhoneUpdateDto(

        /** Numero di telefono (opzionale, può essere null per rimuoverlo). */
        @Size(min = 10, max = 20, message = "Il numero di telefono deve avere tra 10 e 20 caratteri.")
        @Pattern(regexp = "^\\+?[\\d\\s\\-()]+$", message = "Il numero di telefono può contenere solo cifre, spazi, trattini, parentesi e il prefisso +.")
        String phone

) { }

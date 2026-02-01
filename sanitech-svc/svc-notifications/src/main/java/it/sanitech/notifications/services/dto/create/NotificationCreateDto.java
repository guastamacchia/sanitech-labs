package it.sanitech.notifications.services.dto.create;

import it.sanitech.notifications.repositories.entities.NotificationChannel;
import it.sanitech.notifications.repositories.entities.RecipientType;
import jakarta.validation.constraints.*;

import org.hibernate.validator.constraints.Length;

/**
 * DTO di input per creare una notifica.
 */
public record NotificationCreateDto(

        @NotNull
        RecipientType recipientType,

        @NotBlank
        @Length(max = 64)
        String recipientId,

        @NotNull
        NotificationChannel channel,

        /**
         * Per il canale EMAIL deve essere valorizzato e in formato valido.
         * Per IN_APP può essere {@code null}.
         */
        @Email
        @Length(max = 200)
        String toAddress,

        @NotBlank
        @Length(max = 200)
        String subject,

        @NotBlank
        String body

) {

    /**
     * Validazione cross-field: se il canale è EMAIL, l'indirizzo deve essere presente.
     */
    @AssertTrue(message = "Per il canale EMAIL è obbligatorio un indirizzo email valido")
    public boolean isEmailPresentWhenNeeded() {
        if (channel != NotificationChannel.EMAIL) {
            return true;
        }
        return toAddress != null && !toAddress.isBlank();
    }
}

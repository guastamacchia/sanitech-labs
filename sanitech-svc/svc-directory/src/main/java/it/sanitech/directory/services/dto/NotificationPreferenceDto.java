package it.sanitech.directory.services.dto;

/**
 * DTO per le preferenze di notifica del paziente.
 *
 * <p>
 * Espone le preferenze configurabili per ogni categoria di notifica:
 * <ul>
 *   <li>Promemoria appuntamenti (email/SMS)</li>
 *   <li>Nuovi documenti clinici (email/SMS)</li>
 *   <li>Pagamenti e fatture (email/SMS)</li>
 * </ul>
 * </p>
 */
public record NotificationPreferenceDto(

        /** Ricevi promemoria appuntamenti via email. */
        boolean emailReminders,

        /** Ricevi promemoria appuntamenti via SMS. */
        boolean smsReminders,

        /** Ricevi notifica nuovi documenti clinici via email. */
        boolean emailDocuments,

        /** Ricevi notifica nuovi documenti clinici via SMS. */
        boolean smsDocuments,

        /** Ricevi notifica pagamenti e fatture via email. */
        boolean emailPayments,

        /** Ricevi notifica pagamenti e fatture via SMS. */
        boolean smsPayments

) {
    /**
     * Crea un DTO con i valori di default.
     * Di default sono attive solo le notifiche email.
     */
    public static NotificationPreferenceDto defaults() {
        return new NotificationPreferenceDto(
                true,   // emailReminders
                false,  // smsReminders
                true,   // emailDocuments
                false,  // smsDocuments
                true,   // emailPayments
                false   // smsPayments
        );
    }
}

package it.sanitech.notifications.repositories.entities;

/**
 * Canale di consegna della notifica.
 *
 * <p>
 * In questo microservizio implementiamo in modo “production-ready”:
 * <ul>
 *   <li>{@link #EMAIL} (invio SMTP)</li>
 *   <li>{@link #IN_APP} (persistenza e consultazione via API)</li>
 * </ul>
 * </p>
 */
public enum NotificationChannel {
    EMAIL,
    IN_APP
}

package it.sanitech.notifications.repositories.entities;

/**
 * Stato di lavorazione della notifica.
 *
 * <ul>
 *   <li>PENDING — in coda (EMAIL non ancora inviata)</li>
 *   <li>SENT — consegnata / disponibile in-app</li>
 *   <li>FAILED — invio fallito</li>
 *   <li>READ — letta dall'utente</li>
 *   <li>ARCHIVED — archiviata dall'utente</li>
 * </ul>
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    READ,
    ARCHIVED
}

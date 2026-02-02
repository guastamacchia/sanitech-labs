package it.sanitech.televisit.repositories.entities;

/**
 * Stati principali di una sessione di video-visita.
 */
public enum TelevisitStatus {
    CREATED,
    SCHEDULED,
    ACTIVE,
    ENDED,
    CANCELED
}

package it.sanitech.directory.repositories.entities;

/**
 * Stato dell'utente nella piattaforma.
 *
 * <ul>
 *   <li>{@code PENDING} - Utente registrato ma non ancora attivato (in attesa di conferma email o primo accesso).</li>
 *   <li>{@code ACTIVE} - Utente attivo e operativo.</li>
 *   <li>{@code DISABLED} - Accesso temporaneamente sospeso dall'amministratore.</li>
 * </ul>
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    DISABLED
}

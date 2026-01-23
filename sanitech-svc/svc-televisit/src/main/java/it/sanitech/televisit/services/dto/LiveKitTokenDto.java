package it.sanitech.televisit.services.dto;

/**
 * DTO di risposta per il token LiveKit.
 */
public record LiveKitTokenDto(

        /** Room LiveKit associata alla sessione. */
        String roomName,

        /** URL del LiveKit server (da configurazione). */
        String livekitUrl,

        /** Token JWT firmato per join alla room. */
        String token,

        /** TTL residuo del token (secondi). */
        long expiresInSeconds

) {
}

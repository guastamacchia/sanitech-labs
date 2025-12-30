package it.sanitech.consents.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Rappresentazione RFC 7807 "Problem Details".
 * <p>
 * Spring espone già {@code org.springframework.http.ProblemDetail}; qui usiamo una versione
 * leggera e controllata per mantenere formato e naming coerenti tra i microservizi Sanitech.
 * </p>
 */
@Getter
@Builder
public class ProblemDetails {

    /** URI che identifica in modo univoco il tipo di problema. */
    private String type;

    /** Riassunto breve e leggibile del problema. */
    private String title;

    /** HTTP status code. */
    private int status;

    /** Dettaglio specifico dell'errore. */
    private String detail;

    /** URI della risorsa/endpoint che ha generato l'errore. */
    private String instance;

    /** Timestamp (UTC) di generazione del problema. */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Campo esteso opzionale per inserire dettagli strutturati (es. errori di validazione).
     * Manteniamo il tipo {@code Object} per flessibilità.
     */
    private Object extra;
}

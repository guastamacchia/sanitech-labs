package it.sanitech.commons.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

/**
 * Modello RFC 7807 "Problem Details" per risposte di errore HTTP.
 *
 * <p>
 * Campi standard (RFC 7807):
 * <ul>
 *   <li>{@code type}: URI che identifica la tipologia di problema</li>
 *   <li>{@code title}: titolo breve, leggibile (human-readable)</li>
 *   <li>{@code status}: HTTP status code</li>
 *   <li>{@code detail}: descrizione dettagliata dell'errore</li>
 *   <li>{@code instance}: URI della richiesta che ha generato l'errore</li>
 * </ul>
 * </p>
 *
 * <p>
 * Campo esteso (non standard RFC 7807):
 * <ul>
 *   <li>{@code extra}: payload aggiuntivo strutturato (es. errori field-level)</li>
 * </ul>
 * </p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ProblemDetails {

    /** URI che identifica la tipologia di problema (RFC 7807). */
    private final String type;

    /** Titolo breve e leggibile del problema. */
    private final String title;

    /** HTTP status code associato al problema. */
    private final Integer status;

    /** Descrizione dettagliata dell'errore. */
    private final String detail;

    /** URI della richiesta che ha generato l'errore. */
    private final String instance;

    /**
     * Payload aggiuntivo (non standard RFC 7807) per arricchire la risposta.
     *
     * <p>
     * Tipicamente usato per:
     * <ul>
     *   <li>errori di validazione field-level</li>
     *   <li>dettagli strutturati per il frontend</li>
     * </ul>
     * </p>
     */
    private final Object extra;
}

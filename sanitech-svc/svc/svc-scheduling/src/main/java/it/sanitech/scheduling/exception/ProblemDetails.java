package it.sanitech.scheduling.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Modello RFC 7807 "Problem Details" per risposte di errore.
 *
 * <p>
 * Campi standard:
 * <ul>
 *   <li>{@code type}: URI che identifica la tipologia di problema</li>
 *   <li>{@code title}: titolo breve, leggibile</li>
 *   <li>{@code status}: HTTP status code</li>
 *   <li>{@code detail}: dettaglio descrittivo</li>
 *   <li>{@code instance}: URI della richiesta che ha generato l'errore</li>
 * </ul>
 *
 * Campo esteso:
 * <ul>
 *   <li>{@code extra}: dettagli aggiuntivi (es. lista errori di validazione)</li>
 * </ul>
 * </p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;

    /**
     * Payload aggiuntivo (non standard RFC 7807) per arricchire la risposta.
     * Esempio tipico: lista errori field-level in caso di validazione.
     */
    private Object extra;
}

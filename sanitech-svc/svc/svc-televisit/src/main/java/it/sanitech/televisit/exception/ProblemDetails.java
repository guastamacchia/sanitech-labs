package it.sanitech.televisit.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Modello di risposta errori conforme a RFC 7807 (Problem Details).
 *
 * <p>Campi standard:
 * <ul>
 *   <li>{@code type} — URI che identifica la tipologia di errore</li>
 *   <li>{@code title} — titolo breve, leggibile</li>
 *   <li>{@code status} — HTTP status code</li>
 *   <li>{@code detail} — dettaglio descrittivo</li>
 *   <li>{@code instance} — URI della richiesta che ha generato l'errore</li>
 * </ul>
 *
 * <p>Campo estensione:
 * <ul>
 *   <li>{@code extra} — payload aggiuntivo (es. lista errori di validazione)</li>
 * </ul>
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;

    /**
     * Campo estensione RFC 7807 (application/problem+json).
     * Può contenere dettagli strutturati (es. errori per campo).
     */
    private Object extra;
}

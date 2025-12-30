package it.sanitech.admissions.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

/**
 * Rappresenta una risposta di errore in formato RFC 7807 (Problem Details).
 *
 * <p>
 * Campi standard:
 * <ul>
 *   <li>{@code type}: URI che identifica la categoria del problema</li>
 *   <li>{@code title}: breve titolo leggibile</li>
 *   <li>{@code status}: codice HTTP</li>
 *   <li>{@code detail}: descrizione estesa</li>
 *   <li>{@code instance}: URI/Path della richiesta che ha generato l'errore</li>
 * </ul>
 * </p>
 *
 * <p>
 * Campo opzionale {@code extra} per dettagli strutturati (es. errori di validazione).
 * </p>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiProblem {
    String type;
    String title;
    int status;
    String detail;
    String instance;
    Object extra;
}

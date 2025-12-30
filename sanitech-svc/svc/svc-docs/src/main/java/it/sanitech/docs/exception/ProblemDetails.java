package it.sanitech.docs.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

/**
 * Rappresentazione RFC 7807 (Problem Details).
 *
 * <p>
 * Viene restituita come body delle risposte di errore per garantire:
 * <ul>
 *   <li>formato uniforme;</li>
 *   <li>informazioni minimali ma utili;</li>
 *   <li>possibilità di estendere con un campo {@code extra} (opzionale).</li>
 * </ul>
 * </p>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {

    /** URI che identifica il tipo di problema (RFC 7807 "type"). */
    String type;

    /** Titolo sintetico del problema (RFC 7807 "title"). */
    String title;

    /** Status HTTP numerico (RFC 7807 "status"). */
    Integer status;

    /** Descrizione dettagliata per l'utente/client (RFC 7807 "detail"). */
    String detail;

    /** URI della risorsa richiesta (RFC 7807 "instance"). */
    String instance;

    /** Estensione libera (es. lista errori di validazione). */
    Object extra;
}

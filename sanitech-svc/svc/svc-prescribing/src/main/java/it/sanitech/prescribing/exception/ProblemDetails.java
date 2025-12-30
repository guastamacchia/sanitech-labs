package it.sanitech.prescribing.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Rappresentazione compatta di RFC 7807 (Problem Details).
 *
 * <p>
 * Spring espone anche {@code org.springframework.http.ProblemDetail}; qui manteniamo un DTO
 * controllato per avere un payload consistente e facilmente estendibile (campo {@code extra}).
 * </p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {

    private final String type;
    private final String title;
    private final Integer status;
    private final String detail;
    private final String instance;

    /**
     * Dati aggiuntivi (es. errori di validazione) senza rompere il contratto principale.
     */
    private final Map<String, Object> extra;
}

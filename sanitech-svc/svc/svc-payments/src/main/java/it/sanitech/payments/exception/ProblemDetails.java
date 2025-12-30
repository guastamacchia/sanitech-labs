package it.sanitech.payments.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * DTO RFC 7807 (Problem Details).
 *
 * <p>
 * Campi standard: {@code type,title,status,detail,instance}.
 * Il campo {@code extra} è un'estensione lecita RFC 7807 per aggiungere dettagli contestuali.
 * </p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {

    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;

    /** Campo esteso (es. lista errori di validazione). */
    private Object extra;

    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();
}

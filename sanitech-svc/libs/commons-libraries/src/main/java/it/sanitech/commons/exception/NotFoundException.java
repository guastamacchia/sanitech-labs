package it.sanitech.commons.exception;

import it.sanitech.commons.utilities.AppConstants;

import java.util.Objects;

/**
 * Eccezione applicativa che indica l'assenza di una risorsa richiesta (HTTP 404).
 */
public class NotFoundException extends RuntimeException {

    private NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory per lookup per identificatore.
     *
     * @param entity nome logico dell'entità
     * @param id     identificatore ricercato
     */
    public static NotFoundException of(String entity, Object id) {
        String safeEntity = normalize(entity);
        String safeId = normalize(id);

        return new NotFoundException(String.format(AppConstants.ErrorMessage.MSG_NOT_FOUND_BY_ID, safeEntity, safeId));
    }

    /**
     * Factory generica quando il dettaglio dell'identificatore non è rilevante.
     */
    public static NotFoundException of(String entity) {
        String safeEntity = normalize(entity);

        return new NotFoundException(String.format(AppConstants.ErrorMessage.MSG_NOT_FOUND, safeEntity));
    }

    /**
     * Normalizza un valore per uso nei messaggi di errore.
     */
    private static String normalize(Object value) {
        if (Objects.isNull(value)) {
            return AppConstants.ErrorMessage.FALLBACK_VALUE;
        }
        String s = value.toString().trim();
        return s.isBlank() ? AppConstants.ErrorMessage.FALLBACK_VALUE : s;
    }
}

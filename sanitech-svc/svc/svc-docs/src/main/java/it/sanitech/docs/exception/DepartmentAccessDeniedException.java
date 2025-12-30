package it.sanitech.docs.exception;

/**
 * Eccezione applicativa lanciata quando un utente tenta di operare su un reparto
 * per il quale non possiede autorizzazione (ABAC).
 */
public class DepartmentAccessDeniedException extends RuntimeException {

    private DepartmentAccessDeniedException(String message) {
        super(message);
    }

    /**
     * Factory method: costruisce un messaggio coerente per il reparto richiesto.
     *
     * @param dept reparto richiesto
     * @return eccezione pronta da lanciare
     */
    public static DepartmentAccessDeniedException forDepartment(String dept) {
        String normalized = (dept == null || dept.isBlank()) ? "<non specificato>" : dept.trim().toUpperCase();
        return new DepartmentAccessDeniedException("Accesso non autorizzato al reparto " + normalized + ".");
    }
}

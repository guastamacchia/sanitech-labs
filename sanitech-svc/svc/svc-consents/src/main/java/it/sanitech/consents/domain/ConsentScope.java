package it.sanitech.consents.domain;

/**
 * Ambito del consenso.
 * <p>
 * L'ambito consente di distinguere che cosa il medico può consultare (es. cartella clinica, documenti, ecc.).
 * </p>
 */
public enum ConsentScope {
    RECORDS,
    DOCS,
    PRESCRIPTIONS
}

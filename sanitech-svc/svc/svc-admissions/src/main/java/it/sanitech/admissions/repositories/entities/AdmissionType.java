package it.sanitech.admissions.repositories.entities;

/**
 * Tipologia di ricovero.
 *
 * <p>
 * Nota: le visite/controlli sono gestiti da {@code svc-scheduling}.
 * </p>
 */
public enum AdmissionType {
    INPATIENT,
    DAY_HOSPITAL,
    OBSERVATION
}

package it.sanitech.directory.services.dto;

/**
 * DTO di lettura per Reparto.
 *
 * <p>
 * Veicola i dati essenziali dell'anagrafica reparto verso i controller REST
 * e verso i client di consultazione, incluso il riferimento alla struttura di appartenenza.
 * </p>
 */
public record DepartmentDto(

        /** Identificatore del reparto. */
        Long id,

        /** Codice reparto (univoco). */
        String code,

        /** Nome leggibile reparto. */
        String name,

        /** Capacità posti letto. */
        Integer capacity,

        /** Codice della struttura di appartenenza. */
        String facilityCode,

        /** Nome della struttura di appartenenza. */
        String facilityName,

        /** Numero di medici associati al reparto. */
        Long doctorCount
) {
    /**
     * Costruttore di compatibilità senza doctorCount (usato dal mapper).
     */
    public DepartmentDto(Long id, String code, String name, Integer capacity,
                         String facilityCode, String facilityName) {
        this(id, code, name, capacity, facilityCode, facilityName, 0L);
    }

    /**
     * Crea una copia del DTO con il conteggio medici aggiornato.
     *
     * @param doctorCount il nuovo conteggio medici
     * @return nuovo DTO con il conteggio aggiornato
     */
    public DepartmentDto withDoctorCount(Long doctorCount) {
        return new DepartmentDto(id, code, name, capacity, facilityCode, facilityName, doctorCount);
    }
}

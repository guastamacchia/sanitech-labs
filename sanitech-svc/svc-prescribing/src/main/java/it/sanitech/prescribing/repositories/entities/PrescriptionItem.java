package it.sanitech.prescribing.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Riga di prescrizione: rappresenta un singolo farmaco/terapia all'interno di una {@link Prescription}.
 */
@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Prescrizione di appartenenza.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    /**
     * Codice farmaco (es. codice interno o ATC). Opzionale ma consigliato per integrazioni.
     */
    @Column(name = "medication_code", length = 64)
    private String medicationCode;

    /**
     * Nome descrittivo del farmaco/terapia (richiesto).
     */
    @Column(name = "medication_name", nullable = false, length = 200)
    private String medicationName;

    /**
     * Dosaggio (es. "1 compressa", "10mg").
     */
    @Column(nullable = false, length = 100)
    private String dosage;

    /**
     * Frequenza (es. "2 volte al giorno", "ogni 8 ore").
     */
    @Column(nullable = false, length = 80)
    private String frequency;

    /**
     * Durata in giorni (opzionale).
     */
    @Column(name = "duration_days")
    private Integer durationDays;

    /**
     * Istruzioni libere (opzionale).
     */
    @Column(columnDefinition = "text")
    private String instructions;

    /**
     * Ordinamento esplicito dei farmaci (utile per export e UI).
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
}

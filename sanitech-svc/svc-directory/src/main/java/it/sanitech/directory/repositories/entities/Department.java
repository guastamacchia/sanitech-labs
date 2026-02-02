package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Anagrafica Reparto.
 *
 * <p>
 * Modella i reparti clinici con un codice stabile e un nome descrittivo, utilizzati sia per la
 * presentazione in UI sia per le policy ABAC (authority {@code DEPT_*}) applicate agli accessi.
 * Il vincolo di unicità sul codice garantisce la stabilità dei riferimenti nel tempo.
 * Ogni reparto appartiene a una Struttura (Facility) del network Sanitech Labs.
 * </p>
 */
@Entity
@Table(name = "departments", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "code")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codice reparto (univoco, es. HEART). */
    @Column(nullable = false, length = 80)
    private String code;

    /** Nome leggibile reparto (es. Cardiologia). */
    @Column(nullable = false, length = 200)
    private String name;

    /** Capacità posti letto del reparto. */
    @Column
    @Builder.Default
    private Integer capacity = 0;

    /** Struttura di appartenenza del reparto. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;
}

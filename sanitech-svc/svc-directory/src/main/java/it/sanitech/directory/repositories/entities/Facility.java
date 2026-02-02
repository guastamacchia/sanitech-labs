package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Anagrafica Struttura (Facility).
 *
 * <p>
 * Rappresenta una struttura sanitaria all'interno del network Sanitech Labs.
 * Ogni struttura contiene uno o più reparti, e ogni reparto ha i propri medici.
 * Il codice univoco identifica stabilmente la struttura nelle integrazioni e nelle policy ABAC.
 * </p>
 */
@Entity
@Table(name = "facilities", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "code")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codice struttura (univoco, es. HOSP_CENTRAL). */
    @Column(nullable = false, length = 80)
    private String code;

    /** Nome leggibile struttura (es. Ospedale Centrale). */
    @Column(nullable = false, length = 200)
    private String name;

    /** Indirizzo della struttura. */
    @Column(length = 500)
    private String address;

    /** Numero di telefono della struttura. */
    @Column(length = 50)
    private String phone;
}

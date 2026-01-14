package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Anagrafica Specializzazione.
 *
 * <p>
 * Rappresenta la tassonomia delle specializzazioni mediche tramite un codice stabile e un nome
 * leggibile. Il codice è usato per filtrare e collegare i medici alle competenze dichiarate.
 * </p>
 */
@Entity
@Table(name = "specializations", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "code")
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codice specializzazione (univoco, es. CARDIOLOGY). */
    @Column(nullable = false, length = 80)
    private String code;

    /** Nome leggibile specializzazione (es. Cardiologia). */
    @Column(nullable = false, length = 200)
    private String name;
}

package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Anagrafica Reparto.
 *
 * <p>
 * Il campo {@code code} è l'identificativo stabile usato anche nelle policy ABAC (authority {@code DEPT_*}).
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
}

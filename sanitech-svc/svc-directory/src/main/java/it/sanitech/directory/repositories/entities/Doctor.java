package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entità Medico (Directory).
 *
 * <p>
 * Rappresenta i dati anagrafici del medico e la relazione con il reparto di appartenenza.
 * Il reparto appartiene a sua volta a una struttura (Facility) del network Sanitech Labs.
 * Gerarchia: Struttura -> Reparto -> Medico.
 * </p>
 */
@Entity
@Table(name = "doctors", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome del medico. */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /** Cognome del medico. */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /** Email univoca del medico. */
    @Column(nullable = false, length = 200)
    private String email;

    /** Telefono (opzionale). */
    @Column(length = 50)
    private String phone;

    /** Reparto di appartenenza del medico. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}

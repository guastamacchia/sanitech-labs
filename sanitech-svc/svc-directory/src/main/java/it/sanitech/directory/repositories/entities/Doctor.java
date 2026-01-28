package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entità Medico (Directory).
 *
 * <p>
 * Rappresenta i dati anagrafici del medico e le relazioni con reparti e specializzazioni.
 * Ogni medico è associato a un singolo reparto e a una singola specializzazione.
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

    /** Reparto di competenza/appartenenza del medico. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /** Specializzazione del medico. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;
}

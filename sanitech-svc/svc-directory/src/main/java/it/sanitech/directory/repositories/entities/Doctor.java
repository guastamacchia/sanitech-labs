package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entità Medico (Directory).
 *
 * <p>
 * Rappresenta i dati anagrafici del medico e le relazioni con reparti e specializzazioni.
 * Le associazioni many-to-many sono gestite tramite tabelle ponte dedicate e la mail è
 * vincolata a unicità per evitare duplicazioni anagrafiche.
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

    /** Reparti di competenza/appartenenza del medico. */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doctor_departments",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> departments = new HashSet<>();

    /** Specializzazioni del medico. */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doctor_specializations",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private Set<Specialization> specializations = new HashSet<>();
}

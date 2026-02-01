package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entità Paziente (Directory).
 *
 * <p>
 * Gestisce i dati anagrafici del paziente e le associazioni ai reparti di competenza.
 * Le informazioni su ricoveri/visite sono responsabilità di altri microservizi
 * (es. admissions/scheduling); qui si mantiene la sola appartenenza logica per filtri e ABAC.
 * </p>
 */
@Entity
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome del paziente. */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /** Cognome del paziente. */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /** Email univoca del paziente. */
    @Column(nullable = false, length = 200)
    private String email;

    /** Telefono (opzionale). */
    @Column(length = 50)
    private String phone;

    /** Reparti associati al paziente. */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "patient_departments",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> departments = new HashSet<>();
}

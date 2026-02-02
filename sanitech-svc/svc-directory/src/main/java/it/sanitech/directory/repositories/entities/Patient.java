package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.time.LocalDate;
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

    /** Codice fiscale del paziente. */
    @Column(name = "fiscal_code", length = 16)
    private String fiscalCode;

    /** Data di nascita. */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** Indirizzo di residenza. */
    @Column(length = 500)
    private String address;

    /** Stato dell'account (PENDING, ACTIVE, DISABLED). */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "user_status")
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    /** Data/ora di registrazione. */
    @Column(name = "registered_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant registeredAt = Instant.now();

    /** Data/ora di attivazione dell'account. */
    @Column(name = "activated_at")
    private Instant activatedAt;

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

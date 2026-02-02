package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;

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

    /** Specializzazione del medico (es. Cardiologia interventistica). */
    @Column(length = 200)
    private String specialization;

    /** Stato dell'account (PENDING, ACTIVE, DISABLED). */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "user_status")
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    /** Data/ora di creazione dell'account. */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /** Data/ora di attivazione dell'account. */
    @Column(name = "activated_at")
    private Instant activatedAt;

    /** Reparto di appartenenza del medico. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}

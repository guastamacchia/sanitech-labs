package it.sanitech.admissions.repositories.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

/**
 * Capacità posti letto per reparto.
 *
 * <p>
 * La capacità è gestita da un profilo amministratore.
 * L'occupazione viene calcolata contando i ricoveri attivi nel reparto.
 * </p>
 */
@Entity
@Table(name = "department_capacity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCapacity {

    /**
     * Codice reparto (PK).
     */
    @Id
    @Column(name = "dept_code", length = 80)
    private String deptCode;

    /**
     * Posti letto totali configurati per il reparto.
     */
    @Column(name = "total_beds", nullable = false)
    private int totalBeds;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;
}

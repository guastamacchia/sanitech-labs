package it.sanitech.admissions.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entità che rappresenta un ricovero (ammissione) di un paziente in un reparto.
 */
@Entity
@Table(name = "admissions",
        indexes = {
                @Index(name = "idx_admissions_dept_status", columnList = "department_code,status"),
                @Index(name = "idx_admissions_patient", columnList = "patient_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificativo del paziente (id logico dal Directory service).
     */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /**
     * Codice reparto (es. "HEART").
     */
    @Column(name = "department_code", nullable = false, length = 80)
    private String departmentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type", nullable = false, length = 32)
    private AdmissionType admissionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AdmissionStatus status;

    @Column(name = "admitted_at", nullable = false, columnDefinition = "timestamptz")
    private Instant admittedAt;

    @Column(name = "discharged_at", columnDefinition = "timestamptz")
    private Instant dischargedAt;

    @Column(length = 500)
    private String notes;

    /**
     * (Opzionale) id del medico che ha preso in carico il ricovero.
     */
    @Column(name = "attending_doctor_id")
    private Long attendingDoctorId;
}

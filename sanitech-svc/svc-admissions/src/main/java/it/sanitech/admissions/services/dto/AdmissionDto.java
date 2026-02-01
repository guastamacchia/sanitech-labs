package it.sanitech.admissions.services.dto;

import it.sanitech.admissions.repositories.entities.AdmissionStatus;
import it.sanitech.admissions.repositories.entities.AdmissionType;

import java.time.Instant;

/**
 * DTO di lettura per un ricovero.
 */
public record AdmissionDto(
        Long id,
        Long patientId,
        String departmentCode,
        AdmissionType admissionType,
        AdmissionStatus status,
        Instant admittedAt,
        Instant dischargedAt,
        String notes,
        Long attendingDoctorId
) { }

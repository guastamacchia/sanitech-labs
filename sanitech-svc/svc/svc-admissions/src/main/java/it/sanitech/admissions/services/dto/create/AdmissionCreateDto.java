package it.sanitech.admissions.services.dto.create;

import it.sanitech.admissions.repositories.entities.AdmissionType;
import jakarta.validation.constraints.*;

 /**
  * DTO per creare un nuovo ricovero.
  */
public record AdmissionCreateDto(

        @NotNull
        Long patientId,

        @NotBlank
        @Size(max = 80)
        String departmentCode,

        @NotNull
        AdmissionType admissionType,

        @Size(max = 500)
        String notes,

        Long attendingDoctorId
) { }

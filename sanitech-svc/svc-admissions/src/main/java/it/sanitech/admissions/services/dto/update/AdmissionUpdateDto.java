package it.sanitech.admissions.services.dto.update;

import jakarta.validation.constraints.Size;

/**
 * DTO per l'aggiornamento parziale di un ricovero.
 * Tutti i campi sono opzionali: solo quelli non nulli verranno aggiornati.
 */
public record AdmissionUpdateDto(
        Long attendingDoctorId,
        @Size(max = 500, message = "Le note non possono superare i 500 caratteri")
        String notes
) { }

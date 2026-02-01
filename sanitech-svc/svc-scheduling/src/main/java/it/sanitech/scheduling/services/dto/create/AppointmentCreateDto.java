package it.sanitech.scheduling.services.dto.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO di input per prenotare un appuntamento su uno slot.
 *
 * <p>
 * Il {@code patientId} può essere omesso se il chiamante è un utente PATIENT e
 * il relativo identificativo è fornito via claim JWT (es. {@code pid}).
 * </p>
 */
public record AppointmentCreateDto(
        @NotNull Long slotId,
        Long patientId,
        @Size(max = 500) String reason
) { }

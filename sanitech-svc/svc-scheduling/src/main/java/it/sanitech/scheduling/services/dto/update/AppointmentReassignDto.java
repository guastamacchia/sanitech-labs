package it.sanitech.scheduling.services.dto.update;

import jakarta.validation.constraints.NotNull;

public record AppointmentReassignDto(
        @NotNull Long newDoctorId,
        @NotNull Long newSlotId
) { }

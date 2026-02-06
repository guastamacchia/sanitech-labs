package it.sanitech.scheduling.services.dto.update;

import jakarta.validation.constraints.NotNull;

public record AppointmentRescheduleDto(
        @NotNull Long newSlotId
) { }

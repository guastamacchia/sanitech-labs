package it.sanitech.admissions.services.dto;

import java.time.Instant;

/**
 * DTO di lettura per capacità/occupazione di un reparto.
 */
public record CapacityDto(
        String departmentCode,
        int totalBeds,
        long occupiedBeds,
        long availableBeds,
        Instant updatedAt
) { }

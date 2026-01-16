package it.sanitech.scheduling.services.mapper;

import it.sanitech.scheduling.repositories.entities.Appointment;
import it.sanitech.scheduling.services.dto.AppointmentDto;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct per {@link Appointment}.
 */
@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    AppointmentDto toDto(Appointment entity);
}

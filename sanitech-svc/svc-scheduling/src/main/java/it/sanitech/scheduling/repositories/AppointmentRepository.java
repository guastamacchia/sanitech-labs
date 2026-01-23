package it.sanitech.scheduling.repositories;

import it.sanitech.scheduling.repositories.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Repository Spring Data JPA per l'entità {@link Appointment}.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByPatientIdOrderByStartAtDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByStartAtDesc(Long doctorId);
}

package it.sanitech.directory;

import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.repositories.entities.Facility;
import it.sanitech.directory.repositories.entities.Patient;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.FacilityDto;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.create.DepartmentCreateDto;
import it.sanitech.directory.services.dto.create.DoctorCreateDto;
import it.sanitech.directory.services.dto.create.FacilityCreateDto;
import it.sanitech.directory.services.dto.create.PatientCreateDto;
import it.sanitech.directory.services.dto.update.DepartmentUpdateDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
import it.sanitech.directory.services.dto.update.FacilityUpdateDto;
import it.sanitech.directory.services.dto.update.PatientUpdateDto;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
public final class TestDataFactory {

    // ===== FACILITY (STRUTTURA) =====

    public static Facility centralHospitalFacility() {
        return Facility.builder()
                .id(1L)
                .code("HOSP_CENTRAL")
                .name("Ospedale Centrale")
                .build();
    }

    public static FacilityDto facilityDto(long id, String code, String name) {
        return new FacilityDto(id, code, name);
    }

    public static FacilityCreateDto facilityCreateDto() {
        return new FacilityCreateDto("HOSP_NORD", "Ospedale Nord");
    }

    public static FacilityUpdateDto facilityUpdateDto() {
        return new FacilityUpdateDto("Ospedale Centrale Rinnovato");
    }

    // ===== DEPARTMENT (REPARTO) =====

    public static Department cardiologyDepartment() {
        Facility facility = centralHospitalFacility();
        return Department.builder()
                .id(10L)
                .code("CARD")
                .name("Cardiologia")
                .facility(facility)
                .build();
    }

    public static DepartmentDto departmentDto(long id, String code, String name, String facilityCode) {
        return new DepartmentDto(id, code, name, facilityCode);
    }

    public static DepartmentCreateDto departmentCreateDto() {
        return new DepartmentCreateDto("CARD", "Cardiologia", "HOSP_CENTRAL");
    }

    public static DepartmentUpdateDto departmentUpdateDto() {
        return new DepartmentUpdateDto("Cardiologia interventistica");
    }

    // ===== DOCTOR (MEDICO) =====

    public static Doctor doctorEntity(long id, String firstName, String lastName, String email) {
        return Doctor.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();
    }

    public static DoctorDto doctorDto(long id, String firstName, String lastName, String email) {
        return new DoctorDto(
                id,
                firstName,
                lastName,
                email,
                null,   // phone
                null,   // departmentCode
                null,   // departmentName
                null,   // facilityCode
                null    // facilityName
        );
    }

    public static DoctorCreateDto doctorCreateDto() {
        return new DoctorCreateDto(
                "Luca",
                "Bianchi",
                "luca.bianchi@email.it",
                null,
                "CARD"
        );
    }

    public static DoctorCreateDto doctorCreateDtoWithMixedCaseEmail() {
        return new DoctorCreateDto(
                "Luca",
                "Bianchi",
                "Luca.Bianchi@Email.it",
                null,
                "CARD"
        );
    }

    public static DoctorUpdateDto doctorUpdateDto() {
        return new DoctorUpdateDto(
                " Luca ",
                "Bianchi",
                "Nuova.Email@Email.it",
                null,
                "CARD"
        );
    }

    // ===== PATIENT (PAZIENTE) =====

    public static Patient patientEntity(long id, String firstName, String lastName, String email, String phone) {
        return Patient.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .build();
    }

    public static PatientDto patientDto(long id, String firstName, String lastName, String email, String phone) {
        return new PatientDto(
                id,
                firstName,
                lastName,
                email,
                phone,
                Set.of()
        );
    }

    public static PatientCreateDto patientCreateDto() {
        return new PatientCreateDto(
                "Mario",
                "Rossi",
                "nome.cognome@email.it",
                "+39 333 123 4567",
                Set.of("CARD")
        );
    }

    public static PatientCreateDto patientCreateDtoWithMixedCaseEmail() {
        return new PatientCreateDto(
                "Mario",
                "Rossi",
                "Nome.Cognome@Email.it",
                "+39 333 123 4567",
                Set.of("card")
        );
    }

    public static PatientUpdateDto patientUpdateDto() {
        return new PatientUpdateDto(
                "Maria",
                "Rossi",
                "maria.rossi@email.it",
                "+39 333 123 9999",
                null
        );
    }

    public static PatientUpdateDto patientUpdateDtoWithWhitespace() {
        return new PatientUpdateDto(
                " Maria ",
                "Rossi",
                "Nuova.Email@Email.it",
                " 333 444 555 ",
                null
        );
    }
}

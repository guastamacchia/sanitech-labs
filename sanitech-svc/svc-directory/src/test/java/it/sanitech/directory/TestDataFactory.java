package it.sanitech.directory;

import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.repositories.entities.Facility;
import it.sanitech.directory.repositories.entities.Patient;
import it.sanitech.directory.repositories.entities.UserStatus;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@NoArgsConstructor
public final class TestDataFactory {

    // ===== FACILITY (STRUTTURA) =====

    public static Facility centralHospitalFacility() {
        return Facility.builder()
                .id(1L)
                .code("HOSP_CENTRAL")
                .name("Ospedale Centrale")
                .address("Via Roma 1, Milano")
                .phone("+39 02 1234567")
                .build();
    }

    public static FacilityDto facilityDto(long id, String code, String name) {
        return new FacilityDto(id, code, name, null, null);
    }

    public static FacilityCreateDto facilityCreateDto() {
        return new FacilityCreateDto("HOSP_NORD", "Ospedale Nord", "Via Torino 10, Milano", "+39 02 9876543");
    }

    public static FacilityUpdateDto facilityUpdateDto() {
        return new FacilityUpdateDto("Ospedale Centrale Rinnovato", "Via Roma 1, Milano", "+39 02 1234567");
    }

    // ===== DEPARTMENT (REPARTO) =====

    public static Department cardiologyDepartment() {
        Facility facility = centralHospitalFacility();
        return Department.builder()
                .id(10L)
                .code("CARD")
                .name("Cardiologia")
                .capacity(50)
                .facility(facility)
                .build();
    }

    public static DepartmentDto departmentDto(long id, String code, String name, String facilityCode) {
        return new DepartmentDto(id, code, name, null, facilityCode, null, 0L);
    }

    public static DepartmentCreateDto departmentCreateDto() {
        return new DepartmentCreateDto("CARD", "Cardiologia", 50, "HOSP_CENTRAL");
    }

    public static DepartmentUpdateDto departmentUpdateDto() {
        return new DepartmentUpdateDto("Cardiologia interventistica", 60);
    }

    // ===== DOCTOR (MEDICO) =====

    public static Doctor doctorEntity(long id, String firstName, String lastName, String email) {
        return Doctor.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .status(UserStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public static DoctorDto doctorDto(long id, String firstName, String lastName, String email) {
        return new DoctorDto(
                id,
                firstName,
                lastName,
                email,
                null,   // phone
                null,   // specialization
                UserStatus.PENDING, // status
                null,   // createdAt
                null,   // activatedAt
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
                null,   // specialization
                "CARD"
        );
    }

    public static DoctorCreateDto doctorCreateDtoWithMixedCaseEmail() {
        return new DoctorCreateDto(
                "Luca",
                "Bianchi",
                "Luca.Bianchi@Email.it",
                null,
                null,   // specialization
                "CARD"
        );
    }

    public static DoctorUpdateDto doctorUpdateDto() {
        return new DoctorUpdateDto(
                " Luca ",
                "Bianchi",
                "Nuova.Email@Email.it",
                null,
                null,   // specialization
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
                .status(UserStatus.PENDING)
                .registeredAt(Instant.now())
                .build();
    }

    public static PatientDto patientDto(long id, String firstName, String lastName, String email, String phone) {
        return new PatientDto(
                id,
                firstName,
                lastName,
                email,
                phone,
                null,   // fiscalCode
                null,   // birthDate
                null,   // address
                UserStatus.PENDING, // status
                null,   // registeredAt
                null,   // activatedAt
                Set.of()
        );
    }

    public static PatientCreateDto patientCreateDto() {
        return new PatientCreateDto(
                "Mario",
                "Rossi",
                "nome.cognome@email.it",
                "+39 333 123 4567",
                "RSSMRA80A01F205X",  // fiscalCode
                LocalDate.of(1980, 1, 1), // birthDate
                "Via Milano 10, Roma", // address
                Set.of("CARD")
        );
    }

    public static PatientCreateDto patientCreateDtoWithMixedCaseEmail() {
        return new PatientCreateDto(
                "Mario",
                "Rossi",
                "Nome.Cognome@Email.it",
                "+39 333 123 4567",
                "RSSMRA80A01F205X",  // fiscalCode
                LocalDate.of(1980, 1, 1), // birthDate
                "Via Milano 10, Roma", // address
                Set.of("card")
        );
    }

    public static PatientUpdateDto patientUpdateDto() {
        return new PatientUpdateDto(
                "Maria",
                "Rossi",
                "maria.rossi@email.it",
                "+39 333 123 9999",
                null,   // fiscalCode
                null,   // birthDate
                null,   // address
                null
        );
    }

    public static PatientUpdateDto patientUpdateDtoWithWhitespace() {
        return new PatientUpdateDto(
                " Maria ",
                "Rossi",
                "Nuova.Email@Email.it",
                " 333 444 555 ",
                null,   // fiscalCode
                null,   // birthDate
                null,   // address
                null
        );
    }
}

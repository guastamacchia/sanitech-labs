package it.sanitech.admissions.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.admissions.repositories.AdmissionRepository;
import it.sanitech.admissions.repositories.DepartmentCapacityRepository;
import it.sanitech.admissions.repositories.entities.AdmissionStatus;
import it.sanitech.admissions.repositories.entities.DepartmentCapacity;
import it.sanitech.admissions.services.dto.CapacityDto;
import it.sanitech.commons.exception.ConflictException;
import it.sanitech.outbox.core.DomainEventPublisher;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class CapacityServiceTest {

    @Test
    void getReturnsCapacityDtoWithAvailability() {
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        CapacityService service = new CapacityService(capacityRepository, admissions, domainEvents);

        DepartmentCapacity capacity = new DepartmentCapacity("NEURO", 10, Instant.parse("2024-01-01T00:00:00Z"));
        when(capacityRepository.findById("NEURO")).thenReturn(Optional.of(capacity));
        when(admissions.countByDepartmentCodeIgnoreCaseAndStatus("NEURO", AdmissionStatus.ACTIVE))
                .thenReturn(3L);

        CapacityDto result = service.get("neuro");

        assertThat(result.departmentCode()).isEqualTo("NEURO");
        assertThat(result.occupiedBeds()).isEqualTo(3L);
        assertThat(result.availableBeds()).isEqualTo(7L);
    }

    @Test
    void getThrowsWhenCapacityMissing() {
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        CapacityService service = new CapacityService(capacityRepository, admissions, domainEvents);

        when(capacityRepository.findById("CARDIO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("cardio"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void upsertCreatesCapacityAndPublishesEvent() {
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        CapacityService service = new CapacityService(capacityRepository, admissions, domainEvents);

        when(capacityRepository.findById("ORTHO")).thenReturn(Optional.empty());
        when(capacityRepository.save(any(DepartmentCapacity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(admissions.countByDepartmentCodeIgnoreCaseAndStatus("ORTHO", AdmissionStatus.ACTIVE))
                .thenReturn(2L);

        CapacityDto result = service.upsert(" ortho ", 5);

        assertThat(result.departmentCode()).isEqualTo("ORTHO");
        assertThat(result.totalBeds()).isEqualTo(5);
        assertThat(result.availableBeds()).isEqualTo(3L);

        ArgumentCaptor<DepartmentCapacity> captor = ArgumentCaptor.forClass(DepartmentCapacity.class);
        verify(capacityRepository).save(captor.capture());
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();

        verify(domainEvents).publish(eq("DEPARTMENT_CAPACITY"), eq("ORTHO"), eq("CAPACITY_SET"), any(), eq("audits.events"));
    }

    @Test
    void upsertRejectsNegativeTotals() {
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        CapacityService service = new CapacityService(capacityRepository, admissions, domainEvents);

        assertThatThrownBy(() -> service.upsert("ORTHO", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

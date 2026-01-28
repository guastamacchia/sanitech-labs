import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';

type UpcomingVisit = {
  id: number;
  department: string;
  patient: string;
  date: string;
  time: string;
  modality: string;
  reason: string;
  status: 'CONFIRMED' | 'PENDING';
};

type SchedulingAppointment = {
  id: number;
  patientId: number;
  doctorId: number;
  slotId: number;
  reason: string;
  status: string;
};

type SchedulingSlot = {
  id: number;
  doctorId: number;
  date: string;
  time: string;
  status: string;
  modality: 'IN_PERSON' | 'REMOTE';
  notes?: string;
};

type DoctorItem = {
  id: number;
  firstName: string;
  lastName: string;
  speciality: string;
};

type PatientItem = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
};

type DepartmentItem = {
  code: string;
  name: string;
};

type PagedResponse<T> = {
  content?: T[];
};

@Component({
  selector: 'app-doctor-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './doctor-home.component.html'
})
export class DoctorHomeComponent {
  upcomingVisits: UpcomingVisit[] = [];

  pageSizeOptions = [5, 10];
  pageSize = 5;
  currentPage = 1;

  constructor(private api: ApiService) {
    this.loadUpcomingVisits();
  }

  get paginatedVisits(): UpcomingVisit[] {
    const confirmedVisits = this.upcomingVisits.filter((visit) => visit.status === 'CONFIRMED');
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    return confirmedVisits.slice(start, end);
  }

  get totalPages(): number {
    const confirmedCount = this.upcomingVisits.filter((visit) => visit.status === 'CONFIRMED').length;
    return Math.max(1, Math.ceil(confirmedCount / this.pageSize));
  }

  changePage(nextPage: number): void {
    this.currentPage = Math.min(Math.max(nextPage, 1), this.totalPages);
  }

  resetPagination(): void {
    this.currentPage = 1;
  }

  private loadUpcomingVisits(): void {
    forkJoin({
      appointments: this.api
        .request<SchedulingAppointment[] | PagedResponse<SchedulingAppointment>>('GET', '/api/appointments')
        .pipe(catchError(() => of([] as SchedulingAppointment[]))),
      slots: this.api
        .request<SchedulingSlot[] | PagedResponse<SchedulingSlot>>('GET', '/api/slots')
        .pipe(catchError(() => of([] as SchedulingSlot[]))),
      doctors: this.api
        .request<DoctorItem[] | PagedResponse<DoctorItem>>('GET', '/api/doctors')
        .pipe(catchError(() => of([] as DoctorItem[]))),
      patients: this.api
        .request<PatientItem[] | PagedResponse<PatientItem>>('GET', '/api/patients')
        .pipe(catchError(() => of([] as PatientItem[]))),
      departments: this.api.request<DepartmentItem[]>('GET', '/api/departments').pipe(catchError(() => of([] as DepartmentItem[])))
    }).subscribe(({ appointments, slots, doctors, patients, departments }) => {
      const appointmentList = this.normalizeList(appointments);
      const slotList = this.normalizeList(slots);
      const doctorList = this.normalizeList(doctors);
      const patientList = this.normalizeList(patients);
      const currentDoctorId = doctorList[0]?.id;

      this.upcomingVisits = appointmentList
        .filter((appointment) => appointment.status)
        .filter((appointment) => (currentDoctorId ? appointment.doctorId === currentDoctorId : true))
        .map((appointment) => {
          const slot = slotList.find((item) => item.id === appointment.slotId);
          const doctor = doctorList.find((item) => item.id === appointment.doctorId);
          const patient = patientList.find((item) => item.id === appointment.patientId);
          return {
            id: appointment.id,
            department: this.getDepartmentLabel(doctor?.speciality, departments),
            patient: patient ? `${patient.firstName} ${patient.lastName}` : `Paziente ${appointment.patientId}`,
            date: slot?.date ? this.formatDate(slot.date) : '-',
            time: slot?.time ?? '-',
            modality: this.getModalityLabel(slot?.modality),
            reason: appointment.reason || '-',
            status: appointment.status === 'CONFIRMED' ? 'CONFIRMED' : 'PENDING'
          };
        });

      this.resetPagination();
    });
  }

  private normalizeList<T>(data: T[] | PagedResponse<T> | null | undefined): T[] {
    if (!data) {
      return [];
    }
    if (Array.isArray(data)) {
      return data;
    }
    return data.content ?? [];
  }

  private getModalityLabel(modality?: SchedulingSlot['modality']): string {
    if (!modality) {
      return '-';
    }
    return modality === 'REMOTE' ? 'Da remoto' : 'In presenza';
  }

  private getDepartmentLabel(code: string | undefined, departments: DepartmentItem[]): string {
    if (!code) {
      return '-';
    }
    return departments.find((department) => department.code === code)?.name ?? code;
  }

  private formatDate(value: string): string {
    if (!value) {
      return '-';
    }
    const normalizedValue = value.trim();
    if (/^\d{2}\/\d{2}\/\d{4}$/.test(normalizedValue)) {
      return normalizedValue;
    }
    const isoMatch = normalizedValue.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (isoMatch) {
      return `${isoMatch[3]}/${isoMatch[2]}/${isoMatch[1]}`;
    }
    const parsed = new Date(normalizedValue);
    if (Number.isNaN(parsed.getTime())) {
      return normalizedValue;
    }
    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    return `${day}/${month}/${parsed.getFullYear()}`;
  }
}

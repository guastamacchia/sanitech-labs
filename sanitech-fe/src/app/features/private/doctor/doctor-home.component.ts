import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/auth/auth.service';

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
  departmentCode: string;
  mode: 'IN_PERSON' | 'TELEVISIT';
  startAt: string;
  endAt: string;
  status: string;
};

type DoctorItem = {
  id: number;
  firstName: string;
  lastName: string;
  departmentCode?: string;
  facilityCode?: string;
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

  constructor(
    private api: ApiService,
    private auth: AuthService
  ) {
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
    const doctorId = this.getDoctorIdClaim();
    if (!doctorId) {
      this.upcomingVisits = [];
      this.resetPagination();
      return;
    }
    forkJoin({
      appointments: this.api
        .request<SchedulingAppointment[] | PagedResponse<SchedulingAppointment>>('GET', `/api/appointments?doctorId=${doctorId}`)
        .pipe(catchError(() => of([] as SchedulingAppointment[]))),
      doctors: this.api
        .request<DoctorItem[] | PagedResponse<DoctorItem>>('GET', '/api/doctors')
        .pipe(catchError(() => of([] as DoctorItem[]))),
      patients: this.api
        .request<PatientItem[] | PagedResponse<PatientItem>>('GET', '/api/patients')
        .pipe(catchError(() => of([] as PatientItem[]))),
      departments: this.api.request<DepartmentItem[]>('GET', '/api/departments').pipe(catchError(() => of([] as DepartmentItem[])))
    }).subscribe(({ appointments, doctors, patients, departments }) => {
      const appointmentList = this.normalizeList(appointments);
      const doctorList = this.normalizeList(doctors);
      const patientList = this.normalizeList(patients);

      this.upcomingVisits = appointmentList
        .filter((appointment) => appointment.status === 'BOOKED')
        .map((appointment) => {
          const patient = patientList.find((item) => item.id === appointment.patientId);
          const appointmentDate = this.getDateLabel(appointment.startAt);
          const appointmentTime = this.getTimeLabel(appointment.startAt);
          return {
            id: appointment.id,
            department: this.getDepartmentLabel(appointment.departmentCode, departments),
            patient: patient ? `${patient.firstName} ${patient.lastName}` : `Paziente ${appointment.patientId}`,
            date: appointmentDate,
            time: appointmentTime,
            modality: this.getModalityLabel(appointment.mode),
            reason: '-',
            status: 'CONFIRMED'
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

  private getModalityLabel(modality?: SchedulingAppointment['mode']): string {
    if (!modality) {
      return '-';
    }
    return modality === 'TELEVISIT' ? 'Da remoto' : 'In presenza';
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

  private getDateLabel(value: string): string {
    if (!value) {
      return '-';
    }
    return this.formatDate(value);
  }

  private getTimeLabel(value: string): string {
    if (!value) {
      return '-';
    }
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return '-';
    }
    return `${String(parsed.getHours()).padStart(2, '0')}:${String(parsed.getMinutes()).padStart(2, '0')}`;
  }

  private getDoctorIdClaim(): number | null {
    const claim = this.auth.getAccessTokenClaim('did');
    if (typeof claim === 'number') {
      return claim;
    }
    if (typeof claim === 'string') {
      const parsed = Number(claim);
      return Number.isNaN(parsed) ? null : parsed;
    }
    return null;
  }
}

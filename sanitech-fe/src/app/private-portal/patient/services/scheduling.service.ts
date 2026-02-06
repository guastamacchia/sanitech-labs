import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, forkJoin, map, catchError } from 'rxjs';
import { environment } from '@env/environment';

// ============ TIPI ============

export type VisitMode = 'IN_PERSON' | 'TELEVISIT';
export type SlotStatus = 'AVAILABLE' | 'BOOKED' | 'CANCELLED';
export type AppointmentStatus = 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// DTO dal backend svc-directory
export interface DoctorDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  departmentCode: string;
  facilityCode: string;
}

export interface DepartmentDto {
  id: number;
  code: string;
  name: string;
  facilityCode: string;
}

export interface FacilityDto {
  id: number;
  code: string;
  name: string;
}

// DTO dal backend svc-scheduling
export interface SlotDto {
  id: number;
  doctorId: number;
  departmentCode: string;
  mode: VisitMode;
  startAt: string; // ISO instant
  endAt: string;
  status: SlotStatus;
}

export interface AppointmentDto {
  id: number;
  slotId: number;
  patientId: number;
  doctorId: number;
  departmentCode: string;
  mode: VisitMode;
  startAt: string;
  endAt: string;
  status: AppointmentStatus;
  reason?: string;
}

export interface AppointmentCreateDto {
  slotId: number;
  patientId?: number;
  reason?: string;
}

// ============ Extended types per il frontend ============

export interface DoctorWithDetails extends DoctorDto {
  departmentName?: string;
  facilityName?: string;
}

export interface AppointmentWithDetails extends AppointmentDto {
  doctorName?: string;
  departmentName?: string;
  facilityName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class SchedulingService {

  private readonly gatewayUrl = environment.gatewayUrl;

  constructor(private http: HttpClient) {}

  // ============ MEDICI ============

  getDoctors(params?: {
    q?: string;
    department?: string;
    page?: number;
    size?: number;
  }): Observable<PagedResponse<DoctorDto>> {
    let httpParams = new HttpParams();
    if (params?.q) httpParams = httpParams.set('q', params.q);
    if (params?.department) httpParams = httpParams.set('department', params.department);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<PagedResponse<DoctorDto>>(
      `${this.gatewayUrl}/api/doctors`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  getDoctor(id: number): Observable<DoctorDto | null> {
    return this.http.get<DoctorDto>(`${this.gatewayUrl}/api/doctors/${id}`).pipe(
      catchError(() => of(null))
    );
  }

  // ============ REPARTI ============

  getDepartments(): Observable<DepartmentDto[]> {
    return this.http.get<DepartmentDto[]>(`${this.gatewayUrl}/api/departments`).pipe(
      catchError(() => of([]))
    );
  }

  // ============ STRUTTURE ============

  getFacilities(): Observable<FacilityDto[]> {
    return this.http.get<FacilityDto[]>(`${this.gatewayUrl}/api/facilities`).pipe(
      catchError(() => of([]))
    );
  }

  // ============ SLOTS ============

  getAvailableSlots(params?: {
    doctorId?: number;
    department?: string;
    mode?: VisitMode;
    from?: string;
    to?: string;
    page?: number;
    size?: number;
  }): Observable<PagedResponse<SlotDto>> {
    let httpParams = new HttpParams();
    if (params?.doctorId) httpParams = httpParams.set('doctorId', params.doctorId.toString());
    if (params?.department) httpParams = httpParams.set('department', params.department);
    if (params?.mode) httpParams = httpParams.set('mode', params.mode);
    if (params?.from) httpParams = httpParams.set('from', params.from);
    if (params?.to) httpParams = httpParams.set('to', params.to);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<PagedResponse<SlotDto>>(
      `${this.gatewayUrl}/api/slots`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  // ============ APPUNTAMENTI ============

  getAppointments(params?: {
    patientId?: number;
    doctorId?: number;
    department?: string;
    page?: number;
    size?: number;
  }): Observable<PagedResponse<AppointmentDto>> {
    let httpParams = new HttpParams();
    if (params?.patientId) httpParams = httpParams.set('patientId', params.patientId.toString());
    if (params?.doctorId) httpParams = httpParams.set('doctorId', params.doctorId.toString());
    if (params?.department) httpParams = httpParams.set('department', params.department);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<PagedResponse<AppointmentDto>>(
      `${this.gatewayUrl}/api/appointments`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  bookAppointment(dto: AppointmentCreateDto): Observable<AppointmentDto> {
    return this.http.post<AppointmentDto>(`${this.gatewayUrl}/api/appointments`, dto);
  }

  cancelAppointment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.gatewayUrl}/api/appointments/${id}`);
  }

  // ============ DATI AGGREGATI ============

  /**
   * Carica tutti i dati necessari per la pagina di prenotazione:
   * - Medici
   * - Reparti (che fungono da specializzazioni)
   * - Strutture
   */
  loadBookingData(): Observable<{
    doctors: DoctorDto[];
    departments: DepartmentDto[];
    facilities: FacilityDto[];
  }> {
    return forkJoin({
      doctors: this.getDoctors({ size: 100 }).pipe(map(r => r.content)),
      departments: this.getDepartments(),
      facilities: this.getFacilities()
    });
  }

  /**
   * Arricchisce i dati del medico con i nomi di reparto e struttura
   */
  enrichDoctorData(
    doctors: DoctorDto[],
    departments: DepartmentDto[],
    facilities: FacilityDto[]
  ): DoctorWithDetails[] {
    const deptMap = new Map(departments.map(d => [d.code, d]));
    const facMap = new Map(facilities.map(f => [f.code, f]));

    return doctors.map(doc => ({
      ...doc,
      departmentName: deptMap.get(doc.departmentCode)?.name || doc.departmentCode,
      facilityName: facMap.get(doc.facilityCode)?.name || doc.facilityCode
    }));
  }

  /**
   * Arricchisce i dati degli appuntamenti con info medico/reparto/struttura
   */
  enrichAppointmentData(
    appointments: AppointmentDto[],
    doctors: DoctorDto[],
    departments: DepartmentDto[],
    facilities: FacilityDto[]
  ): AppointmentWithDetails[] {
    const docMap = new Map(doctors.map(d => [d.id, d]));
    const deptMap = new Map(departments.map(d => [d.code, d]));
    const facMap = new Map(facilities.map(f => [f.code, f]));

    return appointments.map(appt => {
      const doctor = docMap.get(appt.doctorId);
      const dept = deptMap.get(appt.departmentCode);
      return {
        ...appt,
        doctorName: doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : `Medico #${appt.doctorId}`,
        departmentName: dept?.name || appt.departmentCode,
        facilityName: doctor ? (facMap.get(doctor.facilityCode)?.name || doctor.facilityCode) : undefined
      };
    });
  }
}

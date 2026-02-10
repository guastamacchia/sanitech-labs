import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { forkJoin, Subject, takeUntil } from 'rxjs';
import {
  SchedulingService,
  DepartmentDto,
  FacilityDto,
  SlotDto,
  VisitMode,
  AppointmentStatus,
  DoctorWithDetails,
  AppointmentWithDetails
} from '../../../services/scheduling.service';
import { TimeSlot, DisplayAppointment } from './dtos/booking.dto';

@Component({
  selector: 'app-appointment-booking',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './appointment-booking.component.html'
})
export class AppointmentBookingComponent implements OnInit, OnDestroy {

  private readonly destroy$ = new Subject<void>();

  // Dati dal backend
  doctors: DoctorWithDetails[] = [];
  departments: DepartmentDto[] = [];
  facilities: FacilityDto[] = [];
  availableSlots: TimeSlot[] = [];
  appointments: DisplayAppointment[] = [];

  // Stato UI
  isLoading = false;
  dataLoaded = false;
  activeTab: 'book' | 'appointments' = 'book';
  showBookingModal = false;
  showCancelModal = false;
  showConfirmationModal = false;
  successMessage = '';
  errorMessage = '';

  // Filtri per ricerca medici
  selectedDepartment = '';
  selectedDoctor: DoctorWithDetails | null = null;
  selectedDate = '';
  selectedMode: VisitMode | '' = '';

  // Filtri per appuntamenti
  appointmentStatusFilter: 'ALL' | AppointmentStatus = 'ALL';

  // Slot selezionato per prenotazione
  selectedSlot: TimeSlot | null = null;
  bookingReason = '';

  // Appuntamento selezionato per cancellazione
  selectedAppointmentForCancel: DisplayAppointment | null = null;

  // Paginazione
  pageSize = 5;
  currentPage = 1;

  // Settimana visualizzata nel calendario
  currentWeekStart: Date = new Date();

  constructor(
    private schedulingService: SchedulingService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showBookingModal) {
      this.closeBookingModal();
    } else if (this.showCancelModal) {
      this.closeCancelModal();
    } else if (this.showConfirmationModal) {
      this.closeConfirmationModal();
    }
  }

  ngOnInit(): void {
    this.initializeWeek();
    const tab = this.route.snapshot.queryParamMap.get('tab');
    if (tab === 'appointments') {
      this.activeTab = 'appointments';
    }
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeWeek(): void {
    const today = new Date();
    const dayOfWeek = today.getDay();
    const diff = today.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1);
    this.currentWeekStart = new Date(today.setDate(diff));
  }

  private loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      bookingData: this.schedulingService.loadBookingData(),
      appointments: this.schedulingService.getAppointments({ size: 100 })
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ bookingData, appointments }) => {
        this.departments = bookingData.departments;
        this.facilities = bookingData.facilities;
        this.doctors = this.schedulingService.enrichDoctorData(
          bookingData.doctors,
          bookingData.departments,
          bookingData.facilities
        );

        // Trasforma gli appuntamenti
        const enrichedAppts = this.schedulingService.enrichAppointmentData(
          appointments.content,
          bookingData.doctors,
          bookingData.departments,
          bookingData.facilities
        );
        this.appointments = this.transformAppointments(enrichedAppts);

        this.dataLoaded = true;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Errore nel caricamento dei dati:', err);
        this.errorMessage = 'Errore nel caricamento dei dati. Riprova più tardi.';
        this.dataLoaded = true;
        this.isLoading = false;
      }
    });
  }

  private transformAppointments(appts: AppointmentWithDetails[]): DisplayAppointment[] {
    return appts.map(a => {
      const startDate = new Date(a.startAt);
      const endDate = new Date(a.endAt);
      return {
        id: a.id,
        slotId: a.slotId,
        patientId: a.patientId,
        doctorId: a.doctorId,
        doctorName: a.doctorName || `Medico #${a.doctorId}`,
        departmentName: a.departmentName || a.departmentCode,
        facilityName: a.facilityName || '',
        date: this.toLocalDateStr(startDate),
        startTime: startDate.toTimeString().substring(0, 5),
        endTime: endDate.toTimeString().substring(0, 5),
        mode: a.mode,
        status: a.status,
        reason: a.reason
      };
    });
  }

  private loadSlotsForDoctor(doctorId: number): void {
    const from = new Date();
    const to = new Date();
    to.setDate(to.getDate() + 30);

    this.schedulingService.getAvailableSlots({
      doctorId,
      from: from.toISOString(),
      to: to.toISOString(),
      size: 200
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        this.availableSlots = response.content.map(s => this.transformSlot(s));
      },
      error: (err) => {
        console.error('Errore nel caricamento degli slot:', err);
        this.availableSlots = [];
      }
    });
  }

  private transformSlot(slot: SlotDto): TimeSlot {
    const startDate = new Date(slot.startAt);
    const endDate = new Date(slot.endAt);
    return {
      id: slot.id,
      doctorId: slot.doctorId,
      date: this.toLocalDateStr(startDate),
      startTime: startDate.toTimeString().substring(0, 5),
      endTime: endDate.toTimeString().substring(0, 5),
      mode: slot.mode,
      status: slot.status === 'AVAILABLE' ? 'AVAILABLE' : 'BOOKED'
    };
  }

  // Proprietà calcolate
  get filteredDoctors(): DoctorWithDetails[] {
    let result = this.doctors;
    if (this.selectedDepartment) {
      result = result.filter(d => d.departmentCode === this.selectedDepartment);
    }
    if (this.selectedMode && this.availableSlots.length > 0) {
      const doctorIdsWithMode = new Set(
        this.availableSlots
          .filter(s => s.mode === this.selectedMode && s.status === 'AVAILABLE')
          .map(s => s.doctorId)
      );
      result = result.filter(d => doctorIdsWithMode.has(d.id));
    }
    return result;
  }

  get filteredSlots(): TimeSlot[] {
    if (!this.selectedDoctor) return [];
    let slots = this.availableSlots.filter(s =>
      s.doctorId === this.selectedDoctor!.id &&
      s.status === 'AVAILABLE'
    );

    if (this.selectedDate) {
      slots = slots.filter(s => s.date === this.selectedDate);
    }

    if (this.selectedMode) {
      slots = slots.filter(s => s.mode === this.selectedMode);
    }

    return slots.sort((a, b) => {
      const dateCompare = a.date.localeCompare(b.date);
      if (dateCompare !== 0) return dateCompare;
      return a.startTime.localeCompare(b.startTime);
    });
  }

  get weekDays(): { date: Date; dateStr: string; dayName: string; isToday: boolean }[] {
    const days = [];
    for (let i = 0; i < 7; i++) {
      const date = new Date(this.currentWeekStart);
      date.setDate(date.getDate() + i);
      const today = new Date();
      days.push({
        date,
        dateStr: this.toLocalDateStr(date),
        dayName: date.toLocaleDateString('it-IT', { weekday: 'short' }),
        isToday: date.toDateString() === today.toDateString()
      });
    }
    return days;
  }

  getSlotsForDay(dateStr: string): TimeSlot[] {
    if (!this.selectedDoctor) return [];
    return this.availableSlots.filter(s =>
      s.doctorId === this.selectedDoctor!.id &&
      s.date === dateStr &&
      s.status === 'AVAILABLE' &&
      (!this.selectedMode || s.mode === this.selectedMode)
    ).sort((a, b) => a.startTime.localeCompare(b.startTime));
  }

  get filteredAppointments(): DisplayAppointment[] {
    let result = this.appointments;
    if (this.appointmentStatusFilter !== 'ALL') {
      result = result.filter(a => a.status === this.appointmentStatusFilter);
    }
    return [...result].sort((a, b) => {
      const dateCompare = b.date.localeCompare(a.date);
      if (dateCompare !== 0) return dateCompare;
      return b.startTime.localeCompare(a.startTime);
    });
  }

  get paginatedAppointments(): DisplayAppointment[] {
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = this.totalPages;
    }
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredAppointments.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredAppointments.length / this.pageSize);
  }

  get upcomingAppointmentsCount(): number {
    const today = this.toLocalDateStr(new Date());
    return this.appointments.filter(a =>
      (a.status === 'BOOKED' || a.status === 'CONFIRMED') &&
      a.date >= today
    ).length;
  }

  get completedAppointmentsCount(): number {
    return this.appointments.filter(a => a.status === 'COMPLETED').length;
  }

  get cancelledAppointmentsCount(): number {
    return this.appointments.filter(a => a.status === 'CANCELLED').length;
  }

  get nextAppointment(): DisplayAppointment | null {
    const today = this.toLocalDateStr(new Date());
    const upcoming = this.appointments
      .filter(a => (a.status === 'BOOKED' || a.status === 'CONFIRMED') && a.date >= today)
      .sort((a, b) => {
        const dateCompare = a.date.localeCompare(b.date);
        if (dateCompare !== 0) return dateCompare;
        return a.startTime.localeCompare(b.startTime);
      });
    return upcoming[0] || null;
  }

  // Formattazione
  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('it-IT', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  formatShortDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  getModeLabel(mode: VisitMode): string {
    return mode === 'IN_PERSON' ? 'In presenza' : 'Televisita';
  }

  getModeIcon(mode: VisitMode): string {
    return mode === 'IN_PERSON' ? 'bi-hospital' : 'bi-camera-video';
  }

  getStatusBadgeClass(status: AppointmentStatus): string {
    switch (status) {
      case 'BOOKED': return 'bg-info';
      case 'CONFIRMED': return 'bg-success';
      case 'COMPLETED': return 'bg-secondary';
      case 'CANCELLED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }

  getStatusLabel(status: AppointmentStatus): string {
    switch (status) {
      case 'BOOKED': return 'Prenotato';
      case 'CONFIRMED': return 'Confermato';
      case 'COMPLETED': return 'Completato';
      case 'CANCELLED': return 'Annullato';
      default: return status;
    }
  }

  // Navigazione
  get canGoToPreviousWeek(): boolean {
    const prevWeekStart = new Date(this.currentWeekStart);
    prevWeekStart.setDate(prevWeekStart.getDate() - 7);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const weekEnd = new Date(prevWeekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    return weekEnd >= today;
  }

  previousWeek(): void {
    if (!this.canGoToPreviousWeek) return;
    const newStart = new Date(this.currentWeekStart);
    newStart.setDate(newStart.getDate() - 7);
    this.currentWeekStart = newStart;
  }

  nextWeek(): void {
    const newStart = new Date(this.currentWeekStart);
    newStart.setDate(newStart.getDate() + 7);
    this.currentWeekStart = newStart;
  }

  getWeekRangeLabel(): string {
    const start = new Date(this.currentWeekStart);
    const end = new Date(this.currentWeekStart);
    end.setDate(end.getDate() + 6);
    const startStr = start.toLocaleDateString('it-IT', { day: 'numeric', month: 'short' });
    const endStr = end.toLocaleDateString('it-IT', { day: 'numeric', month: 'short', year: 'numeric' });
    return `${startStr} - ${endStr}`;
  }

  // Selezione medico
  selectDoctor(doctor: DoctorWithDetails): void {
    this.selectedDoctor = doctor;
    this.selectedSlot = null;
    this.initializeWeek();
    this.loadSlotsForDoctor(doctor.id);
  }

  clearDoctorSelection(): void {
    this.selectedDoctor = null;
    this.selectedSlot = null;
    this.selectedDate = '';
  }

  // Selezione slot e prenotazione
  selectSlot(slot: TimeSlot): void {
    this.selectedSlot = slot;
    this.showBookingModal = true;
    this.bookingReason = '';
    this.errorMessage = '';
  }

  closeBookingModal(): void {
    this.showBookingModal = false;
    this.selectedSlot = null;
    this.bookingReason = '';
  }

  confirmBooking(): void {
    if (!this.selectedSlot || !this.selectedDoctor || !this.bookingReason.trim()) {
      this.errorMessage = 'Inserisci il motivo della visita.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.schedulingService.bookAppointment({
      slotId: this.selectedSlot.id,
      reason: this.bookingReason.trim()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (result) => {
        // Aggiungi l'appuntamento alla lista
        const newAppt: DisplayAppointment = {
          id: result.id,
          slotId: result.slotId,
          patientId: result.patientId,
          doctorId: result.doctorId,
          doctorName: `Dr. ${this.selectedDoctor!.firstName} ${this.selectedDoctor!.lastName}`,
          departmentName: this.selectedDoctor!.departmentName || this.selectedDoctor!.departmentCode,
          facilityName: this.selectedDoctor!.facilityName || this.selectedDoctor!.facilityCode,
          date: this.selectedSlot!.date,
          startTime: this.selectedSlot!.startTime,
          endTime: this.selectedSlot!.endTime,
          mode: result.mode,
          status: result.status,
          reason: result.reason
        };

        // Aggiorna lo slot come occupato
        const slot = this.availableSlots.find(s => s.id === this.selectedSlot!.id);
        if (slot) slot.status = 'BOOKED';

        this.appointments = [newAppt, ...this.appointments];
        this.isLoading = false;
        this.closeBookingModal();
        this.showConfirmationModal = true;
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 409) {
          this.errorMessage = 'Lo slot non è più disponibile. Seleziona un altro orario.';
          if (this.selectedDoctor) {
            this.loadSlotsForDoctor(this.selectedDoctor.id);
          }
        } else if (err.status === 400) {
          this.errorMessage = 'Dati non validi. Controlla il motivo della visita (max 500 caratteri).';
        } else {
          this.errorMessage = 'Errore durante la prenotazione. Riprova più tardi.';
        }
      }
    });
  }

  closeConfirmationModal(): void {
    this.showConfirmationModal = false;
    this.successMessage = 'Prenotazione effettuata con successo! Riceverai una email di conferma.';
    setTimeout(() => this.successMessage = '', 5000);
  }

  // Cancellazione
  openCancelModal(appointment: DisplayAppointment): void {
    this.selectedAppointmentForCancel = appointment;
    this.showCancelModal = true;
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.selectedAppointmentForCancel = null;
  }

  confirmCancellation(): void {
    if (!this.selectedAppointmentForCancel) return;

    this.isLoading = true;
    const appointmentId = this.selectedAppointmentForCancel.id;

    this.schedulingService.cancelAppointment(appointmentId).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.updateAppointmentAsCancelled(appointmentId);
      },
      error: (err) => {
        console.error('Errore durante la cancellazione:', err);
        this.isLoading = false;
        this.closeCancelModal();
        this.errorMessage = 'Errore durante la cancellazione. Riprova più tardi.';
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  private updateAppointmentAsCancelled(appointmentId: number): void {
    const appointment = this.appointments.find(a => a.id === appointmentId);
    if (appointment) {
      appointment.status = 'CANCELLED';
      const slot = this.availableSlots.find(s => s.id === appointment.slotId);
      if (slot) slot.status = 'AVAILABLE';
    }
    this.appointments = [...this.appointments];
    this.isLoading = false;
    this.closeCancelModal();
    this.successMessage = 'Appuntamento annullato con successo.';
    setTimeout(() => this.successMessage = '', 5000);
  }

  canCancel(appointment: DisplayAppointment): boolean {
    if (appointment.status !== 'BOOKED' && appointment.status !== 'CONFIRMED') {
      return false;
    }
    const appointmentDate = new Date(`${appointment.date}T${appointment.startTime}`);
    const now = new Date();
    const hoursUntilAppointment = (appointmentDate.getTime() - now.getTime()) / (1000 * 60 * 60);
    return hoursUntilAppointment >= 24;
  }

  getCancelDisabledReason(appointment: DisplayAppointment): string {
    if (appointment.status !== 'BOOKED' && appointment.status !== 'CONFIRMED') {
      return '';
    }
    const appointmentDate = new Date(`${appointment.date}T${appointment.startTime}`);
    const now = new Date();
    const hoursUntilAppointment = (appointmentDate.getTime() - now.getTime()) / (1000 * 60 * 60);
    if (hoursUntilAppointment < 24) {
      return 'Non è possibile annullare entro 24 ore dall\'appuntamento';
    }
    return '';
  }

  // Aggiornamento
  refresh(): void {
    this.loadData();
  }

  isWeekend(date: Date): boolean {
    return date.getDay() === 0 || date.getDay() === 6;
  }

  isPastDate(dateStr: string): boolean {
    const today = this.toLocalDateStr(new Date());
    return dateStr < today;
  }

  switchTab(tab: 'book' | 'appointments'): void {
    this.activeTab = tab;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: tab === 'appointments' ? { tab: 'appointments' } : {},
      queryParamsHandling: tab === 'appointments' ? 'merge' : '',
      replaceUrl: true
    });
  }

  private toLocalDateStr(date: Date): string {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}

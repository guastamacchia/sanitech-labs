import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  DoctorApiService,
  SlotDto,
  AppointmentDto,
  PatientDto,
  VisitMode,
  SlotStatus as ApiSlotStatus
} from '../../../services/doctor-api.service';
import { SlotStatus, SlotModality, TimeSlot, SlotCreationForm } from './dtos/agenda.dto';

@Component({
  selector: 'app-doctor-agenda',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-agenda.component.html'
})
export class DoctorAgendaComponent implements OnInit {
  // Slots
  slots: TimeSlot[] = [];

  // Cache pazienti
  private patientsCache = new Map<number, PatientDto>();

  // Vista corrente
  currentWeekStart: Date = new Date();
  weekDays: Date[] = [];

  // Filtro paziente da queryParam
  patientIdFilter = 0;

  constructor(private doctorApi: DoctorApiService, private route: ActivatedRoute) {}

  // Form creazione slot
  slotForm: SlotCreationForm = {
    date: '',
    startTime: '08:00',
    endTime: '18:00',
    duration: 30,
    modality: 'BOTH',
    repeatWeekly: false,
    repeatWeeks: 4
  };

  // Stato UI
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showCreateSlotModal = false;
  showSlotDetailModal = false;
  selectedSlot: TimeSlot | null = null;
  selectedDate: string = '';

  // Statistiche
  get totalSlots(): number {
    return this.slots.filter(s => s.status === 'AVAILABLE' || s.status === 'BOOKED').length;
  }

  get availableSlots(): number {
    return this.slots.filter(s => s.status === 'AVAILABLE').length;
  }

  get bookedSlots(): number {
    return this.slots.filter(s => s.status === 'BOOKED').length;
  }

  get blockedSlots(): number {
    return this.slots.filter(s => s.status === 'BLOCKED').length;
  }

  get upcomingAppointments(): TimeSlot[] {
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    return this.slots
      .filter(s => s.status === 'BOOKED' && new Date(s.date) >= now)
      .sort((a, b) => {
        const dateCompare = a.date.localeCompare(b.date);
        return dateCompare !== 0 ? dateCompare : a.time.localeCompare(b.time);
      });
  }

  // Vista: 'calendar' o 'list'
  viewMode: 'calendar' | 'list' = 'calendar';

  ngOnInit(): void {
    const patientIdParam = this.route.snapshot.queryParamMap.get('patientId');
    if (patientIdParam) {
      this.patientIdFilter = +patientIdParam;
      this.viewMode = 'list';
    }
    this.initializeWeek();
    this.loadSlots();
  }

  get filteredUpcomingAppointments(): TimeSlot[] {
    if (this.patientIdFilter > 0) {
      return this.upcomingAppointments.filter(s => s.patientId === this.patientIdFilter);
    }
    return this.upcomingAppointments;
  }

  initializeWeek(): void {
    const today = new Date();
    const dayOfWeek = today.getDay();
    const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
    this.currentWeekStart = new Date(today);
    this.currentWeekStart.setDate(today.getDate() + diff);
    this.currentWeekStart.setHours(0, 0, 0, 0);
    this.generateWeekDays();
  }

  generateWeekDays(): void {
    this.weekDays = [];
    for (let i = 0; i < 7; i++) {
      const day = new Date(this.currentWeekStart);
      day.setDate(this.currentWeekStart.getDate() + i);
      this.weekDays.push(day);
    }
  }

  previousWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
    this.generateWeekDays();
  }

  nextWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.generateWeekDays();
  }

  goToToday(): void {
    this.initializeWeek();
  }

  loadSlots(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const doctorId = this.doctorApi.getDoctorId();
    if (!doctorId) {
      this.errorMessage = 'ID medico non disponibile.';
      this.isLoading = false;
      return;
    }

    // Calcola range settimana (da lunedì a domenica)
    const weekStart = new Date(this.currentWeekStart);
    weekStart.setHours(0, 0, 0, 0);
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekEnd.getDate() + 7);

    // Carica slot e appuntamenti in parallelo
    forkJoin({
      slots: this.doctorApi.searchSlots({
        doctorId,
        from: weekStart.toISOString(),
        to: weekEnd.toISOString(),
        size: 200
      }).pipe(catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 0, number: 0 }))),
      appointments: this.doctorApi.searchAppointments({
        doctorId,
        size: 200
      }).pipe(catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 0, number: 0 })))
    }).subscribe({
      next: ({ slots, appointments }) => {
        // Crea mappa appuntamenti per slotId
        const appointmentsBySlot = new Map<number, AppointmentDto>();
        (appointments.content || []).forEach(apt => {
          if (apt.status === 'BOOKED') {
            appointmentsBySlot.set(apt.slotId, apt);
          }
        });

        // Raccogli patientIds unici per fetch nomi
        const patientIds = new Set<number>();
        appointmentsBySlot.forEach(apt => patientIds.add(apt.patientId));

        // Carica pazienti se necessario
        if (patientIds.size > 0) {
          const patientFetches = Array.from(patientIds)
            .filter(id => !this.patientsCache.has(id))
            .map(id => this.doctorApi.getPatient(id).pipe(
              map(p => ({ id, patient: p })),
              catchError(() => of({ id, patient: null as PatientDto | null }))
            ));

          if (patientFetches.length > 0) {
            forkJoin(patientFetches).subscribe(results => {
              results.forEach(r => {
                if (r.patient) this.patientsCache.set(r.id, r.patient);
              });
              this.mapSlotsToTimeSlots(slots.content || [], appointmentsBySlot);
              this.isLoading = false;
            });
          } else {
            this.mapSlotsToTimeSlots(slots.content || [], appointmentsBySlot);
            this.isLoading = false;
          }
        } else {
          this.mapSlotsToTimeSlots(slots.content || [], appointmentsBySlot);
          this.isLoading = false;
        }
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento degli slot.';
        this.isLoading = false;
      }
    });
  }

  private mapSlotsToTimeSlots(apiSlots: SlotDto[], appointmentsBySlot: Map<number, AppointmentDto>): void {
    this.slots = apiSlots.map(slot => {
      const appointment = appointmentsBySlot.get(slot.id);
      const patient = appointment ? this.patientsCache.get(appointment.patientId) : undefined;

      // Calcola durata in minuti
      const start = new Date(slot.startAt);
      const end = new Date(slot.endAt);
      const durationMinutes = Math.round((end.getTime() - start.getTime()) / 60000);

      // Mappa status
      let status: SlotStatus = 'AVAILABLE';
      if (slot.status === 'BLOCKED') {
        status = 'BLOCKED';
      } else if (appointment) {
        status = 'BOOKED';
      }

      // Mappa modality
      let modality: SlotModality = 'BOTH';
      if (slot.mode === 'IN_PERSON') modality = 'IN_PERSON';
      else if (slot.mode === 'TELEVISIT') modality = 'TELEVISIT';

      return {
        id: slot.id,
        date: start.toISOString().split('T')[0],
        time: `${String(start.getHours()).padStart(2, '0')}:${String(start.getMinutes()).padStart(2, '0')}`,
        duration: durationMinutes,
        modality,
        status,
        patientId: appointment?.patientId,
        patientName: patient ? `${patient.lastName} ${patient.firstName}` : undefined,
        visitReason: appointment?.reason,
        appointmentId: appointment?.id
      };
    });
  }

  getDateString(daysFromMonday: number): string {
    const date = new Date(this.currentWeekStart);
    date.setDate(date.getDate() + daysFromMonday);
    return date.toISOString().split('T')[0];
  }

  getSlotsForDay(day: Date): TimeSlot[] {
    const dateStr = day.toISOString().split('T')[0];
    return this.slots
      .filter(s => s.date === dateStr)
      .sort((a, b) => a.time.localeCompare(b.time));
  }

  openCreateSlotModal(day?: Date): void {
    this.slotForm = {
      date: day ? day.toISOString().split('T')[0] : '',
      startTime: '08:00',
      endTime: '18:00',
      duration: 30,
      modality: 'BOTH',
      repeatWeekly: false,
      repeatWeeks: 4
    };
    this.selectedDate = this.slotForm.date;
    this.errorMessage = '';
    this.showCreateSlotModal = true;
  }

  closeCreateSlotModal(): void {
    this.showCreateSlotModal = false;
  }

  createSlots(): void {
    if (!this.slotForm.date) {
      this.errorMessage = 'Seleziona una data.';
      return;
    }

    const doctorId = this.doctorApi.getDoctorId();
    const departmentCode = this.doctorApi.getDepartmentCode();

    if (!doctorId || !departmentCode) {
      this.errorMessage = 'Dati medico non disponibili.';
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    // Calcola slot da creare
    const startParts = this.slotForm.startTime.split(':');
    const endParts = this.slotForm.endTime.split(':');
    const startMinutes = parseInt(startParts[0]) * 60 + parseInt(startParts[1]);
    const endMinutes = parseInt(endParts[0]) * 60 + parseInt(endParts[1]);
    const slotsCount = Math.floor((endMinutes - startMinutes) / this.slotForm.duration);

    // Mappa modality frontend -> backend
    let mode: VisitMode = 'IN_PERSON';
    if (this.slotForm.modality === 'TELEVISIT') mode = 'TELEVISIT';
    // BOTH non esiste nel backend, usiamo IN_PERSON come default

    const slotCreations: { startAt: string; endAt: string }[] = [];
    let currentTime = startMinutes;
    for (let i = 0; i < slotsCount; i++) {
      const hours = Math.floor(currentTime / 60);
      const mins = currentTime % 60;

      const startDate = new Date(this.slotForm.date);
      startDate.setHours(hours, mins, 0, 0);

      const endDate = new Date(startDate);
      endDate.setMinutes(endDate.getMinutes() + this.slotForm.duration);

      slotCreations.push({
        startAt: startDate.toISOString(),
        endAt: endDate.toISOString()
      });

      currentTime += this.slotForm.duration;
    }

    // Crea slot in parallelo
    const createCalls = slotCreations.map(slot =>
      this.doctorApi.createSlot({
        doctorId,
        departmentCode,
        mode,
        startAt: slot.startAt,
        endAt: slot.endAt
      }).pipe(catchError(() => of(null)))
    );

    forkJoin(createCalls).subscribe({
      next: (results) => {
        const created = results.filter(r => r !== null).length;
        this.isSaving = false;
        this.closeCreateSlotModal();
        this.loadSlots(); // Ricarica slots
        this.successMessage = `${created} slot creati per il ${this.formatDate(this.slotForm.date)}.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Errore nella creazione degli slot.';
      }
    });
  }

  openSlotDetail(slot: TimeSlot): void {
    this.selectedSlot = slot;
    this.showSlotDetailModal = true;
  }

  closeSlotDetailModal(): void {
    this.showSlotDetailModal = false;
    this.selectedSlot = null;
  }

  blockSlot(slot: TimeSlot): void {
    slot.status = 'BLOCKED';
    this.closeSlotDetailModal();
    this.successMessage = `Slot delle ${slot.time} bloccato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  unblockSlot(slot: TimeSlot): void {
    slot.status = 'AVAILABLE';
    this.closeSlotDetailModal();
    this.successMessage = `Slot delle ${slot.time} sbloccato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  cancelAppointment(slot: TimeSlot): void {
    if (!slot.appointmentId) {
      this.errorMessage = 'Appuntamento non trovato.';
      return;
    }

    if (confirm(`Vuoi cancellare l'appuntamento di ${slot.patientName}? Il paziente riceverà una notifica.`)) {
      this.doctorApi.cancelAppointment(slot.appointmentId).subscribe({
        next: () => {
          this.closeSlotDetailModal();
          this.loadSlots(); // Ricarica slots
          this.successMessage = 'Appuntamento cancellato. Il paziente è stato notificato.';
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: () => {
          this.errorMessage = 'Errore nella cancellazione dell\'appuntamento.';
        }
      });
    }
  }

  getSlotBgClass(slot: TimeSlot): string {
    switch (slot.status) {
      case 'AVAILABLE': return 'bg-success bg-opacity-10 border-success';
      case 'BOOKED': return 'bg-primary bg-opacity-10 border-primary';
      case 'BLOCKED': return 'bg-secondary bg-opacity-25 border-secondary';
      default: return '';
    }
  }

  getModalityLabel(modality: SlotModality): string {
    const labels: Record<SlotModality, string> = {
      IN_PERSON: 'In presenza',
      TELEVISIT: 'Televisita',
      BOTH: 'Entrambe'
    };
    return labels[modality];
  }

  getModalityIcon(modality: SlotModality): string {
    const icons: Record<SlotModality, string> = {
      IN_PERSON: 'bi-building',
      TELEVISIT: 'bi-camera-video',
      BOTH: 'bi-arrow-left-right'
    };
    return icons[modality];
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      weekday: 'long',
      day: '2-digit',
      month: 'long'
    });
  }

  formatShortDate(date: Date): string {
    return date.toLocaleDateString('it-IT', {
      weekday: 'short',
      day: '2-digit',
      month: 'short'
    });
  }

  isToday(date: Date): boolean {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  }

  isPast(date: Date): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date < today;
  }

  getWeekRangeLabel(): string {
    const start = this.weekDays[0];
    const end = this.weekDays[6];
    return `${start.getDate()} ${start.toLocaleDateString('it-IT', { month: 'short' })} - ${end.getDate()} ${end.toLocaleDateString('it-IT', { month: 'short', year: 'numeric' })}`;
  }
}

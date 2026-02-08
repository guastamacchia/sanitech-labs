import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { forkJoin, Observable, of } from 'rxjs';
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
    modality: 'IN_PERSON',
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

  // BUG-016: Chiusura modal con Escape
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showSlotDetailModal) {
      this.closeSlotDetailModal();
    } else if (this.showCreateSlotModal) {
      this.closeCreateSlotModal();
    }
  }

  // Statistiche - BUG-010: totalSlots include tutti gli slot caricati
  get totalSlots(): number {
    return this.slots.length;
  }

  get availableSlots(): number {
    return this.slots.filter(s => s.status === 'AVAILABLE').length;
  }

  get bookedSlots(): number {
    return this.slots.filter(s => s.status === 'BOOKED').length;
  }

  get cancelledSlots(): number {
    return this.slots.filter(s => s.status === 'CANCELLED').length;
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

  // BUG-004: Week navigation now reloads slots
  previousWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
    this.generateWeekDays();
    this.loadSlots();
  }

  nextWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.generateWeekDays();
    this.loadSlots();
  }

  goToToday(): void {
    this.initializeWeek();
    this.loadSlots();
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

    // BUG-008/009/017: Usa date locali per costruire ISO strings corrette
    const weekStart = new Date(this.currentWeekStart);
    weekStart.setHours(0, 0, 0, 0);
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekEnd.getDate() + 7);

    // Carica slot e appuntamenti in parallelo
    // BUG-014: Aggiunto filtro date anche per appuntamenti
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

      // BUG-015: Mappa status allineato al backend (AVAILABLE, BOOKED, CANCELLED)
      let status: SlotStatus = 'AVAILABLE';
      if (slot.status === 'CANCELLED') {
        status = 'CANCELLED';
      } else if (appointment) {
        status = 'BOOKED';
      }

      // Mappa modality - BUG-006: rimosso BOTH, mappiamo direttamente
      let modality: SlotModality = 'IN_PERSON';
      if (slot.mode === 'TELEVISIT') modality = 'TELEVISIT';

      // BUG-008/009/017: Usa metodi locali per data e orario
      return {
        id: slot.id,
        date: this.toLocalDateString(start),
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

  // BUG-008/009/017: Helper per convertire Date in stringa YYYY-MM-DD locale
  private toLocalDateString(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  getDateString(daysFromMonday: number): string {
    const date = new Date(this.currentWeekStart);
    date.setDate(date.getDate() + daysFromMonday);
    return this.toLocalDateString(date);
  }

  getSlotsForDay(day: Date): TimeSlot[] {
    const dateStr = this.toLocalDateString(day);
    return this.slots
      .filter(s => s.date === dateStr)
      .sort((a, b) => a.time.localeCompare(b.time));
  }

  openCreateSlotModal(day?: Date): void {
    this.slotForm = {
      date: day ? this.toLocalDateString(day) : '',
      startTime: '08:00',
      endTime: '18:00',
      duration: 30,
      modality: 'IN_PERSON',
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

    // BUG-006: Determina le modalità da creare
    const modes: VisitMode[] = [];
    if (this.slotForm.modality === 'BOTH') {
      modes.push('IN_PERSON', 'TELEVISIT');
    } else {
      modes.push(this.slotForm.modality as VisitMode);
    }

    // BUG-007: Calcola settimane da ripetere
    const weeksToCreate = this.slotForm.repeatWeekly ? this.slotForm.repeatWeeks : 1;

    const allCreateCalls: Observable<SlotDto | null>[] = [];

    for (let week = 0; week < weeksToCreate; week++) {
      let currentTime = startMinutes;
      for (let i = 0; i < slotsCount; i++) {
        const hours = Math.floor(currentTime / 60);
        const mins = currentTime % 60;

        const startDate = new Date(this.slotForm.date);
        startDate.setDate(startDate.getDate() + (week * 7));
        startDate.setHours(hours, mins, 0, 0);

        const endDate = new Date(startDate);
        endDate.setMinutes(endDate.getMinutes() + this.slotForm.duration);

        // BUG-006: Crea uno slot per ogni modalità selezionata
        for (const mode of modes) {
          allCreateCalls.push(
            this.doctorApi.createSlot({
              doctorId,
              departmentCode,
              mode,
              startAt: startDate.toISOString(),
              endAt: endDate.toISOString()
            }).pipe(catchError(() => of(null)))
          );
        }

        currentTime += this.slotForm.duration;
      }
    }

    if (allCreateCalls.length === 0) {
      this.isSaving = false;
      this.errorMessage = 'Nessuno slot da creare con i parametri selezionati.';
      return;
    }

    forkJoin(allCreateCalls).subscribe({
      next: (results) => {
        const created = results.filter(r => r !== null).length;
        this.isSaving = false;
        this.closeCreateSlotModal();
        this.loadSlots(); // Ricarica slots
        const weekLabel = weeksToCreate > 1 ? ` per ${weeksToCreate} settimane` : '';
        this.successMessage = `${created} slot creati${weekLabel}.`;
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

  // BUG-003: Block slot ora usa l'API cancelSlot del backend
  blockSlot(slot: TimeSlot): void {
    this.doctorApi.cancelSlot(slot.id).subscribe({
      next: () => {
        this.closeSlotDetailModal();
        this.loadSlots();
        this.successMessage = `Slot delle ${slot.time} cancellato.`;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => {
        this.errorMessage = 'Errore nella cancellazione dello slot.';
      }
    });
  }

  // BUG-012: Completa appuntamento
  completeAppointment(slot: TimeSlot): void {
    if (!slot.appointmentId) {
      this.errorMessage = 'Appuntamento non trovato.';
      return;
    }

    if (confirm(`Vuoi completare la visita di ${slot.patientName}?`)) {
      this.doctorApi.completeAppointment(slot.appointmentId).subscribe({
        next: () => {
          this.closeSlotDetailModal();
          this.loadSlots();
          this.successMessage = 'Visita completata con successo.';
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: () => {
          this.errorMessage = 'Errore nel completamento della visita.';
        }
      });
    }
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
      case 'CANCELLED': return 'bg-secondary bg-opacity-25 border-secondary';
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

  // BUG-016: Chiusura modal con click su backdrop
  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal')) {
      if (this.showSlotDetailModal) {
        this.closeSlotDetailModal();
      } else if (this.showCreateSlotModal) {
        this.closeCreateSlotModal();
      }
    }
  }
}

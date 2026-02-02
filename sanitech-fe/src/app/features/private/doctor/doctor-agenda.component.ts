import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type SlotStatus = 'AVAILABLE' | 'BOOKED' | 'BLOCKED';
type SlotModality = 'IN_PERSON' | 'TELEVISIT' | 'BOTH';

interface TimeSlot {
  id: number;
  date: string;
  time: string;
  duration: number;
  modality: SlotModality;
  status: SlotStatus;
  patientId?: number;
  patientName?: string;
  visitReason?: string;
}

interface SlotCreationForm {
  date: string;
  startTime: string;
  endTime: string;
  duration: number;
  modality: SlotModality;
  repeatWeekly: boolean;
  repeatWeeks: number;
}

@Component({
  selector: 'app-doctor-agenda',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-agenda.component.html'
})
export class DoctorAgendaComponent implements OnInit {
  // Slots
  slots: TimeSlot[] = [];

  // Vista corrente
  currentWeekStart: Date = new Date();
  weekDays: Date[] = [];

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

  // UI State
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showCreateSlotModal = false;
  showSlotDetailModal = false;
  selectedSlot: TimeSlot | null = null;
  selectedDate: string = '';

  // Stats
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
    this.initializeWeek();
    this.loadSlots();
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

    setTimeout(() => {
      // Mock data - scenario Dott. Verdi
      const baseDate = new Date();
      this.slots = [
        // Lunedì
        { id: 1, date: this.getDateString(0), time: '09:00', duration: 30, modality: 'IN_PERSON', status: 'BOOKED', patientId: 1, patientName: 'Esposito Mario', visitReason: 'Controllo post-operatorio' },
        { id: 2, date: this.getDateString(0), time: '09:30', duration: 30, modality: 'IN_PERSON', status: 'BOOKED', patientId: 2, patientName: 'Verdi Anna', visitReason: 'Prima visita' },
        { id: 3, date: this.getDateString(0), time: '10:00', duration: 30, modality: 'BOTH', status: 'AVAILABLE' },
        { id: 4, date: this.getDateString(0), time: '10:30', duration: 30, modality: 'BOTH', status: 'AVAILABLE' },
        { id: 5, date: this.getDateString(0), time: '11:00', duration: 30, modality: 'BOTH', status: 'BLOCKED' },
        // Martedì
        { id: 6, date: this.getDateString(1), time: '09:00', duration: 30, modality: 'TELEVISIT', status: 'BOOKED', patientId: 3, patientName: 'Bianchi Luigi', visitReason: 'Televisita di controllo' },
        { id: 7, date: this.getDateString(1), time: '09:30', duration: 30, modality: 'TELEVISIT', status: 'AVAILABLE' },
        { id: 8, date: this.getDateString(1), time: '10:00', duration: 30, modality: 'TELEVISIT', status: 'AVAILABLE' },
        // Mercoledì
        { id: 9, date: this.getDateString(2), time: '14:00', duration: 30, modality: 'IN_PERSON', status: 'AVAILABLE' },
        { id: 10, date: this.getDateString(2), time: '14:30', duration: 30, modality: 'IN_PERSON', status: 'AVAILABLE' },
        { id: 11, date: this.getDateString(2), time: '15:00', duration: 30, modality: 'IN_PERSON', status: 'BOOKED', patientId: 4, patientName: 'Rossi Giulia', visitReason: 'Visita specialistica' },
        // Giovedì
        { id: 12, date: this.getDateString(3), time: '14:00', duration: 30, modality: 'BOTH', status: 'AVAILABLE' },
        { id: 13, date: this.getDateString(3), time: '14:30', duration: 30, modality: 'BOTH', status: 'AVAILABLE' },
        { id: 14, date: this.getDateString(3), time: '15:00', duration: 30, modality: 'BOTH', status: 'AVAILABLE' },
        { id: 15, date: this.getDateString(3), time: '15:30', duration: 30, modality: 'BOTH', status: 'AVAILABLE' },
        // Venerdì
        { id: 16, date: this.getDateString(4), time: '09:00', duration: 30, modality: 'IN_PERSON', status: 'BOOKED', patientId: 5, patientName: 'Romano Francesco', visitReason: 'Controllo terapia' },
        { id: 17, date: this.getDateString(4), time: '09:30', duration: 30, modality: 'IN_PERSON', status: 'AVAILABLE' },
        { id: 18, date: this.getDateString(4), time: '10:00', duration: 30, modality: 'IN_PERSON', status: 'AVAILABLE' }
      ];

      this.isLoading = false;
    }, 500);
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

    this.isSaving = true;

    setTimeout(() => {
      // Calcola numero di slot da creare
      const startParts = this.slotForm.startTime.split(':');
      const endParts = this.slotForm.endTime.split(':');
      const startMinutes = parseInt(startParts[0]) * 60 + parseInt(startParts[1]);
      const endMinutes = parseInt(endParts[0]) * 60 + parseInt(endParts[1]);
      const slotsCount = Math.floor((endMinutes - startMinutes) / this.slotForm.duration);

      let currentTime = startMinutes;
      for (let i = 0; i < slotsCount; i++) {
        const hours = Math.floor(currentTime / 60).toString().padStart(2, '0');
        const mins = (currentTime % 60).toString().padStart(2, '0');

        this.slots.push({
          id: this.slots.length + 1,
          date: this.slotForm.date,
          time: `${hours}:${mins}`,
          duration: this.slotForm.duration,
          modality: this.slotForm.modality,
          status: 'AVAILABLE'
        });

        currentTime += this.slotForm.duration;
      }

      this.isSaving = false;
      this.closeCreateSlotModal();
      this.successMessage = `${slotsCount} slot creati per il ${this.formatDate(this.slotForm.date)}.`;
      setTimeout(() => this.successMessage = '', 5000);
    }, 1000);
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
    if (confirm(`Vuoi cancellare l'appuntamento di ${slot.patientName}? Il paziente riceverà una notifica.`)) {
      slot.status = 'AVAILABLE';
      slot.patientId = undefined;
      slot.patientName = undefined;
      slot.visitReason = undefined;
      this.closeSlotDetailModal();
      this.successMessage = 'Appuntamento cancellato. Il paziente è stato notificato.';
      setTimeout(() => this.successMessage = '', 5000);
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

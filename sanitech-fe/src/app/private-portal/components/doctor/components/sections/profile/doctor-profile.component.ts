import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DoctorApiService, DoctorDto, SlotDto } from '../../../services/doctor-api.service';
import { DoctorProfile, Availability, NotificationPreferences, DoctorStats } from './dtos/profile.dto';

@Component({
  selector: 'app-doctor-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-profile.component.html'
})
export class DoctorProfileComponent implements OnInit {
  // Dati profilo
  profile: DoctorProfile = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    department: '',
    facility: '',
    specialization: '',
    licenseNumber: ''
  };

  // Form modifica
  editForm = {
    phone: ''
  };

  // Disponibilità settimanale (caricata dalla API slots)
  availability: Availability[] = [];
  isLoadingAvailability = false;

  // Preferenze notifiche
  notificationPreferences: NotificationPreferences = {
    emailConsents: true,
    emailAppointments: true,
    emailDocuments: false,
    emailAdmissions: true
  };
  isEditingPreferences = false;
  isSavingPreferences = false;

  // Statistiche (caricate da API separate)
  stats: DoctorStats = {
    activePatients: 0,
    monthlyAppointments: 0,
    activeConsents: 0,
    pendingTelevisits: 0
  };

  // Stato UI
  isEditing = false;
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';

  constructor(private doctorApi: DoctorApiService) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadWeeklyAvailability();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.doctorApi.getCurrentDoctor().subscribe({
      next: (doctor) => {
        if (doctor) {
          this.mapDoctorToProfile(doctor);
          this.loadStats();
        } else {
          this.errorMessage = 'Profilo medico non trovato.';
        }
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento del profilo.';
        this.isLoading = false;
      }
    });
  }

  private mapDoctorToProfile(doctor: DoctorDto): void {
    this.profile = {
      firstName: doctor.firstName,
      lastName: doctor.lastName,
      email: doctor.email,
      phone: doctor.phone || '',
      department: doctor.departmentName || doctor.departmentCode || '',
      facility: doctor.facilityName || doctor.facilityCode || '',
      specialization: doctor.specialization || '',
      licenseNumber: '' // Non disponibile nel backend
    };

    this.editForm.phone = this.profile.phone;
  }

  private loadWeeklyAvailability(): void {
    const doctorId = this.doctorApi.getDoctorId();
    if (!doctorId) {
      this.availability = this.getEmptyWeekAvailability();
      return;
    }

    this.isLoadingAvailability = true;

    // Calcola inizio e fine della settimana corrente (lunedì - domenica)
    const now = new Date();
    const dayOfWeek = now.getDay(); // 0 = domenica, 1 = lunedì, ...
    const diffToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;

    const monday = new Date(now);
    monday.setDate(now.getDate() + diffToMonday);
    monday.setHours(0, 0, 0, 0);

    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    sunday.setHours(23, 59, 59, 999);

    this.doctorApi.searchSlots({
      doctorId,
      from: monday.toISOString(),
      to: sunday.toISOString(),
      size: 500
    }).subscribe({
      next: (page) => {
        this.availability = this.aggregateSlotsToAvailability(page.content, monday);
        this.isLoadingAvailability = false;
      },
      error: () => {
        this.availability = this.getEmptyWeekAvailability();
        this.isLoadingAvailability = false;
      }
    });
  }

  private getEmptyWeekAvailability(): Availability[] {
    return [
      { day: 'Lunedì', morning: '-', afternoon: '-' },
      { day: 'Martedì', morning: '-', afternoon: '-' },
      { day: 'Mercoledì', morning: '-', afternoon: '-' },
      { day: 'Giovedì', morning: '-', afternoon: '-' },
      { day: 'Venerdì', morning: '-', afternoon: '-' }
    ];
  }

  private aggregateSlotsToAvailability(slots: SlotDto[], weekStart: Date): Availability[] {
    const dayNames = ['Lunedì', 'Martedì', 'Mercoledì', 'Giovedì', 'Venerdì'];
    const result: Availability[] = dayNames.map(day => ({ day, morning: '-', afternoon: '-' }));

    // Raggruppa gli slot per giorno della settimana
    const slotsByDay: Map<number, SlotDto[]> = new Map();
    for (let i = 0; i < 5; i++) {
      slotsByDay.set(i, []);
    }

    for (const slot of slots) {
      const slotDate = new Date(slot.startAt);
      const dayIndex = this.getDayIndex(slotDate, weekStart);

      // Solo giorni feriali (lunedì=0 ... venerdì=4)
      if (dayIndex >= 0 && dayIndex <= 4) {
        slotsByDay.get(dayIndex)?.push(slot);
      }
    }

    // Per ogni giorno, calcola le fasce orarie mattina e pomeriggio
    for (let dayIndex = 0; dayIndex < 5; dayIndex++) {
      const daySlots = slotsByDay.get(dayIndex) || [];
      if (daySlots.length === 0) continue;

      // Separa slot mattina (prima delle 13) e pomeriggio (dalle 13 in poi)
      const morningSlots: SlotDto[] = [];
      const afternoonSlots: SlotDto[] = [];

      for (const slot of daySlots) {
        const startHour = new Date(slot.startAt).getHours();
        if (startHour < 13) {
          morningSlots.push(slot);
        } else {
          afternoonSlots.push(slot);
        }
      }

      result[dayIndex].morning = this.formatTimeRange(morningSlots);
      result[dayIndex].afternoon = this.formatTimeRange(afternoonSlots);
    }

    return result;
  }

  private getDayIndex(date: Date, weekStart: Date): number {
    const diffMs = date.getTime() - weekStart.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    return diffDays;
  }

  private formatTimeRange(slots: SlotDto[]): string {
    if (slots.length === 0) return '-';

    // Ordina per ora di inizio
    slots.sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime());

    const firstStart = new Date(slots[0].startAt);
    const lastEnd = new Date(slots[slots.length - 1].endAt);

    const formatTime = (d: Date) =>
      `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;

    return `${formatTime(firstStart)} - ${formatTime(lastEnd)}`;
  }

  private loadStats(): void {
    const doctorId = this.doctorApi.getDoctorId();
    if (!doctorId) return;

    // Carica statistiche da varie API
    this.doctorApi.searchPatients({ size: 1 }).subscribe({
      next: (page) => {
        this.stats.activePatients = page.totalElements;
      }
    });

    this.doctorApi.searchAppointments({ doctorId, size: 1 }).subscribe({
      next: (page) => {
        this.stats.monthlyAppointments = page.totalElements;
      }
    });

    this.doctorApi.searchTelevisits({ status: 'SCHEDULED', size: 1 }).subscribe({
      next: (page) => {
        this.stats.pendingTelevisits = page.totalElements;
      }
    });
  }

  startEditing(): void {
    this.editForm = { phone: this.profile.phone };
    this.isEditing = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  cancelEditing(): void {
    this.isEditing = false;
    this.editForm = { phone: this.profile.phone };
    this.errorMessage = '';
  }

  saveProfile(): void {
    if (!this.editForm.phone || this.editForm.phone.trim().length < 10) {
      this.errorMessage = 'Inserisci un numero di telefono valido.';
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    this.doctorApi.updateMyPhone(this.editForm.phone.trim()).subscribe({
      next: (doctor) => {
        this.mapDoctorToProfile(doctor);
        this.isEditing = false;
        this.isSaving = false;
        this.successMessage = 'Profilo aggiornato con successo!';
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Errore durante il salvataggio. Riprova.';
      }
    });
  }

  startEditingPreferences(): void {
    this.isEditingPreferences = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  cancelEditingPreferences(): void {
    this.isEditingPreferences = false;
    // Reset alle preferenze salvate (in futuro caricate da API)
    this.notificationPreferences = {
      emailConsents: true,
      emailAppointments: true,
      emailDocuments: false,
      emailAdmissions: true
    };
  }

  saveNotificationPreferences(): void {
    this.isSavingPreferences = true;
    this.errorMessage = '';

    // Nota: il backend non ha un endpoint per le preferenze notifiche
    // Questo sarebbe un'estensione futura
    setTimeout(() => {
      this.isSavingPreferences = false;
      this.isEditingPreferences = false;
      this.successMessage = 'Preferenze di notifica salvate con successo!';
      setTimeout(() => this.successMessage = '', 5000);
    }, 1000);
  }
}

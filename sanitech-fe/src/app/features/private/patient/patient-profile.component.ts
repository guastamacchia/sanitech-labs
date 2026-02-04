import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { PatientService, PatientDto, NotificationPreferenceDto } from './services/patient.service';

@Component({
  selector: 'app-patient-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-profile.component.html'
})
export class PatientProfileComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Dati profilo dal backend
  profile: PatientDto | null = null;

  // Form di modifica (solo campi editabili)
  editForm = {
    phone: ''
  };

  // Preferenze notifiche caricate dal backend
  notificationPrefs: NotificationPreferenceDto = {
    emailReminders: true,
    smsReminders: false,
    emailDocuments: true,
    smsDocuments: false,
    emailPayments: true,
    smsPayments: false
  };

  // UI State
  isEditing = false;
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showPreferencesModal = false;

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.loadProfileAndPreferences();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadProfileAndPreferences(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      profile: this.patientService.getMyProfile(),
      preferences: this.patientService.getNotificationPreferences()
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ profile, preferences }) => {
          this.profile = profile;
          this.notificationPrefs = preferences;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Errore caricamento profilo:', err);
          this.errorMessage = 'Impossibile caricare il profilo. Riprova.';
          this.isLoading = false;
        }
      });
  }

  startEditing(): void {
    if (!this.profile) return;
    this.editForm = {
      phone: this.profile.phone || ''
    };
    this.isEditing = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  cancelEditing(): void {
    this.isEditing = false;
    this.errorMessage = '';
  }

  saveProfile(): void {
    if (!this.validateForm() || !this.profile) {
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    this.patientService.updateMyPhone({ phone: this.editForm.phone })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedProfile) => {
          this.profile = updatedProfile;
          this.isEditing = false;
          this.isSaving = false;
          this.successMessage = 'Profilo aggiornato con successo!';
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          console.error('Errore aggiornamento profilo:', err);
          this.errorMessage = 'Impossibile aggiornare il profilo. Riprova.';
          this.isSaving = false;
        }
      });
  }

  validateForm(): boolean {
    if (!this.editForm.phone || this.editForm.phone.trim().length < 10) {
      this.errorMessage = 'Inserisci un numero di telefono valido.';
      return false;
    }
    return true;
  }

  openPreferencesModal(): void {
    this.showPreferencesModal = true;
  }

  closePreferencesModal(): void {
    this.showPreferencesModal = false;
  }

  savePreferences(): void {
    this.isSaving = true;
    this.errorMessage = '';

    this.patientService.updateNotificationPreferences(this.notificationPrefs)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedPrefs) => {
          this.notificationPrefs = updatedPrefs;
          this.isSaving = false;
          this.showPreferencesModal = false;
          this.successMessage = 'Preferenze di notifica aggiornate!';
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          console.error('Errore salvataggio preferenze:', err);
          this.errorMessage = 'Impossibile salvare le preferenze. Riprova.';
          this.isSaving = false;
        }
      });
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  getGenderLabel(gender: string): string {
    return gender === 'F' ? 'Femmina' : gender === 'M' ? 'Maschio' : 'Non specificato';
  }
}

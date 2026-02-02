import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { PatientService, PatientDto } from './services/patient.service';

interface NotificationPreferences {
  emailReminders: boolean;
  smsReminders: boolean;
  emailDocuments: boolean;
  smsDocuments: boolean;
  emailPayments: boolean;
  smsPayments: boolean;
}

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

  // Preferenze notifiche (gestite localmente per ora - il backend non ha questo endpoint)
  notificationPrefs: NotificationPreferences = {
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
    this.loadProfile();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.patientService.getMyProfile()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (profile) => {
          this.profile = profile;
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
    // Le preferenze notifiche non sono gestite dal backend attualmente
    // Salvataggio locale simulato
    setTimeout(() => {
      this.isSaving = false;
      this.showPreferencesModal = false;
      this.successMessage = 'Preferenze di notifica aggiornate!';
      setTimeout(() => this.successMessage = '', 5000);
    }, 500);
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

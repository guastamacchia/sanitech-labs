import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil, catchError, of } from 'rxjs';
import { PatientService, PatientDto, NotificationPreferenceDto } from '../../../services/patient.service';

/** Regex per validazione telefono: prefisso + opzionale, poi cifre/spazi/trattini/parentesi, min 10 max 20 caratteri */
const PHONE_REGEX = /^\+?[\d\s\-()]+$/;

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

  // Snapshot preferenze per ripristino su annullamento modale
  private originalPrefs: NotificationPreferenceDto | null = null;

  // Stato UI
  isEditing = false;
  isLoading = false;
  isSavingProfile = false;
  isSavingPreferences = false;
  successMessage = '';
  errorMessage = '';
  phoneError = '';
  showPreferencesModal = false;

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.loadProfileAndPreferences();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeKey(event: KeyboardEvent): void {
    if (this.showPreferencesModal) {
      event.preventDefault();
      this.closePreferencesModal();
    }
  }

  loadProfileAndPreferences(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Carica profilo e preferenze separatamente per gestire errori parziali
    this.patientService.getMyProfile()
      .pipe(
        takeUntil(this.destroy$),
        catchError(err => {
          console.error('Errore caricamento profilo:', err);
          this.errorMessage = this.extractErrorMessage(err, 'Impossibile caricare il profilo.');
          return of(null);
        })
      )
      .subscribe(profile => {
        this.profile = profile;
        this.isLoading = false;
      });

    this.patientService.getNotificationPreferences()
      .pipe(
        takeUntil(this.destroy$),
        catchError(err => {
          console.error('Errore caricamento preferenze:', err);
          // Usa i defaults se le preferenze non si caricano
          return of(this.notificationPrefs);
        })
      )
      .subscribe(preferences => {
        this.notificationPrefs = preferences;
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
    this.phoneError = '';
  }

  cancelEditing(): void {
    this.isEditing = false;
    this.errorMessage = '';
    this.phoneError = '';
  }

  saveProfile(): void {
    if (!this.validateForm() || !this.profile) {
      return;
    }

    this.isSavingProfile = true;
    this.errorMessage = '';

    // Se il telefono è vuoto, invia null per consentire la rimozione
    const phoneValue = this.editForm.phone.trim() || null;

    this.patientService.updateMyPhone({ phone: phoneValue })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedProfile) => {
          this.profile = updatedProfile;
          this.isEditing = false;
          this.isSavingProfile = false;
          this.phoneError = '';
          this.successMessage = 'Profilo aggiornato con successo!';
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          console.error('Errore aggiornamento profilo:', err);
          this.errorMessage = this.extractErrorMessage(err, 'Impossibile aggiornare il profilo.');
          this.isSavingProfile = false;
          setTimeout(() => this.errorMessage = '', 8000);
        }
      });
  }

  validateForm(): boolean {
    this.phoneError = '';
    const phone = this.editForm.phone.trim();

    // Il telefono è opzionale: se vuoto, è valido (il backend accetta null)
    if (!phone) {
      return true;
    }

    if (phone.length < 10) {
      this.phoneError = 'Il numero di telefono deve avere almeno 10 caratteri.';
      return false;
    }

    if (phone.length > 20) {
      this.phoneError = 'Il numero di telefono non può superare i 20 caratteri.';
      return false;
    }

    if (!PHONE_REGEX.test(phone)) {
      this.phoneError = 'Il numero di telefono può contenere solo cifre, spazi, trattini, parentesi e il prefisso +.';
      return false;
    }

    return true;
  }

  openPreferencesModal(): void {
    // Salva snapshot delle preferenze per eventuale ripristino
    this.originalPrefs = { ...this.notificationPrefs };
    this.showPreferencesModal = true;
    this.errorMessage = '';
  }

  closePreferencesModal(): void {
    // Ripristina le preferenze allo stato originale (pre-apertura modale)
    if (this.originalPrefs) {
      this.notificationPrefs = { ...this.originalPrefs };
    }
    this.showPreferencesModal = false;
  }

  savePreferences(): void {
    this.isSavingPreferences = true;
    this.errorMessage = '';

    this.patientService.updateNotificationPreferences(this.notificationPrefs)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedPrefs) => {
          this.notificationPrefs = updatedPrefs;
          this.originalPrefs = { ...updatedPrefs };
          this.isSavingPreferences = false;
          this.showPreferencesModal = false;
          this.successMessage = 'Preferenze di notifica aggiornate!';
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          console.error('Errore salvataggio preferenze:', err);
          this.errorMessage = this.extractErrorMessage(err, 'Impossibile salvare le preferenze.');
          this.isSavingPreferences = false;
          setTimeout(() => this.errorMessage = '', 8000);
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

  private extractErrorMessage(err: any, fallback: string): string {
    if (err?.error?.message) {
      return err.error.message;
    }
    if (err?.status === 0) {
      return 'Impossibile contattare il server. Verifica la connessione.';
    }
    if (err?.status === 401 || err?.status === 403) {
      return 'Sessione scaduta. Ricarica la pagina ed effettua nuovamente l\'accesso.';
    }
    return fallback;
  }
}

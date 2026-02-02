import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface UserProfile {
  firstName: string;
  lastName: string;
  fiscalCode: string;
  email: string;
  phone: string;
  address: string;
  birthDate: string;
  gender: string;
}

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
export class PatientProfileComponent implements OnInit {
  // Dati profilo
  profile: UserProfile = {
    firstName: 'Laura',
    lastName: 'Bianchi',
    fiscalCode: 'BNCLRA85M45H501Z',
    email: 'laura.bianchi@email.it',
    phone: '+39 333 1234567',
    address: 'Via Roma 42, 20121 Milano (MI)',
    birthDate: '1985-08-05',
    gender: 'F'
  };

  // Form di modifica
  editForm: UserProfile = { ...this.profile };

  // Preferenze notifiche
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

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    // Simula caricamento dal backend
    setTimeout(() => {
      this.isLoading = false;
    }, 500);
  }

  startEditing(): void {
    this.editForm = { ...this.profile };
    this.isEditing = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  cancelEditing(): void {
    this.isEditing = false;
    this.editForm = { ...this.profile };
    this.errorMessage = '';
  }

  saveProfile(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    // Simula salvataggio
    setTimeout(() => {
      this.profile = { ...this.editForm };
      this.isEditing = false;
      this.isSaving = false;
      this.successMessage = 'Profilo aggiornato con successo!';
      setTimeout(() => this.successMessage = '', 5000);
    }, 1000);
  }

  validateForm(): boolean {
    if (!this.editForm.phone || this.editForm.phone.trim().length < 10) {
      this.errorMessage = 'Inserisci un numero di telefono valido.';
      return false;
    }
    if (!this.editForm.address || this.editForm.address.trim().length < 5) {
      this.errorMessage = 'Inserisci un indirizzo valido.';
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
    // Simula salvataggio preferenze
    setTimeout(() => {
      this.isSaving = false;
      this.showPreferencesModal = false;
      this.successMessage = 'Preferenze di notifica aggiornate!';
      setTimeout(() => this.successMessage = '', 5000);
    }, 800);
  }

  formatDate(dateStr: string): string {
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

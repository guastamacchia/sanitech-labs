import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface DoctorProfile {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  department: string;
  facility: string;
  specialization: string;
  licenseNumber: string;
}

interface Availability {
  day: string;
  morning: string;
  afternoon: string;
}

interface SpecializationRequest {
  name: string;
  certificate: File | null;
  notes: string;
}

interface DoctorStats {
  activePatients: number;
  monthlyAppointments: number;
  activeConsents: number;
  pendingTelevisits: number;
}

@Component({
  selector: 'app-doctor-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-profile.component.html'
})
export class DoctorProfileComponent implements OnInit {
  // Dati profilo
  profile: DoctorProfile = {
    firstName: 'Marco',
    lastName: 'Ricci',
    email: 'marco.ricci@ospedale.it',
    phone: '+39 02 1234567',
    department: 'Ortopedia',
    facility: 'Ospedale San Raffaele',
    specialization: 'Ortopedia e Traumatologia',
    licenseNumber: 'RM-12345'
  };

  // Form modifica
  editForm = {
    phone: this.profile.phone
  };

  // Disponibilità settimanale
  availability: Availability[] = [
    { day: 'Lunedì', morning: '08:00 - 13:00', afternoon: '14:00 - 18:00' },
    { day: 'Martedì', morning: '08:00 - 13:00', afternoon: '-' },
    { day: 'Mercoledì', morning: '08:00 - 13:00', afternoon: '14:00 - 18:00' },
    { day: 'Giovedì', morning: '-', afternoon: '14:00 - 18:00' },
    { day: 'Venerdì', morning: '08:00 - 13:00', afternoon: '-' }
  ];

  // Specializzazioni
  specializations: string[] = ['Ortopedia e Traumatologia'];

  // Richiesta nuova specializzazione
  specializationRequest: SpecializationRequest = {
    name: '',
    certificate: null,
    notes: ''
  };

  // Statistiche
  stats: DoctorStats = {
    activePatients: 127,
    monthlyAppointments: 48,
    activeConsents: 89,
    pendingTelevisits: 5
  };

  // UI State
  isEditing = false;
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showSpecializationModal = false;

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    setTimeout(() => {
      this.isLoading = false;
    }, 500);
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

    setTimeout(() => {
      this.profile.phone = this.editForm.phone;
      this.isEditing = false;
      this.isSaving = false;
      this.successMessage = 'Profilo aggiornato con successo!';
      setTimeout(() => this.successMessage = '', 5000);
    }, 1000);
  }

  openSpecializationModal(): void {
    this.specializationRequest = { name: '', certificate: null, notes: '' };
    this.showSpecializationModal = true;
  }

  closeSpecializationModal(): void {
    this.showSpecializationModal = false;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.specializationRequest.certificate = input.files[0];
    }
  }

  submitSpecializationRequest(): void {
    if (!this.specializationRequest.name.trim()) {
      this.errorMessage = 'Inserisci il nome della specializzazione.';
      return;
    }
    if (!this.specializationRequest.certificate) {
      this.errorMessage = 'Allega il certificato di specializzazione.';
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    setTimeout(() => {
      this.isSaving = false;
      this.showSpecializationModal = false;
      this.successMessage = 'Richiesta di aggiunta specializzazione inviata all\'amministratore. Riceverai una notifica dopo la verifica.';
      setTimeout(() => this.successMessage = '', 7000);
    }, 1500);
  }
}

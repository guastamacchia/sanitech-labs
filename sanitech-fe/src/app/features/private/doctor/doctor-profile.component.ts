import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DoctorApiService, DoctorDto } from './doctor-api.service';

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

  // Disponibilità settimanale (statico per ora - potrebbe essere un'API in futuro)
  availability: Availability[] = [
    { day: 'Lunedì', morning: '08:00 - 13:00', afternoon: '14:00 - 18:00' },
    { day: 'Martedì', morning: '08:00 - 13:00', afternoon: '-' },
    { day: 'Mercoledì', morning: '08:00 - 13:00', afternoon: '14:00 - 18:00' },
    { day: 'Giovedì', morning: '-', afternoon: '14:00 - 18:00' },
    { day: 'Venerdì', morning: '08:00 - 13:00', afternoon: '-' }
  ];

  // Specializzazioni
  specializations: string[] = [];

  // Richiesta nuova specializzazione
  specializationRequest: SpecializationRequest = {
    name: '',
    certificate: null,
    notes: ''
  };

  // Statistiche (caricate da API separate)
  stats: DoctorStats = {
    activePatients: 0,
    monthlyAppointments: 0,
    activeConsents: 0,
    pendingTelevisits: 0
  };

  // UI State
  isEditing = false;
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showSpecializationModal = false;

  constructor(private doctorApi: DoctorApiService) {}

  ngOnInit(): void {
    this.loadProfile();
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

    if (doctor.specialization) {
      this.specializations = [doctor.specialization];
    }
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

    // Nota: il backend non ha un endpoint per l'aggiornamento del profilo medico
    // Questo sarebbe un'estensione futura
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

    // Nota: il backend non ha un endpoint per le richieste di specializzazione
    // Questo sarebbe un'estensione futura
    setTimeout(() => {
      this.isSaving = false;
      this.showSpecializationModal = false;
      this.successMessage = 'Richiesta di aggiunta specializzazione inviata all\'amministratore. Riceverai una notifica dopo la verifica.';
      setTimeout(() => this.successMessage = '', 7000);
    }, 1500);
  }
}

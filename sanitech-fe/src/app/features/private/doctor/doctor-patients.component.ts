import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type ConsentScope = 'DOCS' | 'PRESCRIPTIONS' | 'TELEVISIT';

interface Patient {
  id: number;
  firstName: string;
  lastName: string;
  fiscalCode: string;
  email: string;
  phone: string;
  birthDate: string;
  consents: ConsentScope[];
  lastAccess?: string;
  nextAppointment?: string;
  hasActiveConsent: boolean;
}

@Component({
  selector: 'app-doctor-patients',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-patients.component.html'
})
export class DoctorPatientsComponent implements OnInit {
  // Lista pazienti
  patients: Patient[] = [];

  // Filtri
  searchQuery = '';
  consentFilter: 'ALL' | ConsentScope = 'ALL';
  viewMode: 'consented' | 'all' = 'consented';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // UI State
  isLoading = false;
  selectedPatient: Patient | null = null;
  showPatientModal = false;

  // Stats
  get totalWithConsent(): number {
    return this.patients.filter(p => p.hasActiveConsent).length;
  }

  get totalPatients(): number {
    return this.patients.length;
  }

  get patientsWithDocs(): number {
    return this.patients.filter(p => p.consents.includes('DOCS')).length;
  }

  get patientsWithPrescriptions(): number {
    return this.patients.filter(p => p.consents.includes('PRESCRIPTIONS')).length;
  }

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.isLoading = true;

    // Mock data - scenario dottoressa Ferretti
    setTimeout(() => {
      this.patients = [
        {
          id: 1,
          firstName: 'Mario',
          lastName: 'Esposito',
          fiscalCode: 'SPSMRA80A01H501X',
          email: 'mario.esposito@email.it',
          phone: '+39 333 1111111',
          birthDate: '1980-01-01',
          consents: ['DOCS', 'PRESCRIPTIONS'],
          lastAccess: '2025-01-28T10:30:00',
          nextAppointment: '2025-02-05T09:00:00',
          hasActiveConsent: true
        },
        {
          id: 2,
          firstName: 'Anna',
          lastName: 'Verdi',
          fiscalCode: 'VRDNNA75B45H501Y',
          email: 'anna.verdi@email.it',
          phone: '+39 333 2222222',
          birthDate: '1975-02-15',
          consents: ['DOCS', 'PRESCRIPTIONS', 'TELEVISIT'],
          lastAccess: '2025-01-25T14:00:00',
          nextAppointment: '2025-02-10T11:30:00',
          hasActiveConsent: true
        },
        {
          id: 3,
          firstName: 'Luigi',
          lastName: 'Bianchi',
          fiscalCode: 'BNCLGU85C20H501Z',
          email: 'luigi.bianchi@email.it',
          phone: '+39 333 3333333',
          birthDate: '1985-03-20',
          consents: ['TELEVISIT'],
          lastAccess: '2025-01-20T16:45:00',
          hasActiveConsent: true
        },
        {
          id: 4,
          firstName: 'Giulia',
          lastName: 'Rossi',
          fiscalCode: 'RSSGLU90D55H501W',
          email: 'giulia.rossi@email.it',
          phone: '+39 333 4444444',
          birthDate: '1990-04-15',
          consents: ['DOCS'],
          lastAccess: '2025-01-15T09:00:00',
          nextAppointment: '2025-02-20T14:00:00',
          hasActiveConsent: true
        },
        {
          id: 5,
          firstName: 'Francesco',
          lastName: 'Romano',
          fiscalCode: 'RMNFNC70E10H501V',
          email: 'francesco.romano@email.it',
          phone: '+39 333 5555555',
          birthDate: '1970-05-10',
          consents: ['DOCS', 'PRESCRIPTIONS'],
          hasActiveConsent: true
        },
        {
          id: 6,
          firstName: 'Elena',
          lastName: 'Colombo',
          fiscalCode: 'CLMLNE88F25H501U',
          email: 'elena.colombo@email.it',
          phone: '+39 333 6666666',
          birthDate: '1988-06-25',
          consents: [],
          hasActiveConsent: false
        },
        {
          id: 7,
          firstName: 'Marco',
          lastName: 'Ferrari',
          fiscalCode: 'FRRMRC82G30H501T',
          email: 'marco.ferrari@email.it',
          phone: '+39 333 7777777',
          birthDate: '1982-07-30',
          consents: [],
          hasActiveConsent: false
        },
        {
          id: 8,
          firstName: 'Sara',
          lastName: 'Conti',
          fiscalCode: 'CNTSRA95H05H501S',
          email: 'sara.conti@email.it',
          phone: '+39 333 8888888',
          birthDate: '1995-08-05',
          consents: ['DOCS', 'TELEVISIT'],
          lastAccess: '2025-01-27T11:15:00',
          hasActiveConsent: true
        }
      ];
      this.isLoading = false;
    }, 500);
  }

  get filteredPatients(): Patient[] {
    let filtered = this.patients;

    // Filtro per vista (con consenso / tutti)
    if (this.viewMode === 'consented') {
      filtered = filtered.filter(p => p.hasActiveConsent);
    }

    // Filtro per tipo consenso
    if (this.consentFilter !== 'ALL') {
      filtered = filtered.filter(p => p.consents.includes(this.consentFilter as ConsentScope));
    }

    // Filtro ricerca
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(p =>
        p.firstName.toLowerCase().includes(query) ||
        p.lastName.toLowerCase().includes(query) ||
        p.fiscalCode.toLowerCase().includes(query)
      );
    }

    return filtered;
  }

  get paginatedPatients(): Patient[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPatients.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredPatients.length / this.pageSize) || 1;
  }

  openPatientModal(patient: Patient): void {
    this.selectedPatient = patient;
    this.showPatientModal = true;
  }

  closePatientModal(): void {
    this.showPatientModal = false;
    this.selectedPatient = null;
  }

  getConsentLabel(scope: ConsentScope): string {
    const labels: Record<ConsentScope, string> = {
      DOCS: 'Documenti',
      PRESCRIPTIONS: 'Prescrizioni',
      TELEVISIT: 'Televisite'
    };
    return labels[scope];
  }

  getConsentBadgeClass(scope: ConsentScope): string {
    const classes: Record<ConsentScope, string> = {
      DOCS: 'bg-primary bg-opacity-10 text-primary',
      PRESCRIPTIONS: 'bg-success bg-opacity-10 text-success',
      TELEVISIT: 'bg-info bg-opacity-10 text-info'
    };
    return classes[scope];
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  calculateAge(birthDate: string): number {
    const birth = new Date(birthDate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  }
}

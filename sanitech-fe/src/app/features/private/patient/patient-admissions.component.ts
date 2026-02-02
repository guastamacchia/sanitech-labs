import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type AdmissionStatus = 'ACTIVE' | 'DISCHARGED' | 'SCHEDULED';
type AdmissionType = 'URGENTE' | 'PROGRAMMATO' | 'DAY_HOSPITAL';

interface AdmissionEvent {
  id: number;
  type: 'ADMISSION' | 'EXAM' | 'VISIT' | 'PROCEDURE' | 'DISCHARGE';
  title: string;
  description?: string;
  timestamp: string;
  doctorName?: string;
  documentId?: number;
}

interface Admission {
  id: number;
  department: string;
  admissionType: AdmissionType;
  status: AdmissionStatus;
  admittedAt: string;
  dischargedAt?: string;
  attendingDoctor: string;
  room?: string;
  bed?: string;
  diagnosis?: string;
  notes?: string;
  timeline?: AdmissionEvent[];
  documents?: { id: number; name: string; type: string }[];
}

@Component({
  selector: 'app-patient-admissions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-admissions.component.html'
})
export class PatientAdmissionsComponent implements OnInit {
  // Dati ricoveri
  admissions: Admission[] = [];

  // UI State
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showDetailModal = false;
  selectedAdmission: Admission | null = null;

  // Filtri
  statusFilter: 'ALL' | AdmissionStatus = 'ALL';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  ngOnInit(): void {
    this.loadAdmissions();
  }

  loadAdmissions(): void {
    this.isLoading = true;

    // Dati mock - Scenario di Francesca (accesso delegato per la madre)
    setTimeout(() => {
      this.admissions = [
        {
          id: 1,
          department: 'Cardiologia',
          admissionType: 'URGENTE',
          status: 'ACTIVE',
          admittedAt: '2025-01-28T14:30:00',
          attendingDoctor: 'Dr. Giovanni Martini',
          room: '304',
          bed: 'B',
          diagnosis: 'Fibrillazione atriale parossistica',
          notes: 'Paziente stabile, in monitoraggio continuo',
          timeline: [
            {
              id: 1,
              type: 'ADMISSION',
              title: 'Ammissione in reparto',
              description: 'Accesso tramite Pronto Soccorso per cardiopalmo',
              timestamp: '2025-01-28T14:30:00',
              doctorName: 'Dr. Giovanni Martini'
            },
            {
              id: 2,
              type: 'EXAM',
              title: 'ECG',
              description: 'Elettrocardiogramma di ingresso',
              timestamp: '2025-01-28T15:00:00'
            },
            {
              id: 3,
              type: 'EXAM',
              title: 'Esami ematochimici',
              description: 'Emocromo, elettroliti, funzionalita\' renale e tiroidea',
              timestamp: '2025-01-28T15:30:00'
            },
            {
              id: 4,
              type: 'VISIT',
              title: 'Visita cardiologica',
              description: 'Valutazione clinica e impostazione terapia antiaritmica',
              timestamp: '2025-01-28T16:00:00',
              doctorName: 'Dr. Giovanni Martini'
            },
            {
              id: 5,
              type: 'EXAM',
              title: 'Ecocardiogramma',
              description: 'Ecocardiogramma transtoracico',
              timestamp: '2025-01-29T09:00:00',
              doctorName: 'Dr. Elena Cuore'
            }
          ],
          documents: [
            { id: 1, name: 'Lettera di ammissione', type: 'ADMISSION' },
            { id: 2, name: 'Referto ECG', type: 'EXAM' },
            { id: 3, name: 'Esiti esami di laboratorio', type: 'EXAM' }
          ]
        },
        {
          id: 2,
          department: 'Medicina Interna',
          admissionType: 'PROGRAMMATO',
          status: 'DISCHARGED',
          admittedAt: '2024-11-10T08:00:00',
          dischargedAt: '2024-11-15T11:00:00',
          attendingDoctor: 'Dr. Marco Internista',
          room: '201',
          bed: 'A',
          diagnosis: 'Scompenso cardiaco cronico - accertamenti',
          documents: [
            { id: 4, name: 'Lettera di dimissione', type: 'DISCHARGE' },
            { id: 5, name: 'Indicazioni follow-up', type: 'DISCHARGE' }
          ]
        },
        {
          id: 3,
          department: 'Cardiologia',
          admissionType: 'DAY_HOSPITAL',
          status: 'DISCHARGED',
          admittedAt: '2024-09-05T07:30:00',
          dischargedAt: '2024-09-05T16:00:00',
          attendingDoctor: 'Dr. Giovanni Martini',
          diagnosis: 'Cardioversione elettrica programmata'
        }
      ];
      this.isLoading = false;
    }, 500);
  }

  get filteredAdmissions(): Admission[] {
    return this.admissions.filter(a => {
      if (this.statusFilter !== 'ALL' && a.status !== this.statusFilter) return false;
      return true;
    });
  }

  get paginatedAdmissions(): Admission[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredAdmissions.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredAdmissions.length / this.pageSize) || 1;
  }

  get activeAdmission(): Admission | null {
    return this.admissions.find(a => a.status === 'ACTIVE') || null;
  }

  get activeCount(): number {
    return this.admissions.filter(a => a.status === 'ACTIVE').length;
  }

  get dischargedCount(): number {
    return this.admissions.filter(a => a.status === 'DISCHARGED').length;
  }

  getStatusLabel(status: AdmissionStatus): string {
    const labels: Record<AdmissionStatus, string> = {
      ACTIVE: 'In corso',
      DISCHARGED: 'Dimesso',
      SCHEDULED: 'Programmato'
    };
    return labels[status];
  }

  getStatusBadgeClass(status: AdmissionStatus): string {
    const classes: Record<AdmissionStatus, string> = {
      ACTIVE: 'bg-success',
      DISCHARGED: 'bg-secondary',
      SCHEDULED: 'bg-primary'
    };
    return classes[status];
  }

  getTypeLabel(type: AdmissionType): string {
    const labels: Record<AdmissionType, string> = {
      URGENTE: 'Urgente',
      PROGRAMMATO: 'Programmato',
      DAY_HOSPITAL: 'Day Hospital'
    };
    return labels[type];
  }

  getTypeBadgeClass(type: AdmissionType): string {
    const classes: Record<AdmissionType, string> = {
      URGENTE: 'bg-danger',
      PROGRAMMATO: 'bg-info',
      DAY_HOSPITAL: 'bg-warning text-dark'
    };
    return classes[type];
  }

  getEventIcon(type: string): string {
    const icons: Record<string, string> = {
      ADMISSION: 'bi-door-open',
      EXAM: 'bi-clipboard2-pulse',
      VISIT: 'bi-person-badge',
      PROCEDURE: 'bi-activity',
      DISCHARGE: 'bi-box-arrow-right'
    };
    return icons[type] || 'bi-circle';
  }

  getEventColor(type: string): string {
    const colors: Record<string, string> = {
      ADMISSION: 'primary',
      EXAM: 'info',
      VISIT: 'success',
      PROCEDURE: 'warning',
      DISCHARGE: 'secondary'
    };
    return colors[type] || 'secondary';
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

  formatTime(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleTimeString('it-IT', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getDaysInHospital(admission: Admission): number {
    const start = new Date(admission.admittedAt);
    const end = admission.dischargedAt ? new Date(admission.dischargedAt) : new Date();
    const diff = end.getTime() - start.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  openDetailModal(admission: Admission): void {
    this.selectedAdmission = admission;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedAdmission = null;
  }

  downloadDocument(doc: { id: number; name: string; type: string }): void {
    this.successMessage = `Download di "${doc.name}" avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }
}

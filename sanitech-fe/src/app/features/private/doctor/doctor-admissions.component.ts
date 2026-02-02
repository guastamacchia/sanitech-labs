import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type AdmissionStatus = 'ACTIVE' | 'DISCHARGED' | 'TRANSFERRED';
type AdmissionType = 'EMERGENCY' | 'SCHEDULED' | 'TRANSFER';

interface DailyNote {
  date: string;
  note: string;
  author: string;
}

interface Admission {
  id: number;
  patientId: number;
  patientName: string;
  patientAge: number;
  admittedAt: string;
  dischargedAt?: string;
  type: AdmissionType;
  diagnosis: string;
  bed: string;
  status: AdmissionStatus;
  isReferent: boolean;
  dailyNotes: DailyNote[];
}

@Component({
  selector: 'app-doctor-admissions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-admissions.component.html'
})
export class DoctorAdmissionsComponent implements OnInit {
  // Ricoveri
  admissions: Admission[] = [];

  // Filtri
  statusFilter: 'ALL' | AdmissionStatus = 'ACTIVE';
  typeFilter: 'ALL' | AdmissionType = 'ALL';
  myPatientsOnly = true;

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // UI State
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showDetailModal = false;
  showDischargModal = false;
  selectedAdmission: Admission | null = null;

  // Form nuova nota
  newNote = '';

  // Form dimissione
  dischargeForm = {
    summary: '',
    followUp: '',
    medications: ''
  };

  // Stats
  get activeAdmissions(): number {
    return this.admissions.filter(a => a.status === 'ACTIVE').length;
  }

  get myActiveAdmissions(): number {
    return this.admissions.filter(a => a.status === 'ACTIVE' && a.isReferent).length;
  }

  get dischargedThisWeek(): number {
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    return this.admissions.filter(a =>
      a.status === 'DISCHARGED' &&
      a.dischargedAt &&
      new Date(a.dischargedAt) > weekAgo
    ).length;
  }

  get newAdmissionsToday(): number {
    const today = new Date().toDateString();
    return this.admissions.filter(a =>
      new Date(a.admittedAt).toDateString() === today
    ).length;
  }

  ngOnInit(): void {
    this.loadAdmissions();
  }

  loadAdmissions(): void {
    this.isLoading = true;

    setTimeout(() => {
      // Mock data - scenario Dott. Martini
      this.admissions = [
        {
          id: 1,
          patientId: 10,
          patientName: 'Greco Giovanni',
          patientAge: 68,
          admittedAt: new Date().toISOString(),
          type: 'SCHEDULED',
          diagnosis: 'Polmonite acquisita in comunità',
          bed: 'Letto 12A',
          status: 'ACTIVE',
          isReferent: true,
          dailyNotes: [
            { date: new Date().toISOString(), note: 'Ammissione. Iniziata terapia antibiotica ev. Richiesti esami ematochimici.', author: 'Dott. Martini' }
          ]
        },
        {
          id: 2,
          patientId: 11,
          patientName: 'Fontana Maria',
          patientAge: 75,
          admittedAt: this.getDaysAgo(2),
          type: 'EMERGENCY',
          diagnosis: 'Insufficienza respiratoria acuta',
          bed: 'Letto 8B',
          status: 'ACTIVE',
          isReferent: true,
          dailyNotes: [
            { date: this.getDaysAgo(2), note: 'Ammissione da PS. Ossigenoterapia ad alti flussi.', author: 'Dott. Martini' },
            { date: this.getDaysAgo(1), note: 'Miglioramento parametri respiratori. Riduzione FiO2.', author: 'Dott. Martini' },
            { date: new Date().toISOString(), note: 'Stabile. Iniziata fisioterapia respiratoria.', author: 'Dott. Martini' }
          ]
        },
        {
          id: 3,
          patientId: 12,
          patientName: 'Rizzo Paolo',
          patientAge: 55,
          admittedAt: this.getDaysAgo(5),
          type: 'SCHEDULED',
          diagnosis: 'BPCO riacutizzata',
          bed: 'Letto 5A',
          status: 'ACTIVE',
          isReferent: false,
          dailyNotes: [
            { date: this.getDaysAgo(5), note: 'Ammissione programmata per riacutizzazione.', author: 'Dott.ssa Neri' }
          ]
        },
        {
          id: 4,
          patientId: 13,
          patientName: 'Costa Anna',
          patientAge: 62,
          admittedAt: this.getDaysAgo(7),
          dischargedAt: this.getDaysAgo(1),
          type: 'SCHEDULED',
          diagnosis: 'Polmonite interstiziale',
          bed: 'Letto 3B',
          status: 'DISCHARGED',
          isReferent: true,
          dailyNotes: [
            { date: this.getDaysAgo(7), note: 'Ammissione. Avviata terapia steroidea.', author: 'Dott. Martini' },
            { date: this.getDaysAgo(1), note: 'Dimissione con terapia domiciliare.', author: 'Dott. Martini' }
          ]
        },
        {
          id: 5,
          patientId: 14,
          patientName: 'Moretti Luigi',
          patientAge: 71,
          admittedAt: this.getDaysAgo(10),
          dischargedAt: this.getDaysAgo(3),
          type: 'TRANSFER',
          diagnosis: 'Embolia polmonare',
          bed: 'Letto 10A',
          status: 'TRANSFERRED',
          isReferent: false,
          dailyNotes: [
            { date: this.getDaysAgo(10), note: 'Trasferimento da altro ospedale.', author: 'Dott.ssa Neri' },
            { date: this.getDaysAgo(3), note: 'Trasferito in cardiologia per follow-up.', author: 'Dott.ssa Neri' }
          ]
        }
      ];

      this.isLoading = false;
    }, 500);
  }

  getDaysAgo(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date.toISOString();
  }

  get filteredAdmissions(): Admission[] {
    let filtered = this.admissions;

    if (this.myPatientsOnly) {
      filtered = filtered.filter(a => a.isReferent);
    }

    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(a => a.status === this.statusFilter);
    }

    if (this.typeFilter !== 'ALL') {
      filtered = filtered.filter(a => a.type === this.typeFilter);
    }

    return filtered.sort((a, b) => new Date(b.admittedAt).getTime() - new Date(a.admittedAt).getTime());
  }

  get paginatedAdmissions(): Admission[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredAdmissions.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredAdmissions.length / this.pageSize) || 1;
  }

  openDetailModal(admission: Admission): void {
    this.selectedAdmission = admission;
    this.newNote = '';
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedAdmission = null;
  }

  takeCharge(admission: Admission): void {
    admission.isReferent = true;
    this.successMessage = `Hai preso in carico il paziente ${admission.patientName}.`;
    setTimeout(() => this.successMessage = '', 5000);
  }

  addDailyNote(): void {
    if (!this.selectedAdmission || !this.newNote.trim()) return;

    this.selectedAdmission.dailyNotes.push({
      date: new Date().toISOString(),
      note: this.newNote,
      author: 'Dott. Martini'
    });

    this.newNote = '';
    this.successMessage = 'Nota giornaliera aggiunta.';
    setTimeout(() => this.successMessage = '', 3000);
  }

  openDischargeModal(): void {
    this.dischargeForm = {
      summary: '',
      followUp: '',
      medications: ''
    };
    this.showDischargModal = true;
  }

  closeDischargeModal(): void {
    this.showDischargModal = false;
  }

  confirmDischarge(): void {
    if (!this.selectedAdmission) return;

    if (!this.dischargeForm.summary.trim()) {
      this.errorMessage = 'Inserisci un riepilogo del ricovero.';
      return;
    }

    this.isSaving = true;

    setTimeout(() => {
      this.selectedAdmission!.status = 'DISCHARGED';
      this.selectedAdmission!.dischargedAt = new Date().toISOString();
      this.selectedAdmission!.dailyNotes.push({
        date: new Date().toISOString(),
        note: `Dimissione. ${this.dischargeForm.summary}`,
        author: 'Dott. Martini'
      });

      this.isSaving = false;
      this.closeDischargeModal();
      this.closeDetailModal();
      this.successMessage = `Paziente ${this.selectedAdmission!.patientName} dimesso. Lettera di dimissione generata.`;
      setTimeout(() => this.successMessage = '', 5000);
    }, 1500);
  }

  getStatusLabel(status: AdmissionStatus): string {
    const labels: Record<AdmissionStatus, string> = {
      ACTIVE: 'Attivo',
      DISCHARGED: 'Dimesso',
      TRANSFERRED: 'Trasferito'
    };
    return labels[status];
  }

  getStatusBadgeClass(status: AdmissionStatus): string {
    const classes: Record<AdmissionStatus, string> = {
      ACTIVE: 'bg-success',
      DISCHARGED: 'bg-secondary',
      TRANSFERRED: 'bg-info'
    };
    return classes[status];
  }

  getTypeLabel(type: AdmissionType): string {
    const labels: Record<AdmissionType, string> = {
      EMERGENCY: 'Urgente',
      SCHEDULED: 'Programmato',
      TRANSFER: 'Trasferimento'
    };
    return labels[type];
  }

  getTypeBadgeClass(type: AdmissionType): string {
    const classes: Record<AdmissionType, string> = {
      EMERGENCY: 'bg-danger',
      SCHEDULED: 'bg-primary',
      TRANSFER: 'bg-warning text-dark'
    };
    return classes[type];
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

  getDaysInHospital(admission: Admission): number {
    const start = new Date(admission.admittedAt);
    const end = admission.dischargedAt ? new Date(admission.dischargedAt) : new Date();
    return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
  }

  isNewAdmission(admission: Admission): boolean {
    const today = new Date().toDateString();
    return new Date(admission.admittedAt).toDateString() === today;
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  DoctorApiService,
  AdmissionDto,
  PatientDto,
  AdmissionStatus as ApiAdmissionStatus,
  AdmissionType as ApiAdmissionType,
  DepartmentDto,
  AdmissionCreateDto
} from '../../../services/doctor-api.service';
import { AdmissionStatus, AdmissionType, DailyNote, Admission } from './dtos/admissions.dto';

@Component({
  selector: 'app-doctor-admissions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-admissions.component.html'
})
export class DoctorAdmissionsComponent implements OnInit {
  // Ricoveri
  admissions: Admission[] = [];

  // Cache pazienti
  private patientsCache = new Map<number, PatientDto>();

  // Filtri
  statusFilter: 'ALL' | AdmissionStatus = 'ACTIVE';
  typeFilter: 'ALL' | AdmissionType = 'ALL';
  myPatientsOnly = true;

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Stato UI
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

  // Modal proposta ricovero
  showAdmissionProposalModal = false;
  isSavingProposal = false;
  proposalErrorMessage = '';
  patientSearchQuery = '';
  patientSearchResults: PatientDto[] = [];
  isSearchingPatients = false;
  selectedPatient: PatientDto | null = null;
  departments: DepartmentDto[] = [];
  admissionProposalForm = {
    admissionType: '' as ApiAdmissionType | '',
    departmentCode: '',
    notes: ''
  };

  constructor(private doctorApi: DoctorApiService) {}

  // Statistiche
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
    this.errorMessage = '';

    const doctorId = this.doctorApi.getDoctorId();
    const statusParam = this.statusFilter !== 'ALL' ? this.statusFilter as ApiAdmissionStatus : undefined;

    this.doctorApi.listAdmissions({
      status: statusParam,
      size: 100
    }).subscribe({
      next: (page) => {
        const dtos = page.content || [];

        // Raccogli patientIds unici
        const patientIds = new Set<number>();
        dtos.forEach(dto => patientIds.add(dto.patientId));

        // Carica pazienti
        const patientFetches = Array.from(patientIds)
          .filter(id => !this.patientsCache.has(id))
          .map(id => this.doctorApi.getPatient(id).pipe(
            map(p => ({ id, patient: p })),
            catchError(() => of({ id, patient: null as PatientDto | null }))
          ));

        if (patientFetches.length > 0) {
          forkJoin(patientFetches).subscribe(results => {
            results.forEach(r => {
              if (r.patient) this.patientsCache.set(r.id, r.patient);
            });
            this.mapAdmissions(dtos, doctorId);
            this.isLoading = false;
          });
        } else {
          this.mapAdmissions(dtos, doctorId);
          this.isLoading = false;
        }
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei ricoveri.';
        this.isLoading = false;
      }
    });
  }

  private mapAdmissions(dtos: AdmissionDto[], doctorId: number | null): void {
    this.admissions = dtos.map(dto => {
      const patient = this.patientsCache.get(dto.patientId);

      // Calcola età paziente
      let patientAge = 0;
      if (patient?.birthDate) {
        const birth = new Date(patient.birthDate);
        const today = new Date();
        patientAge = today.getFullYear() - birth.getFullYear();
        const monthDiff = today.getMonth() - birth.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
          patientAge--;
        }
      }

      return {
        id: dto.id,
        patientId: dto.patientId,
        patientName: patient ? `${patient.lastName} ${patient.firstName}` : `Paziente ${dto.patientId}`,
        patientAge,
        admittedAt: dto.admittedAt,
        dischargedAt: dto.dischargedAt,
        type: this.mapAdmissionType(dto.admissionType),
        diagnosis: dto.notes || '-',
        bed: '-', // Il backend non ha questo campo
        status: dto.status as AdmissionStatus,
        isReferent: dto.attendingDoctorId === doctorId,
        dailyNotes: dto.notes ? [{ date: dto.admittedAt, note: dto.notes, author: '-' }] : []
      };
    });
  }

  private mapAdmissionType(apiType: ApiAdmissionType): AdmissionType {
    const mapping: Record<string, AdmissionType> = {
      EMERGENCY: 'EMERGENCY',
      SCHEDULED: 'SCHEDULED',
      TRANSFER: 'TRANSFER'
    };
    return mapping[apiType] || 'SCHEDULED';
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
    // Nota: il backend non ha un endpoint per questo, quindi aggiorniamo solo localmente
    admission.isReferent = true;
    this.successMessage = `Hai preso in carico il paziente ${admission.patientName}.`;
    setTimeout(() => this.successMessage = '', 5000);
  }

  addDailyNote(): void {
    if (!this.selectedAdmission || !this.newNote.trim()) return;

    // Nota: il backend non ha un endpoint per le note giornaliere, aggiorniamo localmente
    this.selectedAdmission.dailyNotes.push({
      date: new Date().toISOString(),
      note: this.newNote,
      author: 'Medico'
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
    this.errorMessage = '';

    this.doctorApi.dischargeAdmission(this.selectedAdmission.id).subscribe({
      next: (updated) => {
        this.isSaving = false;
        this.closeDischargeModal();
        this.closeDetailModal();
        this.loadAdmissions(); // Ricarica lista
        this.successMessage = `Paziente ${this.selectedAdmission!.patientName} dimesso. Lettera di dimissione generata.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Errore nella dimissione del paziente.';
      }
    });
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

  // --- Proposta Ricovero ---

  openAdmissionProposalModal(): void {
    this.showAdmissionProposalModal = true;
    this.proposalErrorMessage = '';
    this.patientSearchQuery = '';
    this.patientSearchResults = [];
    this.selectedPatient = null;
    this.admissionProposalForm = {
      admissionType: '',
      departmentCode: '',
      notes: ''
    };
    this.loadDepartments();
  }

  closeAdmissionProposalModal(): void {
    this.showAdmissionProposalModal = false;
  }

  loadDepartments(): void {
    this.doctorApi.listDepartments().subscribe({
      next: (depts) => {
        this.departments = depts;
      },
      error: () => {
        this.departments = [];
      }
    });
  }

  searchPatients(): void {
    if (!this.patientSearchQuery.trim()) return;

    this.isSearchingPatients = true;

    // Prima ottieni i pazienti che hanno dato consenso per prescrizioni o documenti
    forkJoin([
      this.doctorApi.getPatientsWithConsent('PRESCRIPTIONS'),
      this.doctorApi.getPatientsWithConsent('DOCS')
    ]).pipe(
      map(([prescriptionPatients, documentPatients]) => {
        // Unisci i due array rimuovendo duplicati
        return [...new Set([...prescriptionPatients, ...documentPatients])];
      })
    ).subscribe({
      next: (consentedPatientIds) => {
        // Poi cerca i pazienti e filtra solo quelli con consenso
        this.doctorApi.searchPatients({ q: this.patientSearchQuery, size: 50 }).subscribe({
          next: (page) => {
            const allPatients = page.content || [];
            // Filtra solo i pazienti che hanno dato consenso
            this.patientSearchResults = allPatients.filter(p =>
              p.id && consentedPatientIds.includes(p.id)
            ).slice(0, 10);
            this.isSearchingPatients = false;
          },
          error: () => {
            this.patientSearchResults = [];
            this.isSearchingPatients = false;
          }
        });
      },
      error: () => {
        this.patientSearchResults = [];
        this.isSearchingPatients = false;
      }
    });
  }

  selectPatient(patient: PatientDto): void {
    this.selectedPatient = patient;
    this.patientSearchResults = [];
    this.patientSearchQuery = '';
  }

  clearSelectedPatient(): void {
    this.selectedPatient = null;
  }

  submitAdmissionProposal(): void {
    this.proposalErrorMessage = '';

    if (!this.selectedPatient) {
      this.proposalErrorMessage = 'Seleziona un paziente.';
      return;
    }

    if (!this.admissionProposalForm.admissionType) {
      this.proposalErrorMessage = 'Seleziona il tipo di ricovero.';
      return;
    }

    if (!this.admissionProposalForm.departmentCode) {
      this.proposalErrorMessage = 'Seleziona il reparto.';
      return;
    }

    this.isSavingProposal = true;

    const dto: AdmissionCreateDto = {
      patientId: this.selectedPatient.id!,
      departmentCode: this.admissionProposalForm.departmentCode,
      admissionType: this.admissionProposalForm.admissionType as ApiAdmissionType,
      notes: this.admissionProposalForm.notes || undefined
    };

    this.doctorApi.createAdmission(dto).subscribe({
      next: () => {
        this.isSavingProposal = false;
        this.closeAdmissionProposalModal();
        this.loadAdmissions();
        this.successMessage = `Ricovero proposto per ${this.selectedPatient!.lastName} ${this.selectedPatient!.firstName}.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: () => {
        this.isSavingProposal = false;
        this.proposalErrorMessage = 'Errore nella creazione della proposta di ricovero.';
      }
    });
  }
}

import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import {
  DoctorApiService,
  AdmissionDto,
  PatientDto,
  DoctorDto,
  AdmissionStatus as ApiAdmissionStatus,
  AdmissionType as ApiAdmissionType,
  DepartmentDto,
  AdmissionCreateDto,
  AdmissionUpdateDto
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

  // Dati medico corrente (BUG-014: per autore nota)
  currentDoctorName = '';
  private currentDoctorId: number | null = null;

  // Filtri
  // BUG-005/006: statusFilter ora agisce solo lato client, carichiamo TUTTI i ricoveri
  statusFilter: 'ALL' | AdmissionStatus = 'ALL';
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

  // BUG-012: Chiudi modali con tasto Escape
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showDischargModal) {
      this.closeDischargeModal();
    } else if (this.showAdmissionProposalModal) {
      this.closeAdmissionProposalModal();
    } else if (this.showDetailModal) {
      this.closeDetailModal();
    }
  }

  // Statistiche
  // BUG-005: Le statistiche ora lavorano sull'intero dataset (non filtrato dal server)
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
    // BUG-014: Carica il nome del medico corrente
    this.currentDoctorId = this.doctorApi.getDoctorId();
    this.doctorApi.getCurrentDoctor().subscribe({
      next: (doctor) => {
        if (doctor) {
          this.currentDoctorName = `Dr. ${doctor.lastName} ${doctor.firstName}`;
        }
      }
    });
    this.loadAdmissions();
  }

  loadAdmissions(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const doctorId = this.doctorApi.getDoctorId();

    // BUG-005/006: Carichiamo TUTTI i ricoveri senza filtrare per status lato server.
    // Il filtro status viene applicato solo lato client nel getter filteredAdmissions.
    this.doctorApi.listAdmissions({
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

      // BUG-008: Calcolo età migliorato con fallback
      let patientAge = 0;
      if (patient?.birthDate) {
        const birth = new Date(patient.birthDate);
        if (!isNaN(birth.getTime())) {
          const today = new Date();
          patientAge = today.getFullYear() - birth.getFullYear();
          const monthDiff = today.getMonth() - birth.getMonth();
          if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
            patientAge--;
          }
        }
      }

      // BUG-008: Nome paziente migliorato con fallback leggibile
      const patientName = patient
        ? `${patient.lastName} ${patient.firstName}`
        : 'Paziente non disponibile';

      return {
        id: dto.id,
        patientId: dto.patientId,
        patientName,
        patientAge,
        admittedAt: dto.admittedAt,
        dischargedAt: dto.dischargedAt,
        // BUG-001: I tipi del backend sono già allineati, mapping diretto
        type: dto.admissionType as AdmissionType,
        diagnosis: dto.notes || '-',
        // BUG-007: Status backend è già allineato (ACTIVE/DISCHARGED/CANCELLED)
        status: dto.status as AdmissionStatus,
        isReferent: dto.attendingDoctorId === doctorId,
        attendingDoctorId: dto.attendingDoctorId ?? undefined,
        // BUG-014: Nota iniziale con autore corretto
        dailyNotes: dto.notes ? [{
          date: dto.admittedAt,
          note: dto.notes,
          author: dto.attendingDoctorId === doctorId ? (this.currentDoctorName || 'Medico referente') : 'Medico'
        }] : []
      };
    });
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

    // BUG-005/006: Filtro status solo lato client (non più doppio filtro)
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

  // BUG-012: Chiudi modal al click sul backdrop
  onDetailBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal')) {
      this.closeDetailModal();
    }
  }

  onProposalBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal')) {
      this.closeAdmissionProposalModal();
    }
  }

  onDischargeBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal')) {
      this.closeDischargeModal();
    }
  }

  // BUG-002: Prendi in carico - persiste con PATCH API
  takeCharge(admission: Admission): void {
    if (!this.currentDoctorId) {
      this.errorMessage = 'Impossibile identificare il medico corrente.';
      return;
    }

    this.isSaving = true;
    const dto: AdmissionUpdateDto = { attendingDoctorId: this.currentDoctorId };

    this.doctorApi.updateAdmission(admission.id, dto).subscribe({
      next: () => {
        admission.isReferent = true;
        admission.attendingDoctorId = this.currentDoctorId!;
        this.isSaving = false;
        this.successMessage = `Hai preso in carico il paziente ${admission.patientName}.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Errore nella presa in carico del paziente.';
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  // BUG-003: Note giornaliere - persiste con PATCH API (appende la nota alle notes esistenti)
  addDailyNote(): void {
    if (!this.selectedAdmission || !this.newNote.trim()) return;

    this.isSaving = true;
    const admission = this.selectedAdmission;

    // Costruisci le note concatenando le precedenti con la nuova
    const timestamp = new Date().toLocaleString('it-IT');
    const author = this.currentDoctorName || 'Medico';
    const newNoteEntry = `[${timestamp} - ${author}] ${this.newNote.trim()}`;

    // Recupera notes attuali dalla diagnosis (che mappa dto.notes)
    const existingNotes = admission.diagnosis !== '-' ? admission.diagnosis : '';
    const updatedNotes = existingNotes
      ? `${existingNotes}\n---\n${newNoteEntry}`
      : newNoteEntry;

    const dto: AdmissionUpdateDto = { notes: updatedNotes };

    this.doctorApi.updateAdmission(admission.id, dto).subscribe({
      next: () => {
        // BUG-014: Usa il nome reale del medico
        admission.dailyNotes.push({
          date: new Date().toISOString(),
          note: this.newNote.trim(),
          author: this.currentDoctorName || 'Medico'
        });
        admission.diagnosis = updatedNotes;

        this.newNote = '';
        this.isSaving = false;
        this.successMessage = 'Nota giornaliera aggiunta e salvata.';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Errore nel salvataggio della nota.';
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  openDischargeModal(): void {
    this.dischargeForm = {
      summary: '',
      followUp: '',
      medications: ''
    };
    this.errorMessage = '';
    this.showDischargModal = true;
  }

  closeDischargeModal(): void {
    this.showDischargModal = false;
  }

  // BUG-004: Lettera dimissione - salva i dati come notes prima della dimissione
  confirmDischarge(): void {
    if (!this.selectedAdmission) return;

    if (!this.dischargeForm.summary.trim()) {
      this.errorMessage = 'Inserisci un riepilogo del ricovero.';
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    // Componi la lettera di dimissione come notes
    const dischargeLetter = [
      `=== LETTERA DI DIMISSIONE ===`,
      `Riepilogo: ${this.dischargeForm.summary.trim()}`,
      this.dischargeForm.followUp?.trim() ? `Follow-up: ${this.dischargeForm.followUp.trim()}` : '',
      this.dischargeForm.medications?.trim() ? `Terapia domiciliare: ${this.dischargeForm.medications.trim()}` : '',
      `Data: ${new Date().toLocaleString('it-IT')}`,
      `Medico: ${this.currentDoctorName || 'Medico'}`
    ].filter(Boolean).join('\n');

    // Prima aggiorna le notes con la lettera di dimissione, poi scarica
    const updateDto: AdmissionUpdateDto = { notes: dischargeLetter };
    const admissionId = this.selectedAdmission.id;
    const patientName = this.selectedAdmission.patientName;

    this.doctorApi.updateAdmission(admissionId, updateDto).pipe(
      switchMap(() => this.doctorApi.dischargeAdmission(admissionId))
    ).subscribe({
      next: () => {
        this.isSaving = false;
        this.closeDischargeModal();
        this.closeDetailModal();
        this.loadAdmissions();
        this.successMessage = `Paziente ${patientName} dimesso. Lettera di dimissione salvata.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Errore nella dimissione del paziente.';
      }
    });
  }

  // BUG-001/007: Label allineati con i nuovi tipi backend
  getStatusLabel(status: AdmissionStatus): string {
    const labels: Record<AdmissionStatus, string> = {
      ACTIVE: 'Attivo',
      DISCHARGED: 'Dimesso',
      CANCELLED: 'Annullato'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: AdmissionStatus): string {
    const classes: Record<AdmissionStatus, string> = {
      ACTIVE: 'bg-success',
      DISCHARGED: 'bg-secondary',
      CANCELLED: 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
  }

  // BUG-001: Tipi ricovero allineati con backend
  getTypeLabel(type: AdmissionType): string {
    const labels: Record<AdmissionType, string> = {
      INPATIENT: 'Degenza ordinaria',
      DAY_HOSPITAL: 'Day Hospital',
      OBSERVATION: 'Osservazione'
    };
    return labels[type] || type;
  }

  getTypeBadgeClass(type: AdmissionType): string {
    const classes: Record<AdmissionType, string> = {
      INPATIENT: 'bg-primary',
      DAY_HOSPITAL: 'bg-info',
      OBSERVATION: 'bg-warning text-dark'
    };
    return classes[type] || 'bg-secondary';
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

  // BUG-008: Formatta l'età con gestione del caso "non disponibile"
  getPatientAgeDisplay(age: number): string {
    return age > 0 ? `${age} anni` : 'Età n.d.';
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

  // BUG-010: Pulisci errore alla selezione paziente
  selectPatient(patient: PatientDto): void {
    this.selectedPatient = patient;
    this.patientSearchResults = [];
    this.patientSearchQuery = '';
    this.proposalErrorMessage = '';
  }

  clearSelectedPatient(): void {
    this.selectedPatient = null;
  }

  // BUG-010: Pulisci errore al cambio dei campi del form
  onProposalFormChange(): void {
    if (this.proposalErrorMessage) {
      this.proposalErrorMessage = '';
    }
  }

  // BUG-013: Validazione simultanea (mostra TUTTI gli errori insieme)
  submitAdmissionProposal(): void {
    this.proposalErrorMessage = '';

    const errors: string[] = [];

    if (!this.selectedPatient) {
      errors.push('Seleziona un paziente');
    }
    if (!this.admissionProposalForm.admissionType) {
      errors.push('Seleziona il tipo di ricovero');
    }
    if (!this.admissionProposalForm.departmentCode) {
      errors.push('Seleziona il reparto');
    }

    if (errors.length > 0) {
      this.proposalErrorMessage = errors.join('. ') + '.';
      return;
    }

    this.isSavingProposal = true;

    // BUG-015: Include attendingDoctorId nella proposta
    const dto: AdmissionCreateDto = {
      patientId: this.selectedPatient!.id!,
      departmentCode: this.admissionProposalForm.departmentCode,
      admissionType: this.admissionProposalForm.admissionType as ApiAdmissionType,
      notes: this.admissionProposalForm.notes || undefined,
      attendingDoctorId: this.currentDoctorId ?? undefined
    };

    this.doctorApi.createAdmission(dto).subscribe({
      next: () => {
        this.isSavingProposal = false;
        this.closeAdmissionProposalModal();
        this.loadAdmissions();
        this.successMessage = `Ricovero proposto per ${this.selectedPatient!.lastName} ${this.selectedPatient!.firstName}.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        this.isSavingProposal = false;
        const detail = err?.error?.detail || err?.error?.message || '';
        this.proposalErrorMessage = detail
          ? `Errore nella creazione della proposta: ${detail}`
          : 'Errore nella creazione della proposta di ricovero.';
      }
    });
  }
}

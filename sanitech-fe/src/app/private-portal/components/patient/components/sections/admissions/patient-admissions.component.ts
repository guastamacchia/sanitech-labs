import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import {
  PatientService,
  AdmissionDto,
  AdmissionStatus,
  AdmissionType,
  AdmissionWithDetails,
  DocumentDto
} from '../../../services/patient.service';
import { DoctorDto, DepartmentDto } from '../../../services/scheduling.service';

@Component({
  selector: 'app-patient-admissions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-admissions.component.html'
})
export class PatientAdmissionsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Dati ricoveri
  admissions: AdmissionWithDetails[] = [];

  // Dati per arricchimento
  private doctors: DoctorDto[] = [];
  private departments: DepartmentDto[] = [];

  // Stato UI
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showDetailModal = false;
  selectedAdmission: AdmissionWithDetails | null = null;
  isDownloading = false;

  // Filtri
  statusFilter: 'ALL' | AdmissionStatus = 'ALL';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.loadAdmissions();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /** BUG-06: chiusura modale tramite tasto Escape */
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showDetailModal) {
      this.closeDetailModal();
    }
  }

  loadAdmissions(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      admissions: this.patientService.getMyAdmissions({ size: 100, sort: 'admittedAt,desc' }),
      enrichment: this.patientService.loadEnrichmentData()
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: ({ admissions, enrichment }) => {
        this.doctors = enrichment.doctors;
        this.departments = enrichment.departments;
        this.admissions = this.patientService.enrichAdmissions(
          admissions.content,
          this.doctors,
          this.departments
        );
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Errore caricamento ricoveri:', err);
        this.errorMessage = 'Impossibile caricare i ricoveri. Riprova.';
        this.isLoading = false;
      }
    });
  }

  get filteredAdmissions(): AdmissionWithDetails[] {
    return this.admissions.filter(a => {
      if (this.statusFilter !== 'ALL' && a.status !== this.statusFilter) return false;
      return true;
    });
  }

  get paginatedAdmissions(): AdmissionWithDetails[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredAdmissions.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredAdmissions.length / this.pageSize) || 1;
  }

  get activeAdmission(): AdmissionWithDetails | null {
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
      INPATIENT: 'bg-info',
      DAY_HOSPITAL: 'bg-warning text-dark',
      OBSERVATION: 'bg-danger'
    };
    return classes[type] || 'bg-info';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatTime(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleTimeString('it-IT', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getDaysInHospital(admission: AdmissionWithDetails): number {
    const start = new Date(admission.admittedAt);
    const end = admission.dischargedAt ? new Date(admission.dischargedAt) : new Date();
    const diff = end.getTime() - start.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  openDetailModal(admission: AdmissionWithDetails): void {
    this.selectedAdmission = admission;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedAdmission = null;
  }

  /** BUG-05: chiusura modale cliccando sul backdrop */
  onModalContainerClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.closeDetailModal();
    }
  }

  /** BUG-03/09: download reale della lettera di dimissione */
  downloadDischargeLetter(admission: AdmissionWithDetails): void {
    this.isDownloading = true;
    this.errorMessage = '';

    this.patientService.getDocuments({
      departmentCode: admission.departmentCode,
      documentType: 'LETTERA_DIMISSIONI',
      size: 100
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (docs) => {
        const doc = docs.content[0];
        if (!doc) {
          this.errorMessage = 'Nessuna lettera di dimissione disponibile per questo ricovero.';
          this.isDownloading = false;
          setTimeout(() => this.errorMessage = '', 5000);
          return;
        }
        this.patientService.downloadDocument(doc.id).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (blob) => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = doc.fileName || 'lettera_dimissioni.pdf';
            a.click();
            window.URL.revokeObjectURL(url);
            this.successMessage = 'Download della lettera di dimissione completato.';
            this.isDownloading = false;
            setTimeout(() => this.successMessage = '', 3000);
          },
          error: () => {
            this.errorMessage = 'Errore durante il download della lettera di dimissione.';
            this.isDownloading = false;
            setTimeout(() => this.errorMessage = '', 5000);
          }
        });
      },
      error: () => {
        this.errorMessage = 'Errore durante la ricerca dei documenti.';
        this.isDownloading = false;
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }
}

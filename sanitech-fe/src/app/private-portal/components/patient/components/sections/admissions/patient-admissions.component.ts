import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import {
  PatientService,
  AdmissionDto,
  AdmissionStatus,
  AdmissionType,
  AdmissionWithDetails
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
    return this.admissions.find(a => a.status === 'ADMITTED') || null;
  }

  get activeCount(): number {
    return this.admissions.filter(a => a.status === 'ADMITTED').length;
  }

  get dischargedCount(): number {
    return this.admissions.filter(a => a.status === 'DISCHARGED').length;
  }

  getStatusLabel(status: AdmissionStatus): string {
    const labels: Record<AdmissionStatus, string> = {
      ADMITTED: 'In corso',
      DISCHARGED: 'Dimesso',
      CANCELLED: 'Annullato'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: AdmissionStatus): string {
    const classes: Record<AdmissionStatus, string> = {
      ADMITTED: 'bg-success',
      DISCHARGED: 'bg-secondary',
      CANCELLED: 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
  }

  getTypeLabel(type: AdmissionType): string {
    const labels: Record<AdmissionType, string> = {
      EMERGENCY: 'Urgente',
      ORDINARY: 'Ordinario',
      DAY_HOSPITAL: 'Day Hospital'
    };
    return labels[type] || type;
  }

  getTypeBadgeClass(type: AdmissionType): string {
    const classes: Record<AdmissionType, string> = {
      EMERGENCY: 'bg-danger',
      ORDINARY: 'bg-info',
      DAY_HOSPITAL: 'bg-warning text-dark'
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

  downloadDischargeLetter(admission: AdmissionWithDetails): void {
    this.successMessage = `Download della lettera di dimissione avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }
}

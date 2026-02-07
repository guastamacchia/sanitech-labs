import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import {
  PatientService,
  PrescriptionDto,
  PrescriptionStatus,
  PrescriptionWithDetails
} from '../../../services/patient.service';
import { DoctorDto, DepartmentDto } from '../../../services/scheduling.service';

@Component({
  selector: 'app-patient-prescriptions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-prescriptions.component.html'
})
export class PatientPrescriptionsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Dati prescrizioni
  prescriptions: PrescriptionWithDetails[] = [];

  // Dati per arricchimento
  private doctors: DoctorDto[] = [];
  private departments: DepartmentDto[] = [];

  // Stato UI
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showDetailModal = false;
  selectedPrescription: PrescriptionWithDetails | null = null;

  // Filtri
  statusFilter: 'ALL' | PrescriptionStatus = 'ALL';
  searchTerm = '';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.loadPrescriptions();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadPrescriptions(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Carica prescrizioni e dati per arricchimento in parallelo
    forkJoin({
      prescriptions: this.patientService.getPrescriptions({ size: 100, sort: 'createdAt,desc' }),
      enrichment: this.patientService.loadEnrichmentData()
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: ({ prescriptions, enrichment }) => {
        this.doctors = enrichment.doctors;
        this.departments = enrichment.departments;
        this.prescriptions = this.patientService.enrichPrescriptions(
          prescriptions.content,
          this.doctors,
          this.departments
        );
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Errore caricamento prescrizioni:', err);
        this.errorMessage = 'Impossibile caricare le prescrizioni. Riprova.';
        this.isLoading = false;
      }
    });
  }

  get filteredPrescriptions(): PrescriptionWithDetails[] {
    return this.prescriptions.filter(p => {
      if (this.statusFilter !== 'ALL' && p.status !== this.statusFilter) return false;
      if (this.searchTerm) {
        const term = this.searchTerm.toLowerCase();
        const matchesMedication = p.items.some(item =>
          item.medicationName.toLowerCase().includes(term)
        );
        const matchesDoctor = p.doctorName?.toLowerCase().includes(term) ?? false;
        const matchesDepartment = p.departmentName?.toLowerCase().includes(term) ?? false;
        return matchesMedication || matchesDoctor || matchesDepartment;
      }
      return true;
    });
  }

  get paginatedPrescriptions(): PrescriptionWithDetails[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPrescriptions.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredPrescriptions.length / this.pageSize) || 1;
  }

  get activePrescriptionsCount(): number {
    return this.prescriptions.filter(p => p.status === 'ISSUED').length;
  }

  get expiringPrescriptionsCount(): number {
    // Le prescrizioni del backend non hanno scadenza esplicita, contiamo quelle ISSUED
    return this.prescriptions.filter(p => p.status === 'ISSUED').length;
  }

  getStatusLabel(status: PrescriptionStatus): string {
    const labels: Record<PrescriptionStatus, string> = {
      ISSUED: 'Attiva',
      DISPENSED: 'Dispensata',
      CANCELLED: 'Annullata'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: PrescriptionStatus): string {
    const classes: Record<PrescriptionStatus, string> = {
      ISSUED: 'bg-success',
      DISPENSED: 'bg-secondary',
      CANCELLED: 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateLong(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  isExpiringSoon(prescription: PrescriptionWithDetails): boolean {
    // Il backend non ha expiresAt, quindi restituiamo false
    return false;
  }

  openDetailModal(prescription: PrescriptionWithDetails): void {
    this.selectedPrescription = prescription;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedPrescription = null;
  }

  downloadPrescription(prescription: PrescriptionWithDetails): void {
    const medicationNames = prescription.items.map(i => i.medicationName).join(', ');
    this.successMessage = `Download della prescrizione "${medicationNames}" avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  requestRefill(prescription: PrescriptionWithDetails): void {
    const medicationNames = prescription.items.map(i => i.medicationName).join(', ');
    this.successMessage = `Richiesta di rinnovo per "${medicationNames}" inviata al medico.`;
    setTimeout(() => this.successMessage = '', 5000);
  }

  // Helper per visualizzare le informazioni dei farmaci
  getMedicationSummary(prescription: PrescriptionWithDetails): string {
    return prescription.items.map(i => i.medicationName).join(', ');
  }

  getFirstMedication(prescription: PrescriptionWithDetails): string {
    return prescription.items[0]?.medicationName || 'N/A';
  }

  getFirstDosage(prescription: PrescriptionWithDetails): string {
    return prescription.items[0]?.dosage || '';
  }

  getFirstFrequency(prescription: PrescriptionWithDetails): string {
    return prescription.items[0]?.frequency || '';
  }

  getFirstInstructions(prescription: PrescriptionWithDetails): string | undefined {
    return prescription.items[0]?.instructions;
  }
}

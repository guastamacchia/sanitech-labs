import { Component, OnInit, OnDestroy, HostListener, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import {
  PatientService,
  PrescriptionDto,
  PrescriptionStatus,
  PrescriptionWithDetails,
  PagedResponse
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
  private enrichmentLoaded = false;

  // Stato UI
  isLoading = false;
  errorMessage = '';
  showDetailModal = false;
  selectedPrescription: PrescriptionWithDetails | null = null;

  // Filtri
  statusFilter: 'ALL' | PrescriptionStatus = 'ALL';
  searchTerm = '';

  // Paginazione (client-side su dati caricati)
  pageSize = 10;
  currentPage = 1;

  // Contatori reali dal backend
  totalElements = 0;

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.loadEnrichmentAndPrescriptions();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ---- ESCAPE key per chiudere modale (PRESC-08) ----
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showDetailModal) {
      this.closeDetailModal();
    }
  }

  /**
   * Carica enrichment e poi prescrizioni.
   * Gli errori vengono propagati all'utente (PRESC-05).
   */
  loadEnrichmentAndPrescriptions(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.patientService.loadEnrichmentData()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (enrichment) => {
          this.doctors = enrichment.doctors;
          this.departments = enrichment.departments;
          this.enrichmentLoaded = true;
          this.loadPrescriptions();
        },
        error: () => {
          this.errorMessage = 'Impossibile caricare i dati di supporto. Riprova.';
          this.isLoading = false;
        }
      });
  }

  /**
   * Carica le prescrizioni. NON usa catchError silenzioso (PRESC-05).
   * Usa size: 100 con paginazione client-side (PRESC-07: mitigato con warning).
   */
  loadPrescriptions(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.patientService.getPrescriptions({ size: 100, sort: 'createdAt,desc' })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: PagedResponse<PrescriptionDto>) => {
          this.totalElements = response.totalElements;
          this.prescriptions = this.patientService.enrichPrescriptions(
            response.content,
            this.doctors,
            this.departments
          );
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Impossibile caricare le prescrizioni. Riprova.';
          this.isLoading = false;
        }
      });
  }

  get filteredPrescriptions(): PrescriptionWithDetails[] {
    return this.prescriptions.filter(p => {
      // PRESC-06: Nascondi prescrizioni DRAFT lato frontend (safety-net)
      if (p.status === 'DRAFT') return false;
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

  /**
   * Pagine visibili con ellipsis (PRESC-12).
   * Restituisce array di numeri pagina e -1 per ellipsis.
   */
  get visiblePages(): number[] {
    const total = this.totalPages;
    const current = this.currentPage;

    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }

    const pages: number[] = [1];

    if (current > 3) {
      pages.push(-1); // ellipsis
    }

    const start = Math.max(2, current - 1);
    const end = Math.min(total - 1, current + 1);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    if (current < total - 2) {
      pages.push(-1); // ellipsis
    }

    if (pages[pages.length - 1] !== total) {
      pages.push(total);
    }

    return pages;
  }

  get activePrescriptionsCount(): number {
    return this.prescriptions.filter(p => p.status === 'ISSUED').length;
  }

  // PRESC-04: Card "Annullate" al posto di "Da rinnovare" (che era duplicato)
  get cancelledPrescriptionsCount(): number {
    return this.prescriptions.filter(p => p.status === 'CANCELLED').length;
  }

  // PRESC-01: Allineato con backend (DRAFT, ISSUED, CANCELLED)
  getStatusLabel(status: PrescriptionStatus): string {
    const labels: Record<PrescriptionStatus, string> = {
      DRAFT: 'Bozza',
      ISSUED: 'Attiva',
      CANCELLED: 'Annullata'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: PrescriptionStatus): string {
    const classes: Record<PrescriptionStatus, string> = {
      DRAFT: 'bg-warning text-dark',
      ISSUED: 'bg-success',
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

  openDetailModal(prescription: PrescriptionWithDetails): void {
    this.selectedPrescription = prescription;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedPrescription = null;
  }

  // PRESC-02: Rimosso download fake — ora bottone disabilitato con tooltip

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

import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { forkJoin, of, Subject } from 'rxjs';
import { catchError, map, switchMap, takeUntil } from 'rxjs/operators';
import {
  DoctorApiService,
  PrescriptionDto,
  PatientDto,
  PrescriptionStatus as ApiPrescriptionStatus,
  ConsentScope
} from '../../../services/doctor-api.service';
import { PrescriptionStatus, Medication, Prescription, PrescriptionForm, DrugSuggestion } from './dtos/prescriptions.dto';

@Component({
  selector: 'app-doctor-prescriptions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-prescriptions.component.html'
})
export class DoctorPrescriptionsComponent implements OnInit, OnDestroy {
  // Lifecycle
  private destroy$ = new Subject<void>();

  // Prescrizioni
  prescriptions: Prescription[] = [];

  // Pazienti con consenso PRESCRIPTIONS
  patients: { id: number; name: string }[] = [];

  // Cache pazienti
  private patientsCache = new Map<number, PatientDto>();

  // Database farmaci (statico - potrebbe essere un'API in futuro)
  drugDatabase: DrugSuggestion[] = [
    { name: 'Ramipril', dosages: ['2.5mg', '5mg', '10mg'] },
    { name: 'Bisoprololo', dosages: ['1.25mg', '2.5mg', '5mg', '10mg'] },
    { name: 'Aspirina', dosages: ['100mg', '300mg'] },
    { name: 'Atorvastatina', dosages: ['10mg', '20mg', '40mg', '80mg'] },
    { name: 'Metformina', dosages: ['500mg', '850mg', '1000mg'] },
    { name: 'Omeprazolo', dosages: ['10mg', '20mg', '40mg'] },
    { name: 'Amlodipina', dosages: ['5mg', '10mg'] },
    { name: 'Furosemide', dosages: ['25mg', '50mg', '100mg'] }
  ];

  constructor(private doctorApi: DoctorApiService, private route: ActivatedRoute) {}

  // Suggerimenti filtrati
  filteredDrugs: DrugSuggestion[] = [];
  showDrugSuggestions = false;
  currentMedicationIndex = -1;

  // Form nuova prescrizione
  prescriptionForm: PrescriptionForm = {
    patientId: null,
    medications: [],
    notes: ''
  };

  // Filtri
  searchQuery = '';
  statusFilter: 'ALL' | PrescriptionStatus = 'ALL';
  patientFilter = 0;

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Stato UI
  isLoading = false;
  isSaving = false;
  isCancelling = false;
  successMessage = '';
  errorMessage = '';
  showNewPrescriptionModal = false;
  showPreviewModal = false;
  selectedPrescription: Prescription | null = null;
  showCancelConfirm = false;
  cancelTarget: Prescription | null = null;

  // Statistiche
  get totalPrescriptions(): number {
    return this.prescriptions.length;
  }

  get issuedThisMonth(): number {
    const now = new Date();
    const firstOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    return this.prescriptions.filter(p =>
      p.status === 'ISSUED' && p.issuedAt && new Date(p.issuedAt) >= firstOfMonth
    ).length;
  }

  get activePrescriptions(): number {
    return this.prescriptions.filter(p => p.status === 'ISSUED').length;
  }

  ngOnInit(): void {
    const patientIdParam = this.route.snapshot.queryParamMap.get('patientId');
    if (patientIdParam) {
      this.patientFilter = +patientIdParam;
    }
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /** Chiude i modali premendo ESC */
  @HostListener('document:keydown.escape')
  onEscapePress(): void {
    if (this.showCancelConfirm) {
      this.closeCancelConfirm();
    } else if (this.showPreviewModal) {
      this.closePreview();
    } else if (this.showNewPrescriptionModal) {
      this.closeNewPrescriptionModal();
    } else if (this.selectedPrescription) {
      this.closePrescriptionDetail();
    }
  }

  loadData(): void {
    this.isLoading = true;
    this.clearMessages();

    // BUG-02 FIX: guard departmentCode null anche in loadData
    const departmentCode = this.doctorApi.getDepartmentCode();
    if (!departmentCode) {
      this.errorMessage = 'Reparto non configurato. Contattare l\'amministratore di sistema.';
      this.isLoading = false;
      return;
    }

    // BUG-17 FIX: usa takeUntil per cancellare subscription su destroy
    this.doctorApi.getPatientsWithConsent('PRESCRIPTIONS').pipe(
      takeUntil(this.destroy$),
      switchMap(patientIds => {
        if (patientIds.length === 0) {
          return of({ patientsWithConsent: [] as { id: number; name: string }[] });
        }

        const patientFetches = patientIds.map(id =>
          this.doctorApi.getPatient(id).pipe(
            map(patient => {
              this.patientsCache.set(patient.id, patient);
              return {
                id: patient.id,
                name: `${patient.lastName} ${patient.firstName}`
              };
            }),
            catchError(() => of(null))
          )
        );

        return forkJoin(patientFetches).pipe(
          map(results => ({
            patientsWithConsent: results.filter((p): p is { id: number; name: string } => p !== null)
          }))
        );
      }),
      // BUG-17 FIX: usa switchMap per il caricamento prescrizioni (annulla precedenti)
      switchMap(({ patientsWithConsent }) => {
        this.patients = patientsWithConsent;

        if (patientsWithConsent.length === 0) {
          return of([] as PrescriptionDto[]);
        }

        const prescriptionFetches = patientsWithConsent.map(patient =>
          this.doctorApi.listPrescriptions({
            patientId: patient.id,
            departmentCode: departmentCode,
            size: 50
          }).pipe(
            map(page => page.content || []),
            catchError(() => of([] as PrescriptionDto[]))
          )
        );

        return forkJoin(prescriptionFetches).pipe(
          map(arrays => arrays.flat())
        );
      })
    ).subscribe({
      next: (allPrescriptions) => {
        this.prescriptions = allPrescriptions.map(p => this.mapPrescription(p));
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei dati.';
        this.isLoading = false;
      }
    });
  }

  private mapPrescription(dto: PrescriptionDto): Prescription {
    const patient = this.patientsCache.get(dto.patientId);

    // Calcola data scadenza (issuedAt + max duration dei farmaci)
    const maxDuration = dto.items.reduce((max, item) => Math.max(max, item.durationDays ?? 30), 30);
    const issuedDate = dto.issuedAt ? new Date(dto.issuedAt) : new Date(dto.createdAt);
    const expiryDate = new Date(issuedDate);
    expiryDate.setDate(expiryDate.getDate() + maxDuration);

    // BUG-15 FIX: validazione status con fallback
    const validStatuses: PrescriptionStatus[] = ['DRAFT', 'ISSUED', 'CANCELLED'];
    const mappedStatus: PrescriptionStatus = validStatuses.includes(dto.status as PrescriptionStatus)
      ? (dto.status as PrescriptionStatus)
      : 'DRAFT';

    return {
      id: dto.id,
      patientId: dto.patientId,
      patientName: patient ? `${patient.lastName} ${patient.firstName}` : `Paziente ${dto.patientId}`,
      medications: dto.items.map(item => ({
        name: item.medicationName,
        dosage: item.dosage,
        posology: item.frequency,
        duration: item.durationDays ?? 30,
        notes: item.instructions || ''
      })),
      issuedAt: dto.issuedAt || dto.createdAt,
      expiresAt: expiryDate.toISOString().split('T')[0],
      status: mappedStatus,
      notes: dto.notes || ''
    };
  }

  // --- Helper per ottenere il nome del paziente selezionato nel form ---
  getSelectedPatientName(): string {
    if (!this.prescriptionForm.patientId) return '';
    const patient = this.patients.find(p => p.id === this.prescriptionForm.patientId);
    return patient?.name || '';
  }

  get filteredPrescriptions(): Prescription[] {
    let filtered = this.prescriptions;

    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(p => p.status === this.statusFilter);
    }

    if (this.patientFilter > 0) {
      filtered = filtered.filter(p => p.patientId === this.patientFilter);
    }

    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(p =>
        p.patientName.toLowerCase().includes(query) ||
        p.medications.some(m => m.name.toLowerCase().includes(query))
      );
    }

    return filtered.sort((a, b) => new Date(b.issuedAt).getTime() - new Date(a.issuedAt).getTime());
  }

  get paginatedPrescriptions(): Prescription[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPrescriptions.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredPrescriptions.length / this.pageSize) || 1;
  }

  // BUG-21 FIX: paginazione con finestra (max 7 bottoni)
  get visiblePages(): number[] {
    const total = this.totalPages;
    const current = this.currentPage;
    const maxVisible = 7;
    if (total <= maxVisible) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    const half = Math.floor(maxVisible / 2);
    let start = Math.max(1, current - half);
    let end = start + maxVisible - 1;
    if (end > total) {
      end = total;
      start = Math.max(1, end - maxVisible + 1);
    }
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  openNewPrescriptionModal(): void {
    this.prescriptionForm = {
      patientId: null,
      medications: [],
      notes: ''
    };
    this.addMedication();
    this.errorMessage = '';
    this.showNewPrescriptionModal = true;
  }

  closeNewPrescriptionModal(): void {
    this.showNewPrescriptionModal = false;
    this.errorMessage = '';
  }

  addMedication(): void {
    this.prescriptionForm.medications.push({
      name: '',
      dosage: '',
      posology: '',
      duration: 30,
      notes: ''
    });
  }

  // BUG-22 FIX: guard minimo 1 farmaco
  removeMedication(index: number): void {
    if (this.prescriptionForm.medications.length <= 1) return;
    this.prescriptionForm.medications.splice(index, 1);
  }

  searchDrug(query: string, index: number): void {
    this.currentMedicationIndex = index;
    if (query.length < 2) {
      this.filteredDrugs = [];
      this.showDrugSuggestions = false;
      return;
    }

    this.filteredDrugs = this.drugDatabase.filter(d =>
      d.name.toLowerCase().includes(query.toLowerCase())
    );
    this.showDrugSuggestions = this.filteredDrugs.length > 0;
  }

  selectDrug(drug: DrugSuggestion): void {
    if (this.currentMedicationIndex >= 0) {
      this.prescriptionForm.medications[this.currentMedicationIndex].name = drug.name;
      this.prescriptionForm.medications[this.currentMedicationIndex].dosage = '';
    }
    this.showDrugSuggestions = false;
  }

  // BUG-12 FIX: chiudi suggerimenti con delay per permettere mousedown
  hideDrugSuggestions(): void {
    setTimeout(() => {
      this.showDrugSuggestions = false;
    }, 200);
  }

  getDosagesForMedication(index: number): string[] {
    const medName = this.prescriptionForm.medications[index]?.name;
    const drug = this.drugDatabase.find(d => d.name === medName);
    return drug?.dosages || [];
  }

  openPreview(): void {
    if (!this.validateForm()) return;
    this.showPreviewModal = true;
  }

  closePreview(): void {
    this.showPreviewModal = false;
  }

  // BUG-08 FIX: clear error on change
  onFormChange(): void {
    if (this.errorMessage) {
      this.errorMessage = '';
    }
  }

  validateForm(): boolean {
    if (!this.prescriptionForm.patientId) {
      this.errorMessage = 'Seleziona un paziente.';
      return false;
    }
    if (this.prescriptionForm.medications.length === 0) {
      this.errorMessage = 'Aggiungi almeno un farmaco.';
      return false;
    }
    for (const med of this.prescriptionForm.medications) {
      if (!med.name.trim() || !med.dosage || !med.posology.trim()) {
        this.errorMessage = 'Completa tutti i campi obbligatori per ogni farmaco.';
        return false;
      }
    }
    this.errorMessage = '';
    return true;
  }

  issuePrescription(): void {
    if (!this.validateForm()) return;

    const departmentCode = this.doctorApi.getDepartmentCode();
    if (!departmentCode) {
      this.errorMessage = 'Reparto non configurato.';
      return;
    }

    this.isSaving = true;
    this.clearMessages();

    const patient = this.patients.find(p => p.id === this.prescriptionForm.patientId);

    this.doctorApi.createPrescription({
      patientId: this.prescriptionForm.patientId!,
      departmentCode,
      notes: this.prescriptionForm.notes,
      items: this.prescriptionForm.medications.map((med, index) => ({
        medicationName: med.name,
        dosage: med.dosage,
        frequency: med.posology,
        durationDays: med.duration,
        instructions: med.notes,
        sortOrder: index
      }))
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.isSaving = false;
        this.showPreviewModal = false;
        this.closeNewPrescriptionModal();
        this.loadData();
        this.successMessage = `Prescrizione emessa con successo. Il paziente ${patient?.name} riceverà una notifica.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        this.isSaving = false;
        this.errorMessage = err.error?.detail || 'Errore nella creazione della prescrizione.';
      }
    });
  }

  // BUG-09 FIX: cancel prescription UI
  openCancelConfirm(prescription: Prescription): void {
    this.cancelTarget = prescription;
    this.showCancelConfirm = true;
  }

  closeCancelConfirm(): void {
    this.showCancelConfirm = false;
    this.cancelTarget = null;
  }

  confirmCancelPrescription(): void {
    if (!this.cancelTarget) return;
    this.isCancelling = true;
    this.clearMessages();

    this.doctorApi.cancelPrescription(this.cancelTarget.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        const cancelledId = this.cancelTarget?.id;
        this.isCancelling = false;
        this.closeCancelConfirm();
        this.closePrescriptionDetail();
        this.loadData();
        this.successMessage = `Prescrizione #${cancelledId} annullata con successo.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        this.isCancelling = false;
        this.errorMessage = err.error?.detail || 'Errore nell\'annullamento della prescrizione.';
      }
    });
  }

  duplicatePrescription(prescription: Prescription): void {
    this.prescriptionForm = {
      patientId: prescription.patientId,
      medications: prescription.medications.map(m => ({ ...m })),
      notes: prescription.notes
    };
    this.errorMessage = '';
    this.showNewPrescriptionModal = true;
    this.successMessage = 'Prescrizione duplicata. Modifica i dati se necessario e conferma.';
    setTimeout(() => this.successMessage = '', 3000);
  }

  viewPrescription(prescription: Prescription): void {
    this.selectedPrescription = prescription;
  }

  closePrescriptionDetail(): void {
    this.selectedPrescription = null;
  }

  // BUG-05 FIX: implementazione reale download PDF (placeholder che genera un blob testuale)
  downloadPdf(prescription: Prescription): void {
    // Genera un riepilogo testuale come PDF placeholder
    const content = this.generatePrescriptionText(prescription);
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `prescrizione_${prescription.id}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);

    this.successMessage = `Download prescrizione #${prescription.id} avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  private generatePrescriptionText(prescription: Prescription): string {
    const lines: string[] = [
      '==================================================',
      '            PRESCRIZIONE MEDICA',
      '==================================================',
      '',
      `Prescrizione N.: ${prescription.id}`,
      `Paziente: ${prescription.patientName}`,
      `Data emissione: ${this.formatDateTime(prescription.issuedAt)}`,
      `Scadenza: ${this.formatDate(prescription.expiresAt)}`,
      `Stato: ${this.getStatusLabel(prescription.status)}`,
      '',
      '--- FARMACI PRESCRITTI ---',
      ''
    ];

    prescription.medications.forEach((med, i) => {
      lines.push(`${i + 1}. ${med.name} ${med.dosage}`);
      lines.push(`   Posologia: ${med.posology}`);
      lines.push(`   Durata: ${med.duration} giorni`);
      if (med.notes) {
        lines.push(`   Note: ${med.notes}`);
      }
      lines.push('');
    });

    if (prescription.notes) {
      lines.push('--- NOTE ---');
      lines.push(prescription.notes);
      lines.push('');
    }

    lines.push('==================================================');
    lines.push('Documento generato dal sistema Sanitech');
    return lines.join('\n');
  }

  getStatusLabel(status: PrescriptionStatus): string {
    const labels: Record<string, string> = {
      DRAFT: 'Bozza',
      ISSUED: 'Emessa',
      CANCELLED: 'Annullata'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: PrescriptionStatus): string {
    const classes: Record<string, string> = {
      DRAFT: 'bg-warning text-dark',
      ISSUED: 'bg-success',
      CANCELLED: 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
  }

  // BUG-23 FIX: clear entrambi i messaggi
  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '-';
    return d.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(dateStr: string): string {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '-';
    return d.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}

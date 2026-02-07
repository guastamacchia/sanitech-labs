import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
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
export class DoctorPrescriptionsComponent implements OnInit {
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

  constructor(private doctorApi: DoctorApiService) {}

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
  successMessage = '';
  errorMessage = '';
  showNewPrescriptionModal = false;
  showPreviewModal = false;
  selectedPrescription: Prescription | null = null;

  // Statistiche
  get totalPrescriptions(): number {
    return this.prescriptions.length;
  }

  get issuedThisMonth(): number {
    const now = new Date();
    const firstOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    return this.prescriptions.filter(p =>
      p.status === 'ISSUED' && new Date(p.issuedAt) >= firstOfMonth
    ).length;
  }

  get activePrescriptions(): number {
    return this.prescriptions.filter(p => p.status === 'ISSUED').length;
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Carica direttamente i pazienti con consenso PRESCRIPTIONS attivo per il medico
    this.doctorApi.getPatientsWithConsent('PRESCRIPTIONS').pipe(
      switchMap(patientIds => {
        if (patientIds.length === 0) {
          return of({ patientsWithConsent: [] as { id: number; name: string }[] });
        }

        // Carica i dettagli dei pazienti
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
      })
    ).subscribe({
      next: ({ patientsWithConsent }) => {
        this.patients = patientsWithConsent;

        // Carica prescrizioni per ogni paziente con consenso
        if (patientsWithConsent.length === 0) {
          this.prescriptions = [];
          this.isLoading = false;
          return;
        }

        const departmentCode = this.doctorApi.getDepartmentCode() || '';
        const prescriptionFetches = patientsWithConsent.map(patient =>
          this.doctorApi.listPrescriptions({
            patientId: patient.id,
            departmentCode,
            size: 50
          }).pipe(
            map(page => page.content || []),
            catchError(() => of([] as PrescriptionDto[]))
          )
        );

        forkJoin(prescriptionFetches).subscribe({
          next: (prescriptionArrays) => {
            const allPrescriptions = prescriptionArrays.flat();
            this.prescriptions = allPrescriptions.map(p => this.mapPrescription(p));
            this.isLoading = false;
          },
          error: () => {
            this.errorMessage = 'Errore nel caricamento delle prescrizioni.';
            this.isLoading = false;
          }
        });
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei dati.';
        this.isLoading = false;
      }
    });
  }

  private mapPrescription(dto: PrescriptionDto): Prescription {
    const patient = this.patientsCache.get(dto.patientId);

    // Calcola data scadenza (issuedAt + max duration)
    const maxDuration = dto.items.reduce((max, item) => Math.max(max, item.durationDays || 30), 30);
    const issuedDate = dto.issuedAt ? new Date(dto.issuedAt) : new Date(dto.createdAt);
    const expiryDate = new Date(issuedDate);
    expiryDate.setDate(expiryDate.getDate() + maxDuration);

    return {
      id: dto.id,
      patientId: dto.patientId,
      patientName: patient ? `${patient.lastName} ${patient.firstName}` : `Paziente ${dto.patientId}`,
      medications: dto.items.map(item => ({
        name: item.medicationName,
        dosage: item.dosage,
        posology: item.frequency,
        duration: item.durationDays || 30,
        notes: item.instructions || ''
      })),
      issuedAt: dto.issuedAt || dto.createdAt,
      expiresAt: expiryDate.toISOString().split('T')[0],
      status: dto.status as PrescriptionStatus,
      notes: dto.notes || '',
      pdfUrl: `/prescriptions/rx_${dto.id}.pdf`
    };
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

  removeMedication(index: number): void {
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
    }
    this.showDrugSuggestions = false;
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
    this.errorMessage = '';

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
    }).subscribe({
      next: (created) => {
        this.isSaving = false;
        this.showPreviewModal = false;
        this.closeNewPrescriptionModal();
        this.loadData(); // Ricarica tutto
        this.successMessage = `Prescrizione emessa con successo. Il paziente ${patient?.name} riceverà una notifica.`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        this.isSaving = false;
        this.errorMessage = err.error?.detail || 'Errore nella creazione della prescrizione.';
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

  downloadPdf(prescription: Prescription): void {
    this.successMessage = `Download PDF prescrizione #${prescription.id} avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  getStatusLabel(status: PrescriptionStatus): string {
    const labels: Record<PrescriptionStatus, string> = {
      DRAFT: 'Bozza',
      ISSUED: 'Emessa',
      EXPIRED: 'Scaduta',
      CANCELLED: 'Annullata'
    };
    return labels[status];
  }

  getStatusBadgeClass(status: PrescriptionStatus): string {
    const classes: Record<PrescriptionStatus, string> = {
      DRAFT: 'bg-warning text-dark',
      ISSUED: 'bg-success',
      EXPIRED: 'bg-secondary',
      CANCELLED: 'bg-danger'
    };
    return classes[status];
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
}

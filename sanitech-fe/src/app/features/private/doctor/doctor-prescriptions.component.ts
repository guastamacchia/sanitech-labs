import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type PrescriptionStatus = 'DRAFT' | 'ISSUED' | 'EXPIRED' | 'CANCELLED';

interface Medication {
  name: string;
  dosage: string;
  posology: string;
  duration: number;
  notes: string;
}

interface Prescription {
  id: number;
  patientId: number;
  patientName: string;
  medications: Medication[];
  issuedAt: string;
  expiresAt: string;
  status: PrescriptionStatus;
  notes: string;
  pdfUrl: string;
}

interface PrescriptionForm {
  patientId: number | null;
  medications: Medication[];
  notes: string;
}

interface DrugSuggestion {
  name: string;
  dosages: string[];
}

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

  // Database farmaci (mock)
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

  // UI State
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showNewPrescriptionModal = false;
  showPreviewModal = false;
  selectedPrescription: Prescription | null = null;

  // Stats
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

    setTimeout(() => {
      // Mock pazienti con consenso PRESCRIPTIONS
      this.patients = [
        { id: 1, name: 'Esposito Mario' },
        { id: 2, name: 'Verdi Anna' },
        { id: 5, name: 'Romano Francesco' }
      ];

      // Mock prescrizioni - scenario Dott.ssa Moretti
      this.prescriptions = [
        {
          id: 1,
          patientId: 1,
          patientName: 'Esposito Mario',
          medications: [
            { name: 'Ramipril', dosage: '5mg', posology: '1 compressa al mattino', duration: 90, notes: 'Assumere a stomaco vuoto' },
            { name: 'Bisoprololo', dosage: '2.5mg', posology: '1 compressa al mattino', duration: 90, notes: '' }
          ],
          issuedAt: '2025-01-28T10:30:00',
          expiresAt: '2025-04-28',
          status: 'ISSUED',
          notes: 'Controllare la pressione settimanalmente. Prossimo controllo tra 3 mesi.',
          pdfUrl: '/prescriptions/rx_001.pdf'
        },
        {
          id: 2,
          patientId: 2,
          patientName: 'Verdi Anna',
          medications: [
            { name: 'Atorvastatina', dosage: '20mg', posology: '1 compressa la sera', duration: 30, notes: '' }
          ],
          issuedAt: '2025-01-25T14:00:00',
          expiresAt: '2025-02-25',
          status: 'ISSUED',
          notes: 'Ripetere esami del profilo lipidico tra 1 mese.',
          pdfUrl: '/prescriptions/rx_002.pdf'
        },
        {
          id: 3,
          patientId: 1,
          patientName: 'Esposito Mario',
          medications: [
            { name: 'Aspirina', dosage: '100mg', posology: '1 compressa al giorno', duration: 365, notes: 'Cardioaspirina' }
          ],
          issuedAt: '2024-11-15T09:00:00',
          expiresAt: '2025-11-15',
          status: 'ISSUED',
          notes: 'Terapia continuativa per prevenzione secondaria.',
          pdfUrl: '/prescriptions/rx_003.pdf'
        },
        {
          id: 4,
          patientId: 5,
          patientName: 'Romano Francesco',
          medications: [
            { name: 'Omeprazolo', dosage: '20mg', posology: '1 compressa prima di colazione', duration: 14, notes: '' }
          ],
          issuedAt: '2024-12-20T11:30:00',
          expiresAt: '2025-01-03',
          status: 'EXPIRED',
          notes: 'Ciclo per gastrite. Rivalutare se sintomi persistono.',
          pdfUrl: '/prescriptions/rx_004.pdf'
        }
      ];

      this.isLoading = false;
    }, 500);
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

    this.isSaving = true;

    setTimeout(() => {
      const patient = this.patients.find(p => p.id === this.prescriptionForm.patientId);
      const expiryDate = new Date();
      expiryDate.setDate(expiryDate.getDate() + Math.max(...this.prescriptionForm.medications.map(m => m.duration)));

      const newPrescription: Prescription = {
        id: this.prescriptions.length + 1,
        patientId: this.prescriptionForm.patientId!,
        patientName: patient?.name || '',
        medications: [...this.prescriptionForm.medications],
        issuedAt: new Date().toISOString(),
        expiresAt: expiryDate.toISOString().split('T')[0],
        status: 'ISSUED',
        notes: this.prescriptionForm.notes,
        pdfUrl: '/prescriptions/rx_new.pdf'
      };

      this.prescriptions.unshift(newPrescription);
      this.isSaving = false;
      this.showPreviewModal = false;
      this.closeNewPrescriptionModal();
      this.successMessage = `Prescrizione emessa con successo. Il paziente ${patient?.name} riceverà una notifica.`;
      setTimeout(() => this.successMessage = '', 5000);
    }, 2000);
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

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type PrescriptionStatus = 'ACTIVE' | 'COMPLETED' | 'EXPIRED' | 'SUSPENDED';

interface Prescription {
  id: number;
  drug: string;
  dosage: string;
  frequency: string;
  instructions?: string;
  doctorName: string;
  doctorSpecialty: string;
  prescribedAt: string;
  expiresAt?: string;
  status: PrescriptionStatus;
  refillsRemaining?: number;
  notes?: string;
}

@Component({
  selector: 'app-patient-prescriptions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-prescriptions.component.html'
})
export class PatientPrescriptionsComponent implements OnInit {
  // Dati prescrizioni
  prescriptions: Prescription[] = [];

  // UI State
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showDetailModal = false;
  selectedPrescription: Prescription | null = null;

  // Filtri
  statusFilter: 'ALL' | PrescriptionStatus = 'ALL';
  searchTerm = '';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  ngOnInit(): void {
    this.loadPrescriptions();
  }

  loadPrescriptions(): void {
    this.isLoading = true;

    // Dati mock per screenshot - Scenario di Giulia
    setTimeout(() => {
      this.prescriptions = [
        {
          id: 1,
          drug: 'Ramipril 5mg',
          dosage: '1 compressa',
          frequency: 'Una volta al giorno, al mattino',
          instructions: 'Assumere a stomaco vuoto',
          doctorName: 'Dr. Marco Cardioli',
          doctorSpecialty: 'Cardiologia',
          prescribedAt: '2024-10-15T10:30:00',
          expiresAt: '2025-04-15',
          status: 'ACTIVE',
          refillsRemaining: 2,
          notes: 'Controllo pressione settimanale consigliato'
        },
        {
          id: 2,
          drug: 'Aspirina Cardio 100mg',
          dosage: '1 compressa',
          frequency: 'Una volta al giorno, dopo pranzo',
          instructions: 'Assumere dopo i pasti',
          doctorName: 'Dr. Marco Cardioli',
          doctorSpecialty: 'Cardiologia',
          prescribedAt: '2024-10-15T10:30:00',
          expiresAt: '2025-04-15',
          status: 'ACTIVE',
          refillsRemaining: 3
        },
        {
          id: 3,
          drug: 'Pantoprazolo 20mg',
          dosage: '1 compressa',
          frequency: 'Una volta al giorno, prima di colazione',
          instructions: 'Da assumere almeno 30 minuti prima del pasto',
          doctorName: 'Dr. Luigi Gastri',
          doctorSpecialty: 'Gastroenterologia',
          prescribedAt: '2024-09-01T14:00:00',
          expiresAt: '2025-03-01',
          status: 'ACTIVE',
          refillsRemaining: 1
        },
        {
          id: 4,
          drug: 'Atorvastatina 20mg',
          dosage: '1 compressa',
          frequency: 'Una volta al giorno, la sera',
          doctorName: 'Dr. Marco Cardioli',
          doctorSpecialty: 'Cardiologia',
          prescribedAt: '2024-08-20T09:15:00',
          expiresAt: '2024-11-20',
          status: 'EXPIRED',
          notes: 'Sospesa per valori di colesterolo normalizzati'
        },
        {
          id: 5,
          drug: 'Ibuprofene 600mg',
          dosage: '1 compressa',
          frequency: 'Al bisogno, massimo 3 al giorno',
          instructions: 'Da assumere durante o dopo i pasti',
          doctorName: 'Dr. Anna Ortopedi',
          doctorSpecialty: 'Ortopedia',
          prescribedAt: '2024-07-10T11:45:00',
          status: 'COMPLETED',
          notes: 'Per dolore post-intervento'
        },
        {
          id: 6,
          drug: 'Amoxicillina 1g',
          dosage: '1 compressa',
          frequency: 'Due volte al giorno',
          instructions: 'Assumere ogni 12 ore per 7 giorni',
          doctorName: 'Dr. Paolo Medici',
          doctorSpecialty: 'Medicina generale',
          prescribedAt: '2024-06-05T08:30:00',
          status: 'COMPLETED',
          notes: 'Ciclo antibiotico per infezione vie respiratorie'
        }
      ];
      this.isLoading = false;
    }, 500);
  }

  get filteredPrescriptions(): Prescription[] {
    return this.prescriptions.filter(p => {
      if (this.statusFilter !== 'ALL' && p.status !== this.statusFilter) return false;
      if (this.searchTerm) {
        const term = this.searchTerm.toLowerCase();
        return p.drug.toLowerCase().includes(term) ||
               p.doctorName.toLowerCase().includes(term) ||
               p.doctorSpecialty.toLowerCase().includes(term);
      }
      return true;
    });
  }

  get paginatedPrescriptions(): Prescription[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPrescriptions.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredPrescriptions.length / this.pageSize) || 1;
  }

  get activePrescriptionsCount(): number {
    return this.prescriptions.filter(p => p.status === 'ACTIVE').length;
  }

  get expiringPrescriptionsCount(): number {
    const oneMonth = new Date();
    oneMonth.setMonth(oneMonth.getMonth() + 1);
    return this.prescriptions.filter(p =>
      p.status === 'ACTIVE' && p.expiresAt && new Date(p.expiresAt) <= oneMonth
    ).length;
  }

  getStatusLabel(status: PrescriptionStatus): string {
    const labels: Record<PrescriptionStatus, string> = {
      ACTIVE: 'Attiva',
      COMPLETED: 'Completata',
      EXPIRED: 'Scaduta',
      SUSPENDED: 'Sospesa'
    };
    return labels[status];
  }

  getStatusBadgeClass(status: PrescriptionStatus): string {
    const classes: Record<PrescriptionStatus, string> = {
      ACTIVE: 'bg-success',
      COMPLETED: 'bg-secondary',
      EXPIRED: 'bg-warning text-dark',
      SUSPENDED: 'bg-danger'
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

  formatDateLong(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  isExpiringSoon(prescription: Prescription): boolean {
    if (prescription.status !== 'ACTIVE' || !prescription.expiresAt) return false;
    const oneMonth = new Date();
    oneMonth.setMonth(oneMonth.getMonth() + 1);
    return new Date(prescription.expiresAt) <= oneMonth;
  }

  openDetailModal(prescription: Prescription): void {
    this.selectedPrescription = prescription;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedPrescription = null;
  }

  downloadPrescription(prescription: Prescription): void {
    this.successMessage = `Download della prescrizione "${prescription.drug}" avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  requestRefill(prescription: Prescription): void {
    this.successMessage = `Richiesta di rinnovo per "${prescription.drug}" inviata al medico.`;
    setTimeout(() => this.successMessage = '', 5000);
  }
}

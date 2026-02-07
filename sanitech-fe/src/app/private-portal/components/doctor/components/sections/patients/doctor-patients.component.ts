import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  DoctorApiService,
  PatientDto,
  ConsentScope
} from '../../../services/doctor-api.service';
import { Patient } from './dtos/patients.dto';

@Component({
  selector: 'app-doctor-patients',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-patients.component.html'
})
export class DoctorPatientsComponent implements OnInit {
  // Lista pazienti
  patients: Patient[] = [];

  // Filtri
  searchQuery = '';
  consentFilter: 'ALL' | ConsentScope = 'ALL';
  viewMode: 'consented' | 'all' = 'consented';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Stato UI
  isLoading = false;
  selectedPatient: Patient | null = null;
  showPatientModal = false;
  errorMessage = '';

  constructor(private doctorApi: DoctorApiService) {}

  // Statistiche
  get totalWithConsent(): number {
    return this.patients.filter(p => p.hasActiveConsent).length;
  }

  get totalPatients(): number {
    return this.patients.length;
  }

  get patientsWithDocs(): number {
    return this.patients.filter(p => p.consents.includes('DOCS')).length;
  }

  get patientsWithPrescriptions(): number {
    return this.patients.filter(p => p.consents.includes('PRESCRIPTIONS')).length;
  }

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.doctorApi.searchPatients({ size: 100 }).subscribe({
      next: (page) => {
        const patientDtos = page.content || [];

        // Per ogni paziente, verifica i consensi
        const consentChecks = patientDtos.map(patient => {
          const scopes: ConsentScope[] = ['DOCS', 'PRESCRIPTIONS', 'TELEVISIT'];
          return this.doctorApi.checkMultipleConsents(patient.id, scopes).pipe(
            map(consentsMap => ({ patient, consentsMap })),
            catchError(() => of({ patient, consentsMap: new Map<ConsentScope, boolean>() }))
          );
        });

        if (consentChecks.length > 0) {
          forkJoin(consentChecks).subscribe({
            next: (results) => {
              this.patients = results.map(r => this.mapPatient(r.patient, r.consentsMap));
              this.isLoading = false;
            },
            error: () => {
              // Fallback: mostra pazienti senza info consensi
              this.patients = patientDtos.map(p => this.mapPatient(p, new Map()));
              this.isLoading = false;
            }
          });
        } else {
          this.patients = [];
          this.isLoading = false;
        }
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei pazienti.';
        this.isLoading = false;
      }
    });
  }

  private mapPatient(dto: PatientDto, consentsMap: Map<ConsentScope, boolean>): Patient {
    const consents: ConsentScope[] = [];
    if (consentsMap.get('DOCS')) consents.push('DOCS');
    if (consentsMap.get('PRESCRIPTIONS')) consents.push('PRESCRIPTIONS');
    if (consentsMap.get('TELEVISIT')) consents.push('TELEVISIT');

    return {
      id: dto.id,
      firstName: dto.firstName,
      lastName: dto.lastName,
      fiscalCode: dto.fiscalCode || '',
      email: dto.email,
      phone: dto.phone || '',
      birthDate: dto.birthDate || '',
      consents,
      hasActiveConsent: consents.length > 0
    };
  }

  get filteredPatients(): Patient[] {
    let filtered = this.patients;

    // Filtro per vista (con consenso / tutti)
    if (this.viewMode === 'consented') {
      filtered = filtered.filter(p => p.hasActiveConsent);
    }

    // Filtro per tipo consenso
    if (this.consentFilter !== 'ALL') {
      filtered = filtered.filter(p => p.consents.includes(this.consentFilter as ConsentScope));
    }

    // Filtro ricerca
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(p =>
        p.firstName.toLowerCase().includes(query) ||
        p.lastName.toLowerCase().includes(query) ||
        p.fiscalCode.toLowerCase().includes(query)
      );
    }

    return filtered;
  }

  get paginatedPatients(): Patient[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPatients.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredPatients.length / this.pageSize) || 1;
  }

  openPatientModal(patient: Patient): void {
    this.selectedPatient = patient;
    this.showPatientModal = true;
  }

  closePatientModal(): void {
    this.showPatientModal = false;
    this.selectedPatient = null;
  }

  getConsentLabel(scope: ConsentScope): string {
    const labels: Record<ConsentScope, string> = {
      DOCS: 'Documenti',
      PRESCRIPTIONS: 'Prescrizioni',
      TELEVISIT: 'Televisite'
    };
    return labels[scope];
  }

  getConsentBadgeClass(scope: ConsentScope): string {
    const classes: Record<ConsentScope, string> = {
      DOCS: 'bg-primary bg-opacity-10 text-primary',
      PRESCRIPTIONS: 'bg-success bg-opacity-10 text-success',
      TELEVISIT: 'bg-info bg-opacity-10 text-info'
    };
    return classes[scope];
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

  calculateAge(birthDate: string): number {
    if (!birthDate) return 0;
    const birth = new Date(birthDate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  }
}

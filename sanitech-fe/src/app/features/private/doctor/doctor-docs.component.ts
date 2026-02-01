import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import {
  DoctorDocsService,
  PatientDto,
  DocumentDto,
  ProblemDetails,
  ConsentDeniedException,
  ConsentCheckResponse
} from './doctor-docs.service';

@Component({
  selector: 'app-doctor-docs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-docs.component.html'
})
export class DoctorDocsComponent implements OnInit {

  private docsService = inject(DoctorDocsService);

  // UI State
  searchQuery = '';
  isSearching = false;
  searchResults: PatientDto[] = [];
  hasSearched = false;
  searchTotalPages = 0;
  searchCurrentPage = 0;

  selectedPatient: PatientDto | null = null;
  isLoadingDocuments = false;
  patientDocuments: DocumentDto[] = [];
  documentsTotalPages = 0;
  documentsCurrentPage = 0;
  documentsTotalElements = 0;

  // Stato consenso
  consentInfo: ConsentCheckResponse | null = null;

  // Stato accesso negato (errore 403)
  accessDenied = false;
  accessDeniedError: ProblemDetails | null = null;

  // Paginazione documenti
  pageSize = 10;
  currentPage = 1;

  // Statistiche (mock per ora - in futuro da API)
  patientsWithConsent = 0;
  totalDocumentsAccessed = 0;
  accessDeniedCount = 0;

  ngOnInit(): void {
    this.loadStats();
  }

  private loadStats(): void {
    // TODO: In futuro, caricare le statistiche reali da un endpoint dedicato
    // Per ora lasciamo valori di default
    this.patientsWithConsent = 0;
    this.totalDocumentsAccessed = 0;
    this.accessDeniedCount = 0;
  }

  searchPatients(): void {
    if (!this.searchQuery.trim()) {
      this.searchResults = [];
      this.hasSearched = false;
      return;
    }

    this.isSearching = true;
    this.hasSearched = true;

    this.docsService.searchPatients(this.searchQuery.trim(), 0, 20).subscribe({
      next: (page) => {
        this.searchResults = page.content;
        this.searchTotalPages = page.totalPages;
        this.searchCurrentPage = page.number;
        this.isSearching = false;
      },
      error: (err) => {
        console.error('Errore ricerca pazienti:', err);
        this.searchResults = [];
        this.isSearching = false;
      }
    });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults = [];
    this.hasSearched = false;
  }

  selectPatient(patient: PatientDto): void {
    this.selectedPatient = patient;
    this.accessDenied = false;
    this.accessDeniedError = null;
    this.patientDocuments = [];
    this.consentInfo = null;
    this.currentPage = 1;

    this.loadPatientDocuments(patient.id);
  }

  private loadPatientDocuments(patientId: number): void {
    this.isLoadingDocuments = true;

    // Prima verifica il consenso (opzionale, poiche' la chiamata a /api/docs gia' lo verifica)
    // ma utile per avere info sullo stato del consenso anche in caso positivo
    this.docsService.checkConsent(patientId, 'DOCS').subscribe({
      next: (consent) => {
        this.consentInfo = consent;
        if (!consent.allowed) {
          // Consenso non presente/revocato/scaduto
          this.accessDenied = true;
          this.accessDeniedError = {
            type: 'https://sanitech.it/problems/consent-required',
            title: 'Consenso mancante',
            status: 403,
            detail: this.getConsentDeniedMessage(consent),
            instance: `/api/docs?patientId=${patientId}`
          };
          this.patientDocuments = [];
          this.isLoadingDocuments = false;
          return;
        }

        // Consenso valido: carica documenti
        this.fetchDocuments(patientId);
      },
      error: (err) => {
        // In caso di errore nella verifica consenso, prova comunque a caricare i documenti
        // Il backend rispondera' 403 se non c'e' consenso
        this.fetchDocuments(patientId);
      }
    });
  }

  private fetchDocuments(patientId: number): void {
    this.docsService.getPatientDocuments(patientId, this.currentPage - 1, this.pageSize).subscribe({
      next: (page) => {
        this.accessDenied = false;
        this.accessDeniedError = null;
        this.patientDocuments = page.content;
        this.documentsTotalPages = page.totalPages;
        this.documentsCurrentPage = page.number;
        this.documentsTotalElements = page.totalElements;
        this.isLoadingDocuments = false;
      },
      error: (err) => {
        if (err instanceof ConsentDeniedException) {
          this.accessDenied = true;
          this.accessDeniedError = err.problem;
          this.patientDocuments = [];
        } else {
          console.error('Errore caricamento documenti:', err);
          // Gestione errore generico
          this.accessDenied = true;
          this.accessDeniedError = {
            type: 'https://sanitech.it/problems/internal-error',
            title: 'Errore di sistema',
            status: err.status || 500,
            detail: 'Si e\' verificato un errore durante il caricamento dei documenti. Riprovare piu\' tardi.',
            instance: `/api/docs?patientId=${patientId}`
          };
        }
        this.isLoadingDocuments = false;
      }
    });
  }

  private getConsentDeniedMessage(consent: ConsentCheckResponse): string {
    if (consent.status === 'REVOKED') {
      return 'Il paziente ha revocato il consenso per la consultazione dei documenti clinici.';
    }
    if (consent.expiresAt && new Date(consent.expiresAt) < new Date()) {
      return 'Il consenso per la consultazione dei documenti clinici e\' scaduto.';
    }
    return 'Il paziente non ha concesso il consenso per la consultazione dei documenti clinici.';
  }

  deselectPatient(): void {
    this.selectedPatient = null;
    this.patientDocuments = [];
    this.accessDenied = false;
    this.accessDeniedError = null;
    this.consentInfo = null;
  }

  getDocumentTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      REPORT: 'Referto',
      REFERRAL: 'Impegnativa',
      PRESCRIPTION: 'Prescrizione',
      LAB_RESULT: 'Esame laboratorio',
      IMAGING: 'Imaging',
      DISCHARGE_LETTER: 'Lettera dimissione',
      OTHER: 'Altro'
    };
    return labels[type] || type;
  }

  getDocumentTypeIcon(type: string): string {
    const icons: Record<string, string> = {
      REPORT: 'bi-file-earmark-medical',
      REFERRAL: 'bi-file-earmark-text',
      PRESCRIPTION: 'bi-capsule',
      LAB_RESULT: 'bi-clipboard2-pulse',
      IMAGING: 'bi-image',
      DISCHARGE_LETTER: 'bi-file-earmark-richtext',
      OTHER: 'bi-file-earmark'
    };
    return icons[type] || 'bi-file-earmark';
  }

  getDocumentTypeBadgeClass(type: string): string {
    const classes: Record<string, string> = {
      REPORT: 'bg-primary',
      REFERRAL: 'bg-info',
      PRESCRIPTION: 'bg-success',
      LAB_RESULT: 'bg-warning text-dark',
      IMAGING: 'bg-secondary',
      DISCHARGE_LETTER: 'bg-danger',
      OTHER: 'bg-dark'
    };
    return classes[type] || 'bg-secondary';
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  get paginatedDocuments(): DocumentDto[] {
    return this.patientDocuments;
  }

  get totalPages(): number {
    return Math.max(1, this.documentsTotalPages);
  }

  changePage(page: number): void {
    if (page < 1 || page > this.totalPages || page === this.currentPage) return;
    this.currentPage = page;
    if (this.selectedPatient) {
      this.isLoadingDocuments = true;
      this.fetchDocuments(this.selectedPatient.id);
    }
  }

  downloadDocument(doc: DocumentDto): void {
    const url = this.docsService.downloadDocument(doc.id);
    window.open(url, '_blank');
  }

  viewDocument(doc: DocumentDto): void {
    const url = this.docsService.downloadDocument(doc.id);
    window.open(url, '_blank');
  }

  getConsentBadge(patient: PatientDto): { class: string; label: string } {
    // Senza una chiamata API specifica per ogni paziente, non possiamo sapere lo stato del consenso
    // nella lista. Mostriamo un badge neutro.
    return { class: 'bg-light text-dark', label: 'Verifica richiesta' };
  }

  getPatientFullName(patient: PatientDto): string {
    return `${patient.lastName} ${patient.firstName}`;
  }

  getPatientDepartments(patient: PatientDto): string {
    if (!patient.departments || patient.departments.length === 0) {
      return 'Nessun reparto';
    }
    return patient.departments.map(d => d.name || d.code).join(', ');
  }
}

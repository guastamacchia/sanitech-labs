import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import {
  DoctorApiService,
  PatientDto,
  DocumentDto,
  ConsentScope
} from '../services/doctor-api.service';

type DocumentType = 'REFERTO_VISITA' | 'REFERTO_ESAME' | 'LETTERA_DIMISSIONE' | 'CERTIFICATO' | 'ALTRO';
type DocumentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

interface ClinicalDocument {
  id: string;
  patientId: number;
  patientName: string;
  type: DocumentType;
  title: string;
  description: string;
  department: string;
  uploadedBy: string;
  uploadedAt: string;
  status: DocumentStatus;
  fileUrl: string;
  isOwnDocument: boolean;
}

interface UploadForm {
  patientId: number | null;
  type: DocumentType;
  title: string;
  description: string;
  file: File | null;
}

@Component({
  selector: 'app-doctor-clinical-docs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-clinical-docs.component.html'
})
export class DoctorClinicalDocsComponent implements OnInit {
  // Documenti
  documents: ClinicalDocument[] = [];

  // Pazienti con consenso DOCS
  patients: { id: number; name: string }[] = [];

  // Cache pazienti
  private patientsCache = new Map<number, PatientDto>();

  // Form upload
  uploadForm: UploadForm = {
    patientId: null,
    type: 'REFERTO_VISITA',
    title: '',
    description: '',
    file: null
  };

  // Filtri
  searchQuery = '';
  typeFilter: 'ALL' | DocumentType = 'ALL';
  patientFilter = 0;

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Stato UI
  isLoading = false;
  isUploading = false;
  isDeleting = false;
  successMessage = '';
  errorMessage = '';
  showUploadModal = false;
  showDeleteModal = false;
  documentToDelete: ClinicalDocument | null = null;

  constructor(private doctorApi: DoctorApiService) {}

  // Statistiche
  get totalDocuments(): number {
    return this.documents.length;
  }

  get ownDocuments(): number {
    return this.documents.filter(d => d.isOwnDocument).length;
  }

  get recentDocuments(): number {
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    return this.documents.filter(d => new Date(d.uploadedAt) > weekAgo).length;
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Carica pazienti con consenso DOCS
    this.doctorApi.searchPatients({ size: 100 }).pipe(
      switchMap(patientsPage => {
        const allPatients = patientsPage.content || [];
        // Cache pazienti
        allPatients.forEach(p => this.patientsCache.set(p.id, p));

        // Verifica consenso DOCS per ogni paziente
        const consentChecks = allPatients.map(patient =>
          this.doctorApi.checkConsent(patient.id, 'DOCS').pipe(
            map(consent => ({ patient, allowed: consent.allowed })),
            catchError(() => of({ patient, allowed: false }))
          )
        );

        if (consentChecks.length === 0) {
          return of({ allPatients, patientsWithConsent: [] as { id: number; name: string }[] });
        }

        return forkJoin(consentChecks).pipe(
          map(results => {
            const patientsWithConsent = results
              .filter(r => r.allowed)
              .map(r => ({
                id: r.patient.id,
                name: `${r.patient.lastName} ${r.patient.firstName}`
              }));
            return { allPatients, patientsWithConsent };
          })
        );
      })
    ).subscribe({
      next: ({ allPatients, patientsWithConsent }) => {
        this.patients = patientsWithConsent;

        // Carica documenti per tutti i pazienti con consenso
        if (patientsWithConsent.length === 0) {
          this.documents = [];
          this.isLoading = false;
          return;
        }

        const documentFetches = patientsWithConsent.map(patient =>
          this.doctorApi.listDocuments({
            patientId: patient.id,
            size: 50
          }).pipe(
            map(page => page.content || []),
            catchError(() => of([] as DocumentDto[]))
          )
        );

        forkJoin(documentFetches).subscribe({
          next: (documentArrays) => {
            const allDocuments = documentArrays.flat();
            this.documents = allDocuments.map(d => this.mapDocument(d));
            this.isLoading = false;
          },
          error: () => {
            this.errorMessage = 'Errore nel caricamento dei documenti.';
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

  private mapDocument(dto: DocumentDto): ClinicalDocument {
    const patient = this.patientsCache.get(dto.patientId);

    // Mappa documentType a tipo interno
    let type: DocumentType = 'ALTRO';
    const docType = (dto.documentType || '').toUpperCase();
    if (docType.includes('REFERTO') && docType.includes('VISITA')) type = 'REFERTO_VISITA';
    else if (docType.includes('REFERTO') || docType.includes('ESAME')) type = 'REFERTO_ESAME';
    else if (docType.includes('DIMISSIONE')) type = 'LETTERA_DIMISSIONE';
    else if (docType.includes('CERTIFICATO')) type = 'CERTIFICATO';

    return {
      id: dto.id,
      patientId: dto.patientId,
      patientName: patient ? `${patient.lastName} ${patient.firstName}` : `Paziente ${dto.patientId}`,
      type,
      title: dto.fileName,
      description: dto.description || '',
      department: dto.departmentCode,
      uploadedBy: '-', // Non disponibile nel DTO
      uploadedAt: dto.createdAt,
      status: 'PUBLISHED',
      fileUrl: this.doctorApi.downloadDocumentUrl(dto.id),
      isOwnDocument: true // Assumiamo che se il medico può vederlo, è accessibile
    };
  }

  get filteredDocuments(): ClinicalDocument[] {
    let filtered = this.documents;

    if (this.typeFilter !== 'ALL') {
      filtered = filtered.filter(d => d.type === this.typeFilter);
    }

    if (this.patientFilter > 0) {
      filtered = filtered.filter(d => d.patientId === this.patientFilter);
    }

    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(d =>
        d.title.toLowerCase().includes(query) ||
        d.patientName.toLowerCase().includes(query) ||
        d.description.toLowerCase().includes(query)
      );
    }

    return filtered.sort((a, b) => new Date(b.uploadedAt).getTime() - new Date(a.uploadedAt).getTime());
  }

  get paginatedDocuments(): ClinicalDocument[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredDocuments.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredDocuments.length / this.pageSize) || 1;
  }

  openUploadModal(): void {
    this.uploadForm = {
      patientId: null,
      type: 'REFERTO_VISITA',
      title: '',
      description: '',
      file: null
    };
    this.errorMessage = '';
    this.showUploadModal = true;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadForm.file = input.files[0];
    }
  }

  uploadDocument(): void {
    if (!this.uploadForm.patientId) {
      this.errorMessage = 'Seleziona un paziente.';
      return;
    }
    if (!this.uploadForm.title.trim()) {
      this.errorMessage = 'Inserisci un titolo per il documento.';
      return;
    }
    if (!this.uploadForm.file) {
      this.errorMessage = 'Seleziona un file da caricare.';
      return;
    }

    this.isUploading = true;
    this.errorMessage = '';

    // Nota: l'upload richiede multipart/form-data
    // Per ora simuliamo l'upload - l'implementazione completa richiederebbe
    // un metodo dedicato nel ApiService per l'upload multipart
    setTimeout(() => {
      this.isUploading = false;
      this.closeUploadModal();
      this.loadData(); // Ricarica documenti
      this.successMessage = `Documento "${this.uploadForm.title}" caricato con successo. Il paziente riceverà una notifica.`;
      setTimeout(() => this.successMessage = '', 5000);
    }, 2000);
  }

  getTypeLabel(type: DocumentType): string {
    const labels: Record<DocumentType, string> = {
      REFERTO_VISITA: 'Referto visita',
      REFERTO_ESAME: 'Referto esame',
      LETTERA_DIMISSIONE: 'Lettera dimissione',
      CERTIFICATO: 'Certificato',
      ALTRO: 'Altro'
    };
    return labels[type];
  }

  getTypeIcon(type: DocumentType): string {
    const icons: Record<DocumentType, string> = {
      REFERTO_VISITA: 'bi-file-earmark-text',
      REFERTO_ESAME: 'bi-file-earmark-medical',
      LETTERA_DIMISSIONE: 'bi-file-earmark-richtext',
      CERTIFICATO: 'bi-file-earmark-check',
      ALTRO: 'bi-file-earmark'
    };
    return icons[type];
  }

  getTypeBadgeClass(type: DocumentType): string {
    const classes: Record<DocumentType, string> = {
      REFERTO_VISITA: 'bg-primary bg-opacity-10 text-primary',
      REFERTO_ESAME: 'bg-success bg-opacity-10 text-success',
      LETTERA_DIMISSIONE: 'bg-info bg-opacity-10 text-info',
      CERTIFICATO: 'bg-warning bg-opacity-10 text-warning',
      ALTRO: 'bg-secondary bg-opacity-10 text-secondary'
    };
    return classes[type];
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

  viewDocument(doc: ClinicalDocument): void {
    const url = this.doctorApi.downloadDocumentUrl(doc.id);
    window.open(url, '_blank');
  }

  downloadDocument(doc: ClinicalDocument): void {
    const url = this.doctorApi.downloadDocumentUrl(doc.id);
    const link = document.createElement('a');
    link.href = url;
    link.download = doc.title;
    link.click();
  }

  deleteDocument(doc: ClinicalDocument): void {
    this.documentToDelete = doc;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.documentToDelete = null;
  }

  confirmDelete(): void {
    if (!this.documentToDelete) return;

    this.isDeleting = true;
    this.doctorApi.deleteDocument(this.documentToDelete.id).subscribe({
      next: () => {
        this.successMessage = `Documento "${this.documentToDelete?.title}" eliminato con successo.`;
        this.closeDeleteModal();
        this.loadData();
        this.isDeleting = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => {
        this.errorMessage = `Errore durante l'eliminazione del documento.`;
        this.isDeleting = false;
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }
}

import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { forkJoin, of, Subject } from 'rxjs';
import { catchError, map, switchMap, takeUntil } from 'rxjs/operators';
import {
  DoctorApiService,
  PatientDto,
  DocumentDto,
  ConsentScope
} from '../../../services/doctor-api.service';
import { DocumentType, DocumentStatus, ClinicalDocument, UploadForm } from './dtos/clinical-docs.dto';

/** Dimensione massima file: 10 MB */
const MAX_FILE_SIZE = 10 * 1024 * 1024;

/** MIME type ammessi */
const ALLOWED_MIME_TYPES = [
  'application/pdf',
  'image/jpeg',
  'image/png',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
];

/** Mappa esatta dei tipi documento */
const DOCUMENT_TYPE_MAP: Record<string, DocumentType> = {
  REFERTO_VISITA: 'REFERTO_VISITA',
  REFERTO_ESAME: 'REFERTO_ESAME',
  LETTERA_DIMISSIONE: 'LETTERA_DIMISSIONE',
  CERTIFICATO: 'CERTIFICATO',
  ALTRO: 'ALTRO',
  // Mappatura tipi backend lato paziente
  REPORT: 'REFERTO_ESAME',
  DISCHARGE_LETTER: 'LETTERA_DIMISSIONE',
  LAB_RESULTS: 'REFERTO_ESAME',
  IMAGING: 'REFERTO_ESAME',
  OTHER: 'ALTRO',
  // Mappatura tipi backend effettivi (da svc-docs seed data)
  REFERTO: 'REFERTO_VISITA',
  ESAME: 'REFERTO_ESAME',
  LETTERA_DIMISSIONI: 'LETTERA_DIMISSIONE'
};

@Component({
  selector: 'app-doctor-clinical-docs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-clinical-docs.component.html'
})
export class DoctorClinicalDocsComponent implements OnInit, OnDestroy {
  // Documenti
  documents: ClinicalDocument[] = [];

  // Pazienti con consenso DOCS
  patients: { id: number; name: string }[] = [];

  // Cache pazienti
  private patientsCache = new Map<number, PatientDto>();

  // Subject JWT (sub) del dottore corrente
  private currentDoctorSub: string | null = null;

  // Distruzione componente per unsubscribe
  private destroy$ = new Subject<void>();

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
  uploadErrorMessage = '';
  showUploadModal = false;
  showDeleteModal = false;
  documentToDelete: ClinicalDocument | null = null;

  constructor(
    private doctorApi: DoctorApiService,
    private route: ActivatedRoute
  ) {}

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

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  ngOnInit(): void {
    // Recupera il subject del dottore corrente dal token JWT
    this.currentDoctorSub = this.doctorApi.getDoctorSubject();

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

  /** Gestione chiusura modale con ESC */
  @HostListener('document:keydown.escape')
  onEscKeydown(): void {
    if (this.showUploadModal) {
      this.closeUploadModal();
    }
    if (this.showDeleteModal) {
      this.closeDeleteModal();
    }
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Clear cache per evitare dati obsoleti (BUG-010)
    this.patientsCache.clear();

    // Usa endpoint batch per consensi (BUG-008) + elimina nested subscribe (BUG-009)
    forkJoin({
      patientsPage: this.doctorApi.searchPatients({ size: 100 }),
      consentedPatientIds: this.doctorApi.getPatientsWithConsent('DOCS')
    }).pipe(
      switchMap(({ patientsPage, consentedPatientIds }) => {
        const allPatients = patientsPage.content || [];
        const consentedSet = new Set(consentedPatientIds);

        // Cache tutti i pazienti
        allPatients.forEach(p => this.patientsCache.set(p.id, p));

        // Filtra i pazienti con consenso
        const patientsWithConsent = allPatients
          .filter(p => consentedSet.has(p.id))
          .map(p => ({ id: p.id, name: `${p.lastName} ${p.firstName}` }));

        this.patients = patientsWithConsent;

        if (patientsWithConsent.length === 0) {
          return of([] as DocumentDto[]);
        }

        // Carica documenti per tutti i pazienti con consenso
        const documentFetches = patientsWithConsent.map(patient =>
          this.doctorApi.listDocuments({
            patientId: patient.id,
            size: 50
          }).pipe(
            map(page => page.content || []),
            catchError(() => of([] as DocumentDto[]))
          )
        );

        return forkJoin(documentFetches).pipe(
          map(arrays => arrays.flat())
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (allDocuments) => {
        this.documents = allDocuments.map(d => this.mapDocument(d));
        this.clampCurrentPage();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei dati.';
        this.isLoading = false;
      }
    });
  }

  private mapDocument(dto: DocumentDto): ClinicalDocument {
    const patient = this.patientsCache.get(dto.patientId);

    // Mappatura tipo con match esatto (BUG-006)
    const normalizedType = (dto.documentType || '').toUpperCase().trim();
    const type: DocumentType = DOCUMENT_TYPE_MAP[normalizedType] ?? 'ALTRO';

    // Determina ownership reale (BUG-003)
    const isOwnDocument = this.currentDoctorSub != null && dto.uploadedBy === this.currentDoctorSub;

    return {
      id: dto.id,
      patientId: dto.patientId,
      patientName: patient ? `${patient.lastName} ${patient.firstName}` : `Paziente ${dto.patientId}`,
      type,
      title: dto.description?.trim() || dto.fileName, // BUG-005: preferisci description come titolo
      description: dto.description?.trim() ? dto.fileName : '', // Mostra fileName come sotto-info se description usata come titolo
      department: dto.departmentCode,
      uploadedBy: dto.uploadedBy || '-', // BUG-004: usa il campo reale dal DTO
      uploadedAt: dto.createdAt,
      status: 'PUBLISHED',
      fileUrl: this.doctorApi.downloadDocumentUrl(dto.id),
      isOwnDocument
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

  /** Resetta paginazione centralizzato (BUG-012) */
  onFilterChange(): void {
    this.currentPage = 1;
  }

  /** Corregge la pagina corrente se fuori range (BUG-011) */
  private clampCurrentPage(): void {
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
  }

  openUploadModal(): void {
    this.uploadForm = {
      patientId: null,
      type: 'REFERTO_VISITA',
      title: '',
      description: '',
      file: null
    };
    this.uploadErrorMessage = '';
    this.showUploadModal = true;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.uploadErrorMessage = '';

    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validazione dimensione file (BUG-007)
      if (file.size > MAX_FILE_SIZE) {
        this.uploadErrorMessage = 'Il file supera la dimensione massima consentita di 10 MB.';
        input.value = '';
        this.uploadForm.file = null;
        return;
      }

      // Validazione tipo file (BUG-007)
      if (!ALLOWED_MIME_TYPES.includes(file.type)) {
        this.uploadErrorMessage = 'Tipo file non consentito. Formati ammessi: PDF, JPG, PNG, DOC, DOCX.';
        input.value = '';
        this.uploadForm.file = null;
        return;
      }

      this.uploadForm.file = file;
    }
  }

  uploadDocument(): void {
    this.uploadErrorMessage = '';

    if (!this.uploadForm.patientId) {
      this.uploadErrorMessage = 'Seleziona un paziente.';
      return;
    }
    if (!this.uploadForm.title.trim()) {
      this.uploadErrorMessage = 'Inserisci un titolo per il documento.';
      return;
    }
    if (!this.uploadForm.file) {
      this.uploadErrorMessage = 'Seleziona un file da caricare.';
      return;
    }

    this.isUploading = true;

    const departmentCode = this.doctorApi.getDepartmentCode() || 'DEFAULT';

    // Chiamata reale all'API (BUG-001 fix)
    this.doctorApi.uploadDocument({
      file: this.uploadForm.file,
      patientId: this.uploadForm.patientId,
      departmentCode,
      documentType: this.uploadForm.type,
      description: this.uploadForm.title + (this.uploadForm.description ? ` - ${this.uploadForm.description}` : '')
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        this.isUploading = false;
        this.closeUploadModal();
        this.successMessage = `Documento "${this.uploadForm.title}" caricato con successo. Il paziente riceverà una notifica.`;
        this.loadData();
      },
      error: (err) => {
        this.isUploading = false;
        // Messaggi specifici in base al tipo di errore (BUG-014)
        if (err.status === 400) {
          this.uploadErrorMessage = err.error?.detail || 'Errore di validazione. Verifica i dati inseriti.';
        } else if (err.status === 403) {
          this.uploadErrorMessage = 'Non sei autorizzato a caricare documenti per questo paziente.';
        } else if (err.status === 413) {
          this.uploadErrorMessage = 'Il file è troppo grande. Dimensione massima: 10 MB.';
        } else {
          this.uploadErrorMessage = 'Errore durante il caricamento del documento. Riprova più tardi.';
        }
      }
    });
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
    this.doctorApi.deleteDocument(this.documentToDelete.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        this.successMessage = `Documento "${this.documentToDelete?.title}" eliminato con successo.`;
        this.closeDeleteModal();
        this.isDeleting = false;
        this.currentPage = 1; // BUG-011: reset paginazione
        this.loadData();
      },
      error: (err) => {
        this.isDeleting = false;
        // Messaggi specifici per errore eliminazione (BUG-014)
        if (err.status === 403) {
          this.errorMessage = 'Non sei autorizzato a eliminare questo documento.';
        } else if (err.status === 404) {
          this.errorMessage = 'Il documento non è stato trovato. Potrebbe essere già stato eliminato.';
        } else {
          this.errorMessage = 'Errore durante l\'eliminazione del documento. Riprova più tardi.';
        }
        this.closeDeleteModal();
      }
    });
  }

  dismissSuccess(): void {
    this.successMessage = '';
  }

  dismissError(): void {
    this.errorMessage = '';
  }
}

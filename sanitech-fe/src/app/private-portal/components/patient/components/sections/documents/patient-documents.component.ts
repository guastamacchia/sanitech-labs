import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { PatientService, DocumentDto } from '../../../services/patient.service';
import { DepartmentDto } from '../../../services/scheduling.service';

@Component({
  selector: 'app-patient-documents',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-documents.component.html'
})
export class PatientDocumentsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  private static readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

  // Dati documenti
  documents: DocumentDto[] = [];
  departments: DepartmentDto[] = [];

  // Stato UI
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  uploadError = '';
  showUploadModal = false;
  showDeleteModal = false;
  documentToDelete: DocumentDto | null = null;

  // Filtri
  typeFilter: 'ALL' | string = 'ALL';
  departmentFilter: 'ALL' | string = 'ALL';

  // Form upload
  uploadForm = {
    file: null as File | null,
    fileName: '',
    type: 'REFERTO',
    description: '',
    departmentCode: ''
  };

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Tipi documento disponibili — codici allineati al backend
  documentTypes = [
    { code: 'REFERTO', label: 'Referto' },
    { code: 'LETTERA_DIMISSIONI', label: 'Lettera di dimissione' },
    { code: 'ESAME', label: 'Esami di laboratorio' },
    { code: 'IMAGING', label: 'Imaging' },
    { code: 'ALTRO', label: 'Altro' }
  ];

  constructor(private patientService: PatientService) {}

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showUploadModal) {
      this.closeUploadModal();
    } else if (this.showDeleteModal) {
      this.closeDeleteModal();
    }
  }

  ngOnInit(): void {
    this.loadDocuments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDocuments(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Carica documenti e reparti in parallelo
    this.patientService.getDepartments()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (departments) => {
          this.departments = departments;
          if (departments.length > 0 && !this.uploadForm.departmentCode) {
            this.uploadForm.departmentCode = departments[0].code;
          }
        },
        error: (err) => console.error('Errore caricamento reparti:', err)
      });

    this.patientService.getDocuments({ size: 100 })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.documents = response.content;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Errore caricamento documenti:', err);
          this.errorMessage = 'Impossibile caricare i documenti. Riprova.';
          this.isLoading = false;
        }
      });
  }

  get filteredDocuments(): DocumentDto[] {
    return this.documents.filter(doc => {
      if (this.typeFilter !== 'ALL' && doc.documentType !== this.typeFilter) return false;
      if (this.departmentFilter !== 'ALL' && doc.departmentCode !== this.departmentFilter) return false;
      return true;
    });
  }

  get paginatedDocuments(): DocumentDto[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredDocuments.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredDocuments.length / this.pageSize) || 1;
  }

  get documentsBySource(): { total: number } {
    return {
      total: this.documents.length
    };
  }

  getTypeLabel(type: string): string {
    const found = this.documentTypes.find(t => t.code === type);
    return found?.label || type;
  }

  getTypeIcon(type: string): string {
    const icons: Record<string, string> = {
      REFERTO: 'bi-file-earmark-text',
      LETTERA_DIMISSIONI: 'bi-file-earmark-medical',
      ESAME: 'bi-droplet',
      IMAGING: 'bi-image',
      ALTRO: 'bi-file-earmark'
    };
    return icons[type] || 'bi-file-earmark';
  }

  getDepartmentName(code: string): string {
    const dept = this.departments.find(d => d.code === code);
    return dept?.name || code;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatDateShort(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(0) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  openUploadModal(): void {
    this.uploadForm = {
      file: null,
      fileName: '',
      type: 'ESAME',
      description: '',
      departmentCode: this.departments[0]?.code || ''
    };
    this.uploadError = '';
    this.showUploadModal = true;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      if (file.size > PatientDocumentsComponent.MAX_FILE_SIZE) {
        this.uploadError = `Il file supera la dimensione massima consentita (10 MB). Dimensione: ${this.formatFileSize(file.size)}.`;
        this.uploadForm.file = null;
        this.uploadForm.fileName = '';
        input.value = '';
        return;
      }
      this.uploadError = '';
      this.uploadForm.file = file;
      this.uploadForm.fileName = file.name;
    }
  }

  submitUpload(): void {
    if (!this.uploadForm.file) {
      this.uploadError = 'Seleziona un file da caricare.';
      return;
    }

    if (!this.uploadForm.departmentCode) {
      this.uploadError = 'Seleziona un reparto.';
      return;
    }

    this.isLoading = true;
    this.uploadError = '';

    this.patientService.uploadDocument(this.uploadForm.file, {
      departmentCode: this.uploadForm.departmentCode,
      documentType: this.uploadForm.type,
      description: this.uploadForm.description || undefined
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (newDoc) => {
        this.documents = [newDoc, ...this.documents];
        this.isLoading = false;
        this.closeUploadModal();
        this.successMessage = 'Documento caricato con successo! Il medico potra\' visualizzarlo alla prossima visita.';
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        console.error('Errore upload documento:', err);
        this.uploadError = this.getUploadErrorMessage(err);
        this.isLoading = false;
      }
    });
  }

  downloadDocument(doc: DocumentDto): void {
    this.patientService.downloadDocument(doc.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = doc.fileName;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
          this.successMessage = `Download di "${doc.fileName}" completato.`;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Errore download documento:', err);
          this.errorMessage = this.getErrorMessage(err, 'scaricare');
        }
      });
  }

  viewDocument(doc: DocumentDto): void {
    this.patientService.downloadDocument(doc.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          window.open(url, '_blank');
          setTimeout(() => window.URL.revokeObjectURL(url), 5000);
          this.successMessage = `Apertura documento "${doc.fileName}"...`;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Errore apertura documento:', err);
          this.errorMessage = this.getErrorMessage(err, 'aprire');
        }
      });
  }

  confirmDeleteDocument(doc: DocumentDto): void {
    this.documentToDelete = doc;
    this.errorMessage = '';
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.documentToDelete = null;
  }

  deleteDocument(): void {
    if (!this.documentToDelete) return;

    this.isLoading = true;
    this.errorMessage = '';
    const fileName = this.documentToDelete.fileName;

    this.patientService.deleteDocument(this.documentToDelete.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.documents = this.documents.filter(d => d.id !== this.documentToDelete!.id);
          this.isLoading = false;
          this.closeDeleteModal();
          this.successMessage = `Documento "${fileName}" eliminato con successo.`;
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          console.error('Errore eliminazione documento:', err);
          this.isLoading = false;
          this.closeDeleteModal();
          this.errorMessage = this.getErrorMessage(err, 'eliminare');
        }
      });
  }

  private getErrorMessage(err: any, azione: string): string {
    const status = err?.status;
    if (status === 403) return `Non hai i permessi per ${azione} questo documento.`;
    if (status === 404) return 'Documento non trovato. Potrebbe essere stato gia\' eliminato.';
    if (status === 413) return 'Il file supera la dimensione massima consentita (10 MB).';
    return `Impossibile ${azione} il documento. Riprova piu' tardi.`;
  }

  private getUploadErrorMessage(err: any): string {
    const status = err?.status;
    if (status === 403) return 'Non hai i permessi per caricare documenti.';
    if (status === 413) return 'Il file supera la dimensione massima consentita (10 MB).';
    if (status === 400) return 'Dati non validi. Verifica i campi e riprova.';
    return 'Impossibile caricare il documento. Riprova piu\' tardi.';
  }
}

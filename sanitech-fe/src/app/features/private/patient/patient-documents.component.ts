import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { PatientService, DocumentDto } from './services/patient.service';
import { DepartmentDto } from './services/scheduling.service';

@Component({
  selector: 'app-patient-documents',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-documents.component.html'
})
export class PatientDocumentsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Dati documenti
  documents: DocumentDto[] = [];
  departments: DepartmentDto[] = [];

  // UI State
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showUploadModal = false;

  // Filtri
  typeFilter: 'ALL' | string = 'ALL';
  departmentFilter: 'ALL' | string = 'ALL';

  // Form upload
  uploadForm = {
    file: null as File | null,
    fileName: '',
    type: 'REPORT',
    description: '',
    departmentCode: ''
  };

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Tipi documento disponibili
  documentTypes = [
    { code: 'REPORT', label: 'Referto' },
    { code: 'DISCHARGE_LETTER', label: 'Lettera di dimissione' },
    { code: 'LAB_RESULTS', label: 'Esami di laboratorio' },
    { code: 'IMAGING', label: 'Imaging' },
    { code: 'OTHER', label: 'Altro' }
  ];

  constructor(private patientService: PatientService) {}

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
      REPORT: 'bi-file-earmark-text',
      DISCHARGE_LETTER: 'bi-file-earmark-medical',
      LAB_RESULTS: 'bi-droplet',
      IMAGING: 'bi-image',
      OTHER: 'bi-file-earmark'
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
      type: 'LAB_RESULTS',
      description: '',
      departmentCode: this.departments[0]?.code || ''
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
      this.uploadForm.fileName = input.files[0].name;
    }
  }

  submitUpload(): void {
    if (!this.uploadForm.file) {
      this.errorMessage = 'Seleziona un file da caricare.';
      return;
    }

    if (!this.uploadForm.departmentCode) {
      this.errorMessage = 'Seleziona un reparto.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

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
        this.errorMessage = 'Impossibile caricare il documento. Riprova.';
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
          this.errorMessage = 'Impossibile scaricare il documento. Riprova.';
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
          this.successMessage = `Apertura documento "${doc.fileName}"...`;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Errore apertura documento:', err);
          this.errorMessage = 'Impossibile aprire il documento. Riprova.';
        }
      });
  }
}

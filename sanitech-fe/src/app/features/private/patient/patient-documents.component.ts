import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type DocumentType = 'REFERTO' | 'LETTERA_DIMISSIONE' | 'ESAMI_LABORATORIO' | 'IMAGING' | 'ALTRO';
type DocumentSource = 'MEDICO' | 'PAZIENTE';

interface ClinicalDocument {
  id: number;
  name: string;
  type: DocumentType;
  description?: string;
  department: string;
  uploadedAt: string;
  uploadedBy: DocumentSource;
  doctorName?: string;
  fileSize: string;
  viewedByDoctor?: boolean;
}

@Component({
  selector: 'app-patient-documents',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-documents.component.html'
})
export class PatientDocumentsComponent implements OnInit {
  // Dati documenti
  documents: ClinicalDocument[] = [];

  // UI State
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showUploadModal = false;

  // Filtri
  typeFilter: 'ALL' | DocumentType = 'ALL';
  sourceFilter: 'ALL' | DocumentSource = 'ALL';

  // Form upload
  uploadForm = {
    file: null as File | null,
    fileName: '',
    type: 'ESAMI_LABORATORIO' as DocumentType,
    description: '',
    department: 'Medicina generale'
  };

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Dipartimenti disponibili
  departments = [
    'Medicina generale',
    'Cardiologia',
    'Ortopedia',
    'Dermatologia',
    'Neurologia',
    'Oculistica',
    'Ginecologia',
    'Urologia'
  ];

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.isLoading = true;

    // Dati mock per screenshot
    setTimeout(() => {
      this.documents = [
        {
          id: 1,
          name: 'Referto visita cardiologica',
          type: 'REFERTO',
          description: 'Visita di controllo annuale',
          department: 'Cardiologia',
          uploadedAt: '2025-01-15T10:30:00',
          uploadedBy: 'MEDICO',
          doctorName: 'Dr. Mario Rossi',
          fileSize: '245 KB',
          viewedByDoctor: true
        },
        {
          id: 2,
          name: 'Esami del sangue - Emocromo completo',
          type: 'ESAMI_LABORATORIO',
          description: 'Controllo periodico',
          department: 'Medicina generale',
          uploadedAt: '2025-01-10T14:15:00',
          uploadedBy: 'PAZIENTE',
          fileSize: '180 KB',
          viewedByDoctor: true
        },
        {
          id: 3,
          name: 'Lettera di dimissione - Day Hospital',
          type: 'LETTERA_DIMISSIONE',
          department: 'Cardiologia',
          uploadedAt: '2024-12-20T09:00:00',
          uploadedBy: 'MEDICO',
          doctorName: 'Dr. Luigi Verdi',
          fileSize: '320 KB'
        },
        {
          id: 4,
          name: 'RX Torace',
          type: 'IMAGING',
          description: 'Radiografia di controllo',
          department: 'Radiologia',
          uploadedAt: '2024-12-05T16:45:00',
          uploadedBy: 'MEDICO',
          doctorName: 'Dr. Anna Bianchi',
          fileSize: '1.2 MB'
        },
        {
          id: 5,
          name: 'Esami delle urine',
          type: 'ESAMI_LABORATORIO',
          description: 'Esame completo',
          department: 'Medicina generale',
          uploadedAt: '2024-11-28T11:20:00',
          uploadedBy: 'PAZIENTE',
          fileSize: '95 KB',
          viewedByDoctor: false
        },
        {
          id: 6,
          name: 'Referto ECG',
          type: 'REFERTO',
          description: 'Elettrocardiogramma di controllo',
          department: 'Cardiologia',
          uploadedAt: '2024-11-15T08:30:00',
          uploadedBy: 'MEDICO',
          doctorName: 'Dr. Mario Rossi',
          fileSize: '156 KB'
        }
      ];
      this.isLoading = false;
    }, 500);
  }

  get filteredDocuments(): ClinicalDocument[] {
    return this.documents.filter(doc => {
      if (this.typeFilter !== 'ALL' && doc.type !== this.typeFilter) return false;
      if (this.sourceFilter !== 'ALL' && doc.uploadedBy !== this.sourceFilter) return false;
      return true;
    });
  }

  get paginatedDocuments(): ClinicalDocument[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredDocuments.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredDocuments.length / this.pageSize) || 1;
  }

  get documentsByType(): { medico: number; paziente: number } {
    return {
      medico: this.documents.filter(d => d.uploadedBy === 'MEDICO').length,
      paziente: this.documents.filter(d => d.uploadedBy === 'PAZIENTE').length
    };
  }

  getTypeLabel(type: DocumentType): string {
    const labels: Record<DocumentType, string> = {
      REFERTO: 'Referto',
      LETTERA_DIMISSIONE: 'Lettera di dimissione',
      ESAMI_LABORATORIO: 'Esami di laboratorio',
      IMAGING: 'Imaging',
      ALTRO: 'Altro'
    };
    return labels[type];
  }

  getTypeIcon(type: DocumentType): string {
    const icons: Record<DocumentType, string> = {
      REFERTO: 'bi-file-earmark-text',
      LETTERA_DIMISSIONE: 'bi-file-earmark-medical',
      ESAMI_LABORATORIO: 'bi-droplet',
      IMAGING: 'bi-image',
      ALTRO: 'bi-file-earmark'
    };
    return icons[type];
  }

  getSourceBadgeClass(source: DocumentSource): string {
    return source === 'MEDICO' ? 'bg-primary' : 'bg-info';
  }

  getSourceLabel(source: DocumentSource): string {
    return source === 'MEDICO' ? 'Caricato dal medico' : 'Caricato dal paziente';
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

  openUploadModal(): void {
    this.uploadForm = {
      file: null,
      fileName: '',
      type: 'ESAMI_LABORATORIO',
      description: '',
      department: 'Medicina generale'
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

    this.isLoading = true;

    // Simula upload
    setTimeout(() => {
      const newDoc: ClinicalDocument = {
        id: this.documents.length + 1,
        name: this.uploadForm.fileName,
        type: this.uploadForm.type,
        description: this.uploadForm.description,
        department: this.uploadForm.department,
        uploadedAt: new Date().toISOString(),
        uploadedBy: 'PAZIENTE',
        fileSize: this.formatFileSize(this.uploadForm.file!.size),
        viewedByDoctor: false
      };

      this.documents = [newDoc, ...this.documents];
      this.isLoading = false;
      this.closeUploadModal();
      this.successMessage = 'Documento caricato con successo! Il medico potra\' visualizzarlo alla prossima visita.';
      setTimeout(() => this.successMessage = '', 5000);
    }, 1500);
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(0) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  downloadDocument(doc: ClinicalDocument): void {
    this.successMessage = `Download di "${doc.name}" avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }

  viewDocument(doc: ClinicalDocument): void {
    this.successMessage = `Apertura documento "${doc.name}"...`;
    setTimeout(() => this.successMessage = '', 3000);
  }
}

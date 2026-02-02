import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type DocumentType = 'REFERTO_VISITA' | 'REFERTO_ESAME' | 'LETTERA_DIMISSIONE' | 'CERTIFICATO' | 'ALTRO';
type DocumentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

interface ClinicalDocument {
  id: number;
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

  // UI State
  isLoading = false;
  isUploading = false;
  successMessage = '';
  errorMessage = '';
  showUploadModal = false;

  // Stats
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

    setTimeout(() => {
      // Mock pazienti con consenso DOCS
      this.patients = [
        { id: 1, name: 'Esposito Mario' },
        { id: 2, name: 'Verdi Anna' },
        { id: 4, name: 'Rossi Giulia' },
        { id: 5, name: 'Romano Francesco' },
        { id: 8, name: 'Conti Sara' }
      ];

      // Mock documenti - scenario Dott. Bianchi
      this.documents = [
        {
          id: 1,
          patientId: 1,
          patientName: 'Esposito Mario',
          type: 'REFERTO_ESAME',
          title: 'ECG a riposo',
          description: 'Controllo aritmia - ritmo sinusale regolare',
          department: 'Cardiologia',
          uploadedBy: 'Dott. Bianchi',
          uploadedAt: '2025-01-28T14:30:00',
          status: 'PUBLISHED',
          fileUrl: '/documents/ecg_esposito.pdf',
          isOwnDocument: true
        },
        {
          id: 2,
          patientId: 1,
          patientName: 'Esposito Mario',
          type: 'REFERTO_VISITA',
          title: 'Visita cardiologica di controllo',
          description: 'Controllo post-infarto. Paziente stabile.',
          department: 'Cardiologia',
          uploadedBy: 'Dott. Bianchi',
          uploadedAt: '2025-01-20T10:00:00',
          status: 'PUBLISHED',
          fileUrl: '/documents/visita_esposito.pdf',
          isOwnDocument: true
        },
        {
          id: 3,
          patientId: 2,
          patientName: 'Verdi Anna',
          type: 'REFERTO_ESAME',
          title: 'Ecocardiogramma',
          description: 'Frazione di eiezione nella norma',
          department: 'Cardiologia',
          uploadedBy: 'Dott. Bianchi',
          uploadedAt: '2025-01-25T11:30:00',
          status: 'PUBLISHED',
          fileUrl: '/documents/eco_verdi.pdf',
          isOwnDocument: true
        },
        {
          id: 4,
          patientId: 1,
          patientName: 'Esposito Mario',
          type: 'REFERTO_ESAME',
          title: 'Analisi del sangue',
          description: 'Profilo lipidico e markers cardiaci',
          department: 'Laboratorio analisi',
          uploadedBy: 'Dott.ssa Ferretti',
          uploadedAt: '2025-01-15T09:00:00',
          status: 'PUBLISHED',
          fileUrl: '/documents/analisi_esposito.pdf',
          isOwnDocument: false
        },
        {
          id: 5,
          patientId: 4,
          patientName: 'Rossi Giulia',
          type: 'REFERTO_VISITA',
          title: 'Prima visita cardiologica',
          description: 'Valutazione iniziale per palpitazioni',
          department: 'Cardiologia',
          uploadedBy: 'Dott. Bianchi',
          uploadedAt: '2025-01-10T15:00:00',
          status: 'PUBLISHED',
          fileUrl: '/documents/visita_rossi.pdf',
          isOwnDocument: true
        },
        {
          id: 6,
          patientId: 1,
          patientName: 'Esposito Mario',
          type: 'ALTRO',
          title: 'Foto ferita chirurgica',
          description: 'Documentazione fotografica cicatrice',
          department: 'Cardiologia',
          uploadedBy: 'Paziente',
          uploadedAt: '2025-01-22T08:30:00',
          status: 'PUBLISHED',
          fileUrl: '/documents/foto_esposito.jpg',
          isOwnDocument: false
        }
      ];

      this.isLoading = false;
    }, 500);
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

    setTimeout(() => {
      const patient = this.patients.find(p => p.id === this.uploadForm.patientId);
      const newDoc: ClinicalDocument = {
        id: this.documents.length + 1,
        patientId: this.uploadForm.patientId!,
        patientName: patient?.name || '',
        type: this.uploadForm.type,
        title: this.uploadForm.title,
        description: this.uploadForm.description,
        department: 'Cardiologia',
        uploadedBy: 'Dott. Bianchi',
        uploadedAt: new Date().toISOString(),
        status: 'PUBLISHED',
        fileUrl: '/documents/new_doc.pdf',
        isOwnDocument: true
      };

      this.documents.unshift(newDoc);
      this.isUploading = false;
      this.closeUploadModal();
      this.successMessage = `Documento "${newDoc.title}" caricato con successo. Il paziente riceverà una notifica.`;
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
    this.successMessage = `Apertura documento "${doc.title}"...`;
    setTimeout(() => this.successMessage = '', 2000);
  }

  downloadDocument(doc: ClinicalDocument): void {
    this.successMessage = `Download di "${doc.title}" avviato.`;
    setTimeout(() => this.successMessage = '', 3000);
  }
}

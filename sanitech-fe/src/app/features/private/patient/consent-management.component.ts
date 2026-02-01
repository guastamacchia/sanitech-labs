import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { forkJoin } from 'rxjs';

type ConsentScope = 'DOCS' | 'PRESCRIPTIONS' | 'TELEVISIT';
type ConsentStatus = 'GRANTED' | 'REVOKED';

/** DTO dal backend: /api/consents/me/doctors */
interface ConsentDto {
  id: number;
  patientId: number;
  doctorId: number;
  scope: ConsentScope;
  status: ConsentStatus;
  grantedAt: string;
  revokedAt?: string;
  expiresAt?: string;
  updatedAt: string;
}

/** DTO dal backend: /api/doctors */
interface DoctorDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  departmentCode: string;
  departmentName: string;
  facilityCode: string;
  facilityName: string;
}

/** Risposta paginata dal backend */
interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/** Modello arricchito per la visualizzazione */
interface DoctorConsent {
  id: number;
  doctorId: number;
  doctorName: string;
  doctorSpecialty: string;
  scope: ConsentScope;
  status: ConsentStatus;
  grantedAt: string;
  revokedAt?: string;
  expiresAt?: string;
}

@Component({
  selector: 'app-consent-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './consent-management.component.html'
})
export class ConsentManagementComponent implements OnInit {

  constructor(private api: ApiService) {}

  // Dati
  availableDoctors: DoctorDto[] = [];
  doctorConsents: DoctorConsent[] = [];
  private doctorsMap = new Map<number, DoctorDto>();

  // UI State
  isLoading = false;
  showGrantModal = false;
  showRevokeModal = false;
  successMessage = '';
  errorMessage = '';
  loadError = '';

  // Form per nuovo consenso
  grantForm = {
    doctorId: null as number | null,
    scope: 'DOCS' as ConsentScope,
    hasExpiry: false,
    expiryDate: ''
  };

  // Consenso selezionato per revoca
  selectedConsentForRevoke: DoctorConsent | null = null;

  // Filtri
  statusFilter: 'ALL' | 'GRANTED' | 'REVOKED' = 'ALL';
  scopeFilter: 'ALL' | ConsentScope = 'ALL';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.loadError = '';

    forkJoin({
      doctors: this.api.request<PageResponse<DoctorDto>>('GET', '/api/doctors?size=100'),
      consents: this.api.request<ConsentDto[]>('GET', '/api/consents/me/doctors')
    }).subscribe({
      next: ({ doctors, consents }) => {
        this.availableDoctors = doctors.content;
        this.doctorsMap.clear();
        this.availableDoctors.forEach(d => this.doctorsMap.set(d.id, d));
        this.doctorConsents = this.enrichConsents(consents);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Errore caricamento dati:', err);
        this.loadError = 'Impossibile caricare i dati. Riprova.';
        this.isLoading = false;
      }
    });
  }

  private enrichConsents(consents: ConsentDto[]): DoctorConsent[] {
    return consents.map(c => {
      const doctor = this.doctorsMap.get(c.doctorId);
      return {
        id: c.id,
        doctorId: c.doctorId,
        doctorName: doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : `Medico #${c.doctorId}`,
        doctorSpecialty: doctor?.departmentName || 'N/D',
        scope: c.scope,
        status: c.status,
        grantedAt: c.grantedAt,
        revokedAt: c.revokedAt,
        expiresAt: c.expiresAt
      };
    });
  }

  get filteredConsents(): DoctorConsent[] {
    return this.doctorConsents.filter(c => {
      if (this.statusFilter !== 'ALL' && c.status !== this.statusFilter) return false;
      if (this.scopeFilter !== 'ALL' && c.scope !== this.scopeFilter) return false;
      return true;
    });
  }

  get paginatedConsents(): DoctorConsent[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredConsents.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredConsents.length / this.pageSize) || 1;
  }

  get activeConsentsCount(): number {
    return this.doctorConsents.filter(c => c.status === 'GRANTED').length;
  }

  get revokedConsentsCount(): number {
    return this.doctorConsents.filter(c => c.status === 'REVOKED').length;
  }

  get doctorsWithConsent(): number {
    const uniqueDoctors = new Set(
      this.doctorConsents.filter(c => c.status === 'GRANTED').map(c => c.doctorId)
    );
    return uniqueDoctors.size;
  }

  getScopeLabel(scope: ConsentScope): string {
    const labels: Record<ConsentScope, string> = {
      DOCS: 'Documenti clinici',
      PRESCRIPTIONS: 'Prescrizioni',
      TELEVISIT: 'Televisite'
    };
    return labels[scope];
  }

  getScopeIcon(scope: ConsentScope): string {
    const icons: Record<ConsentScope, string> = {
      DOCS: 'bi-file-earmark-medical',
      PRESCRIPTIONS: 'bi-capsule',
      TELEVISIT: 'bi-camera-video'
    };
    return icons[scope];
  }

  getStatusBadgeClass(consent: DoctorConsent): string {
    if (consent.status === 'REVOKED') return 'bg-secondary';
    if (consent.expiresAt && new Date(consent.expiresAt) < new Date()) return 'bg-warning text-dark';
    return 'bg-success';
  }

  getStatusLabel(consent: DoctorConsent): string {
    if (consent.status === 'REVOKED') return 'Revocato';
    if (consent.expiresAt && new Date(consent.expiresAt) < new Date()) return 'Scaduto';
    return 'Attivo';
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatExpiryDate(dateStr?: string): string {
    if (!dateStr) return 'Nessuna scadenza';
    const date = new Date(dateStr);
    const now = new Date();
    const isExpired = date < now;
    const formatted = date.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
    return isExpired ? `Scaduto il ${formatted}` : formatted;
  }

  openGrantModal(): void {
    this.grantForm = {
      doctorId: null,
      scope: 'DOCS',
      hasExpiry: false,
      expiryDate: ''
    };
    this.errorMessage = '';
    this.showGrantModal = true;
  }

  closeGrantModal(): void {
    this.showGrantModal = false;
  }

  openRevokeModal(consent: DoctorConsent): void {
    this.selectedConsentForRevoke = consent;
    this.showRevokeModal = true;
  }

  closeRevokeModal(): void {
    this.showRevokeModal = false;
    this.selectedConsentForRevoke = null;
  }

  submitGrant(): void {
    if (!this.grantForm.doctorId) {
      this.errorMessage = 'Seleziona un medico.';
      return;
    }

    const doctor = this.doctorsMap.get(this.grantForm.doctorId);
    if (!doctor) {
      this.errorMessage = 'Medico non trovato.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const body = {
      doctorId: this.grantForm.doctorId,
      scope: this.grantForm.scope,
      expiresAt: this.grantForm.hasExpiry && this.grantForm.expiryDate
        ? new Date(this.grantForm.expiryDate).toISOString()
        : null
    };

    this.api.request<ConsentDto>('POST', '/api/consents/me/doctors', body).subscribe({
      next: (consent) => {
        const enriched: DoctorConsent = {
          id: consent.id,
          doctorId: consent.doctorId,
          doctorName: `Dr. ${doctor.firstName} ${doctor.lastName}`,
          doctorSpecialty: doctor.departmentName,
          scope: consent.scope,
          status: consent.status,
          grantedAt: consent.grantedAt,
          expiresAt: consent.expiresAt
        };
        this.doctorConsents = [enriched, ...this.doctorConsents];
        this.isLoading = false;
        this.closeGrantModal();
        this.successMessage = `Consenso concesso con successo a ${doctor.firstName} ${doctor.lastName}`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        console.error('Errore concessione consenso:', err);
        this.isLoading = false;
        if (err.status === 409 || err.error?.message?.includes('esiste')) {
          this.errorMessage = 'Esiste gia\' un consenso attivo per questo medico e ambito.';
        } else {
          this.errorMessage = err.error?.message || 'Errore durante la concessione del consenso.';
        }
      }
    });
  }

  confirmRevoke(): void {
    if (!this.selectedConsentForRevoke) return;

    const consent = this.selectedConsentForRevoke;
    this.isLoading = true;

    this.api.request<void>(
      'DELETE',
      `/api/consents/me/doctors/${consent.doctorId}/${consent.scope}`
    ).subscribe({
      next: () => {
        // Aggiorna localmente il consenso come revocato
        const idx = this.doctorConsents.findIndex(c => c.id === consent.id);
        if (idx !== -1) {
          this.doctorConsents[idx] = {
            ...this.doctorConsents[idx],
            status: 'REVOKED',
            revokedAt: new Date().toISOString()
          };
          this.doctorConsents = [...this.doctorConsents];
        }
        this.isLoading = false;
        this.successMessage = `Consenso revocato con successo. Il medico non potra' piu' accedere ai tuoi dati.`;
        this.closeRevokeModal();
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        console.error('Errore revoca consenso:', err);
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Errore durante la revoca del consenso.';
        this.closeRevokeModal();
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  canRevoke(consent: DoctorConsent): boolean {
    return consent.status === 'GRANTED';
  }

  refresh(): void {
    this.loadData();
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ConsentsService } from './consents.service';
import {
  ConsentScope,
  ConsentStatus,
  ConsentDto,
  DoctorDto,
  DoctorConsent
} from './dtos/consents.dto';

@Component({
  selector: 'app-consent-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './consent-management.component.html'
})
export class ConsentManagementComponent implements OnInit {

  constructor(private consentsService: ConsentsService) {}

  // Dati
  availableDoctors: DoctorDto[] = [];
  doctorConsents: DoctorConsent[] = [];
  private doctorsMap = new Map<number, DoctorDto>();

  // Stato UI
  isLoading = false;
  showGrantModal = false;
  showRevokeModal = false;
  showViewModal = false;
  showEditModal = false;
  successMessage = '';
  errorMessage = '';
  editErrorMessage = '';
  loadError = '';

  // Form per nuovo consenso (selezione multipla scope)
  grantForm = {
    doctorId: null as number | null,
    scopes: ['DOCS'] as ConsentScope[],
    hasExpiry: false,
    expiryDate: ''
  };

  // Lista degli scope disponibili per selezione multipla
  availableScopes: ConsentScope[] = ['DOCS', 'PRESCRIPTIONS', 'TELEVISIT'];

  // Consenso selezionato per revoca/visualizzazione/modifica
  selectedConsentForRevoke: DoctorConsent | null = null;
  selectedConsentForView: DoctorConsent | null = null;
  selectedConsentForEdit: DoctorConsent | null = null;

  // Form per modifica consenso
  editForm = {
    hasExpiry: false,
    expiryDate: ''
  };

  // Data minima per scadenza (domani)
  minExpiryDate = new Date(Date.now() + 86400000).toISOString().split('T')[0];

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

    this.consentsService.loadData().subscribe({
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
      scopes: [],
      hasExpiry: false,
      expiryDate: ''
    };
    this.errorMessage = '';
    this.showGrantModal = true;
  }

  /** Toggle selezione scope (per selezione multipla) */
  toggleScope(scope: ConsentScope): void {
    const idx = this.grantForm.scopes.indexOf(scope);
    if (idx === -1) {
      this.grantForm.scopes = [...this.grantForm.scopes, scope];
    } else {
      this.grantForm.scopes = this.grantForm.scopes.filter(s => s !== scope);
    }
  }

  /** Verifica se uno scope e' selezionato */
  isScopeSelected(scope: ConsentScope): boolean {
    return this.grantForm.scopes.includes(scope);
  }

  /** Seleziona tutti gli scope */
  selectAllScopes(): void {
    this.grantForm.scopes = [...this.availableScopes];
  }

  /** Deseleziona tutti gli scope */
  deselectAllScopes(): void {
    this.grantForm.scopes = [];
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

  openViewModal(consent: DoctorConsent): void {
    this.selectedConsentForView = consent;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedConsentForView = null;
  }

  openEditModal(consent: DoctorConsent): void {
    this.selectedConsentForEdit = consent;
    this.editForm = {
      hasExpiry: !!consent.expiresAt,
      expiryDate: consent.expiresAt ? consent.expiresAt.split('T')[0] : ''
    };
    this.editErrorMessage = '';
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedConsentForEdit = null;
  }

  isExpired(dateStr: string): boolean {
    return new Date(dateStr) < new Date();
  }

  submitEdit(): void {
    if (!this.selectedConsentForEdit) return;

    const consent = this.selectedConsentForEdit;
    this.isLoading = true;
    this.editErrorMessage = '';

    const body = {
      doctorId: consent.doctorId,
      scope: consent.scope,
      expiresAt: this.editForm.hasExpiry && this.editForm.expiryDate
        ? new Date(this.editForm.expiryDate).toISOString()
        : null
    };

    this.consentsService.updateConsent(consent.doctorId, consent.scope, body).subscribe({
      next: (updated) => {
        const idx = this.doctorConsents.findIndex(c => c.id === consent.id);
        if (idx !== -1) {
          this.doctorConsents[idx] = {
            ...this.doctorConsents[idx],
            expiresAt: updated.expiresAt
          };
          this.doctorConsents = [...this.doctorConsents];
        }
        this.isLoading = false;
        this.closeEditModal();
        this.successMessage = 'Consenso modificato con successo.';
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        console.error('Errore modifica consenso:', err);
        this.isLoading = false;
        this.editErrorMessage = err.error?.message || 'Errore durante la modifica del consenso.';
      }
    });
  }

  submitGrant(): void {
    if (!this.grantForm.doctorId) {
      this.errorMessage = 'Seleziona un medico.';
      return;
    }

    if (this.grantForm.scopes.length === 0) {
      this.errorMessage = 'Seleziona almeno un ambito.';
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
      scopes: this.grantForm.scopes,
      expiresAt: this.grantForm.hasExpiry && this.grantForm.expiryDate
        ? new Date(this.grantForm.expiryDate).toISOString()
        : null
    };

    this.consentsService.grantBulkConsents(body).subscribe({
      next: (consents) => {
        const enriched: DoctorConsent[] = consents.map(consent => ({
          id: consent.id,
          doctorId: consent.doctorId,
          doctorName: `Dr. ${doctor.firstName} ${doctor.lastName}`,
          doctorSpecialty: doctor.departmentName,
          scope: consent.scope,
          status: consent.status,
          grantedAt: consent.grantedAt,
          expiresAt: consent.expiresAt
        }));
        this.doctorConsents = [...enriched, ...this.doctorConsents];
        this.isLoading = false;
        this.closeGrantModal();
        const scopeLabels = this.grantForm.scopes.map(s => this.getScopeLabel(s)).join(', ');
        this.successMessage = `Consensi concessi con successo a ${doctor.firstName} ${doctor.lastName} per: ${scopeLabels}`;
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        console.error('Errore concessione consensi:', err);
        this.isLoading = false;
        if (err.status === 409 || err.error?.message?.includes('esiste')) {
          this.errorMessage = 'Esiste gia\' un consenso attivo per questo medico e uno degli ambiti selezionati.';
        } else {
          this.errorMessage = err.error?.message || 'Errore durante la concessione dei consensi.';
        }
      }
    });
  }

  confirmRevoke(): void {
    if (!this.selectedConsentForRevoke) return;

    const consent = this.selectedConsentForRevoke;
    this.isLoading = true;

    this.consentsService.revokeConsent(consent.doctorId, consent.scope).subscribe({
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

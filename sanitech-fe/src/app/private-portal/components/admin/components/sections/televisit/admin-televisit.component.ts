import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { TelevisitRoomComponent } from '../../../../televisit/televisit-room.component';
import { TelevisitService } from './televisit.service';
import {
  TelevisitItem,
  TelevisitCreatePayload,
  TelevisitRoomConfig,
  DoctorItem,
  PatientItem,
  FacilityItem,
  DepartmentItem
} from './dtos/televisit.dto';

@Component({
  selector: 'app-admin-televisit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TelevisitRoomComponent],
  templateUrl: './admin-televisit.component.html'
})
export class AdminTelevisitComponent implements OnInit {

  // Stato
  isLoading = false;
  televisitError = '';
  televisits: TelevisitItem[] = [];

  // Dati di supporto
  doctors: DoctorItem[] = [];
  patients: PatientItem[] = [];
  facilities: FacilityItem[] = [];
  departments: DepartmentItem[] = [];

  // Form nuova televisita
  televisitForm = {
    doctorId: null as number | null,
    patientId: null as number | null,
    facilityCode: '',
    department: '',
    scheduledAt: ''
  };
  televisitPatientsFiltered: PatientItem[] = [];

  // Modali
  showAdminTelevisitModal = false;
  showEndTelevisitModal = false;
  showDeleteTelevisitModal = false;
  showCancelTelevisitModal = false;
  selectedTelevisitForAction: TelevisitItem | null = null;

  // Overlay stanza televisita
  showTelevisitRoomOverlay = false;
  televisitRoomConfig: TelevisitRoomConfig | null = null;

  // Paginazione
  currentPage = 1;
  pageSize = 10;
  pageSizeOptions = [5, 10, 20, 50];

  constructor(
    private televisitService: TelevisitService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAdminTelevisit();
  }

  // ===== CARICAMENTO DATI =====

  loadAdminTelevisit(): void {
    this.televisitError = '';
    this.isLoading = true;

    this.televisitService.loadPatients().subscribe({
      next: (patients) => this.patients = patients,
      error: () => this.patients = []
    });

    this.televisitService.loadDoctors().subscribe({
      next: (doctors) => this.doctors = doctors,
      error: () => this.doctors = []
    });

    this.televisitService.loadFacilities().subscribe({
      next: (facilities) => this.facilities = facilities,
      error: () => this.facilities = []
    });

    this.televisitService.loadDepartments().subscribe({
      next: (departments) => this.departments = departments,
      error: () => this.departments = []
    });

    this.televisitService.loadTelevisits().subscribe({
      next: (televisits) => {
        this.televisits = televisits;
        this.isLoading = false;
      },
      error: () => {
        this.televisitError = 'Impossibile caricare le televisite.';
        this.isLoading = false;
      }
    });
  }

  // ===== STATISTICHE TELEVISITE =====

  get televisiteProgrammateOggi(): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    return this.televisits.filter(tv => {
      const scheduled = new Date(tv.scheduledAt);
      return scheduled >= today && scheduled < tomorrow && tv.status === 'CREATED';
    }).length;
  }

  get televisiteConcluse(): number {
    return this.televisits.filter(tv => tv.status === 'ENDED').length;
  }

  get televisiteInAttesaPaziente(): number {
    return this.televisits.filter(tv => tv.status === 'ACTIVE').length;
  }

  // ===== CASCADING FORM LOGIC =====

  get televisitDepartmentsFiltered(): DepartmentItem[] {
    if (!this.televisitForm.facilityCode) return [];
    return this.departments.filter(d => d.facilityCode === this.televisitForm.facilityCode);
  }

  get televisitDoctorsFiltered(): DoctorItem[] {
    if (!this.televisitForm.department) return [];
    return this.doctors.filter(d => d.departmentCode === this.televisitForm.department);
  }

  onTelevisitFacilityChange(): void {
    this.televisitForm.department = '';
    this.televisitForm.doctorId = null;
    this.televisitForm.patientId = null;
    this.televisitPatientsFiltered = [];
  }

  onTelevisitDepartmentChange(): void {
    this.televisitForm.doctorId = null;
    this.televisitForm.patientId = null;
    this.televisitPatientsFiltered = [];
  }

  onTelevisitDoctorChange(): void {
    this.televisitForm.patientId = null;
    this.televisitPatientsFiltered = [];
    if (!this.televisitForm.doctorId) return;

    this.televisitService.loadPatientsWithTelevisitConsent(this.televisitForm.doctorId).subscribe({
      next: (patientIds) => {
        this.televisitPatientsFiltered = this.patients.filter(p => patientIds.includes(p.id));
      },
      error: () => {
        this.televisitPatientsFiltered = [];
      }
    });
  }

  // ===== SUBMIT TELEVISITA =====

  submitTelevisit(): void {
    this.isLoading = true;
    this.televisitError = '';

    if (!this.televisitForm.doctorId) {
      this.televisitError = 'Seleziona un medico.';
      this.isLoading = false;
      return;
    }
    if (!this.televisitForm.patientId) {
      this.televisitError = 'Seleziona un paziente.';
      this.isLoading = false;
      return;
    }
    if (!this.televisitForm.department?.trim()) {
      this.televisitError = 'Seleziona un reparto.';
      this.isLoading = false;
      return;
    }
    if (!this.televisitForm.scheduledAt) {
      this.televisitError = 'Seleziona data e ora della sessione.';
      this.isLoading = false;
      return;
    }

    const doctor = this.doctors.find(d => d.id === this.televisitForm.doctorId);
    const patient = this.televisitPatientsFiltered.find(p => p.id === this.televisitForm.patientId);

    if (!doctor || !patient) {
      this.televisitError = 'Medico o paziente non trovato.';
      this.isLoading = false;
      return;
    }

    const payload: TelevisitCreatePayload = {
      doctorSubject: `${doctor.firstName} ${doctor.lastName}`,
      patientSubject: `${patient.firstName} ${patient.lastName}`,
      department: this.televisitForm.department,
      scheduledAt: new Date(this.televisitForm.scheduledAt).toISOString()
    };

    this.televisitService.createTelevisit(payload).subscribe({
      next: (televisit) => {
        this.televisits = [...this.televisits, televisit];
        this.televisitForm = {
          doctorId: null,
          patientId: null,
          facilityCode: '',
          department: '',
          scheduledAt: ''
        };
        this.televisitPatientsFiltered = [];
        this.closeAdminTelevisitModal();
        this.isLoading = false;
      },
      error: (err) => {
        this.televisitError = err?.error?.message || 'Impossibile creare la sessione.';
        this.isLoading = false;
      }
    });
  }

  // ===== LABEL HELPERS =====

  getTelevisitStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      CREATED: 'Creata',
      SCHEDULED: 'Programmata',
      ACTIVE: 'In corso',
      ENDED: 'Conclusa',
      CANCELED: 'Annullata'
    };
    return labels[status] ?? status;
  }

  getTelevisitPatientLabel(televisit: TelevisitItem): string {
    if (!televisit.patientSubject) return '-';
    return televisit.patientSubject.length > 20
      ? televisit.patientSubject.substring(0, 17) + '...'
      : televisit.patientSubject;
  }

  getTelevisitDoctorLabel(televisit: TelevisitItem): string {
    if (!televisit.doctorSubject) return '-';
    return televisit.doctorSubject.length > 20
      ? televisit.doctorSubject.substring(0, 17) + '...'
      : televisit.doctorSubject;
  }

  getTelevisitDateTimeLabel(televisit: TelevisitItem): string {
    if (!televisit.scheduledAt) return '-';
    return this.formatDateTime(televisit.scheduledAt);
  }

  getDepartmentLabel(code: string): string {
    const department = this.departments.find((item) => item.code === code);
    const labels: Record<string, string> = {
      CARD: 'Cardiologia',
      NEURO: 'Neurologia',
      DERM: 'Dermatologia',
      ORTHO: 'Ortopedia',
      PNEUMO: 'Pneumologia',
      HEART: 'Cardiologia',
      METAB: 'Metabolismo',
      RESP: 'Pneumologia'
    };
    const label = department?.name ?? labels[code] ?? code;
    return this.toUpperCamelCase(label);
  }

  formatDateTime(value: string): string {
    if (!value) return '-';
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) return value;
    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    const year = parsed.getFullYear();
    const hours = String(parsed.getHours()).padStart(2, '0');
    const minutes = String(parsed.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }

  private toUpperCamelCase(value: string): string {
    const trimmed = value?.trim();
    if (!trimmed) return value;
    const lower = trimmed.toLowerCase();
    return `${lower.charAt(0).toUpperCase()}${lower.slice(1)}`;
  }

  // ===== COPY LINK =====

  copyTelevisitLink(televisit: TelevisitItem): void {
    const link = `${window.location.origin}/televisit/room?id=${televisit.id}&room=${televisit.roomName}`;
    navigator.clipboard.writeText(link).then(() => {
      this.showToast('Link copiato negli appunti', 'success');
    }).catch(() => {
      this.showToast('Errore durante la copia del link', 'danger');
    });
  }

  // ===== SESSIONE TELEVISITA / LIVEKIT =====

  startTelevisitSession(televisit: TelevisitItem): void {
    if (televisit.status === 'ENDED' || televisit.status === 'CANCELED') {
      this.showToast('La televisita non può essere avviata in questo stato', 'warning');
      return;
    }

    this.isLoading = true;
    this.televisitError = '';

    if (televisit.status === 'ACTIVE') {
      this.fetchTokenAndNavigate(televisit);
      return;
    }

    this.televisitService.startTelevisitSession(televisit.id).subscribe({
      next: () => {
        this.televisits = this.televisits.map(tv =>
          tv.id === televisit.id ? { ...tv, status: 'ACTIVE' as const } : tv
        );
        this.fetchTokenAndNavigate(televisit);
      },
      error: (err) => {
        this.isLoading = false;
        this.televisitError = err?.error?.message || 'Errore nell\'avvio della televisita.';
        this.showToast('Errore nell\'avvio della televisita', 'danger');
      }
    });
  }

  private fetchTokenAndNavigate(televisit: TelevisitItem): void {
    this.televisitService.fetchDoctorToken(televisit.id).subscribe({
      next: (tokenResponse) => {
        this.isLoading = false;
        this.televisitRoomConfig = {
          id: String(televisit.id),
          room: tokenResponse.roomName,
          url: tokenResponse.livekitUrl,
          token: tokenResponse.token
        };
        this.showTelevisitRoomOverlay = true;
      },
      error: () => {
        this.isLoading = false;
        this.televisitError = 'Errore nel recupero del token LiveKit.';
        this.showToast('Errore nel recupero del token LiveKit', 'danger');
      }
    });
  }

  closeTelevisitRoomOverlay(): void {
    this.showTelevisitRoomOverlay = false;
    this.televisitRoomConfig = null;
  }

  // ===== MODALE NUOVA TELEVISITA =====

  openAdminTelevisitModal(): void {
    this.televisitError = '';
    this.showAdminTelevisitModal = true;
  }

  closeAdminTelevisitModal(): void {
    this.showAdminTelevisitModal = false;
    this.televisitError = '';
  }

  // ===== MODALE CONCLUDI TELEVISITA =====

  openEndTelevisitModal(televisit: TelevisitItem): void {
    if (televisit.status !== 'ACTIVE') {
      this.showToast('Solo le televisite attive possono essere concluse', 'warning');
      return;
    }
    this.selectedTelevisitForAction = televisit;
    this.showEndTelevisitModal = true;
  }

  closeEndTelevisitModal(): void {
    this.showEndTelevisitModal = false;
    this.selectedTelevisitForAction = null;
  }

  confirmEndTelevisit(): void {
    if (!this.selectedTelevisitForAction) return;
    this.isLoading = true;
    this.televisitService.endTelevisit(this.selectedTelevisitForAction.id).subscribe({
      next: () => {
        this.televisits = this.televisits.map(tv =>
          tv.id === this.selectedTelevisitForAction!.id ? { ...tv, status: 'ENDED' as const } : tv
        );
        this.isLoading = false;
        this.showToast('Televisita conclusa', 'success');
        this.closeEndTelevisitModal();
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast(err?.error?.message || 'Errore nella conclusione della televisita', 'danger');
      }
    });
  }

  // ===== MODALE ELIMINA TELEVISITA =====

  openDeleteTelevisitModal(televisit: TelevisitItem): void {
    if (televisit.status === 'ACTIVE' || televisit.status === 'ENDED') {
      this.showToast('La televisita non può essere eliminata in questo stato', 'warning');
      return;
    }
    this.selectedTelevisitForAction = televisit;
    this.showDeleteTelevisitModal = true;
  }

  closeDeleteTelevisitModal(): void {
    this.showDeleteTelevisitModal = false;
    this.selectedTelevisitForAction = null;
  }

  confirmDeleteTelevisit(): void {
    if (!this.selectedTelevisitForAction) return;
    this.isLoading = true;
    this.televisitService.deleteTelevisit(this.selectedTelevisitForAction.id).subscribe({
      next: () => {
        this.televisits = this.televisits.filter(tv => tv.id !== this.selectedTelevisitForAction!.id);
        this.isLoading = false;
        this.showToast('Televisita eliminata con successo', 'success');
        this.closeDeleteTelevisitModal();
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast(err?.error?.message || 'Errore nell\'eliminazione della televisita', 'danger');
      }
    });
  }

  // ===== MODALE ANNULLA TELEVISITA =====

  openCancelTelevisitModal(televisit: TelevisitItem): void {
    if (televisit.status === 'ENDED' || televisit.status === 'CANCELED') {
      this.showToast('La televisita non può essere annullata in questo stato', 'warning');
      return;
    }
    this.selectedTelevisitForAction = televisit;
    this.showCancelTelevisitModal = true;
  }

  closeCancelTelevisitModal(): void {
    this.showCancelTelevisitModal = false;
    this.selectedTelevisitForAction = null;
  }

  confirmCancelTelevisit(): void {
    if (!this.selectedTelevisitForAction) return;
    this.isLoading = true;
    this.televisitService.cancelTelevisit(this.selectedTelevisitForAction.id).subscribe({
      next: () => {
        this.televisits = this.televisits.map(tv =>
          tv.id === this.selectedTelevisitForAction!.id ? { ...tv, status: 'CANCELED' as const } : tv
        );
        this.isLoading = false;
        this.showToast('Televisita annullata', 'success');
        this.closeCancelTelevisitModal();
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast(err?.error?.message || 'Errore nell\'annullamento della televisita', 'danger');
      }
    });
  }

  // ===== PAGINAZIONE =====

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.televisits.length / this.pageSize));
  }

  get sliceStart(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  get sliceEnd(): number {
    return this.currentPage * this.pageSize;
  }

  setPage(page: number): void {
    this.currentPage = Math.min(Math.max(1, page), this.totalPages);
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
  }

  // ===== TOAST =====

  private showToast(message: string, type: 'success' | 'danger' | 'warning' | 'info'): void {
    const toastContainer = document.getElementById('toast-container') || this.createToastContainer();
    const toast = document.createElement('div');
    toast.className = `toast show align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
      <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    `;
    toastContainer.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
  }

  private createToastContainer(): HTMLElement {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    container.style.zIndex = '1100';
    document.body.appendChild(container);
    return container;
  }
}

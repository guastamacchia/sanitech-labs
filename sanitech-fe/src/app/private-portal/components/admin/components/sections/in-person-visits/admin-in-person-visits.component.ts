import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { InPersonVisitsService } from './in-person-visits.service';
import {
  InPersonVisitItem,
  AvailableSlotItem,
  DoctorItem,
  PatientItem,
  FacilityItem,
  DepartmentItem,
  VisitCreatePayload
} from './dtos/in-person-visits.dto';

@Component({
  selector: 'app-admin-in-person-visits',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-in-person-visits.component.html'
})
export class AdminInPersonVisitsComponent implements OnInit {

  // Stato principale
  isLoading = false;
  inPersonVisits: InPersonVisitItem[] = [];
  filteredInPersonVisits: InPersonVisitItem[] = [];
  inPersonVisitsError = '';
  inPersonVisitsSuccess = '';
  inPersonVisitStatusFilter: 'ALL' | 'BOOKED' | 'COMPLETED' | 'CANCELLED' = 'ALL';

  // Modali
  showAdminInPersonVisitModal = false;
  showCompleteInPersonVisitModal = false;
  showCancelInPersonVisitModal = false;
  showDeleteInPersonVisitModal = false;
  showRescheduleInPersonVisitModal = false;
  showReassignInPersonVisitModal = false;
  selectedInPersonVisit: InPersonVisitItem | null = null;

  // Ripianifica
  rescheduleSlotId: number | null = null;
  rescheduleSlotsFiltered: AvailableSlotItem[] = [];

  // Riassegna medico
  reassignDoctorId: number | null = null;
  reassignSlotId: number | null = null;
  reassignSlotsFiltered: AvailableSlotItem[] = [];

  // Form nuova visita (cascata)
  inPersonVisitForm = {
    facilityCode: '',
    department: '',
    doctorId: null as number | null,
    patientId: null as number | null,
    slotId: null as number | null,
    reason: ''
  };
  inPersonVisitPatientsFiltered: PatientItem[] = [];
  inPersonVisitSlotsFiltered: AvailableSlotItem[] = [];

  // Dati di supporto
  doctors: DoctorItem[] = [];
  patients: PatientItem[] = [];
  facilities: FacilityItem[] = [];
  departments: DepartmentItem[] = [];

  // Paginazione locale
  currentPage = 1;
  pageSize = 10;
  pageSizeOptions = [5, 10, 20, 50];

  constructor(private inPersonVisitsService: InPersonVisitsService) {}

  ngOnInit(): void {
    this.loadAdminInPersonVisits();
  }

  // ===== CARICAMENTO DATI =====

  loadAdminInPersonVisits(): void {
    this.inPersonVisitsError = '';
    this.isLoading = true;

    this.inPersonVisitsService.loadPatients().subscribe({
      next: (patients) => this.patients = patients,
      error: () => this.patients = []
    });

    this.inPersonVisitsService.loadDoctors().subscribe({
      next: (doctors) => this.doctors = doctors,
      error: () => this.doctors = []
    });

    this.inPersonVisitsService.loadFacilities().subscribe({
      next: (facilities) => this.facilities = facilities,
      error: () => this.facilities = []
    });

    this.inPersonVisitsService.loadDepartments().subscribe({
      next: (departments) => this.departments = departments,
      error: () => this.departments = []
    });

    this.inPersonVisitsService.loadInPersonVisits().subscribe({
      next: (visits) => {
        this.inPersonVisits = visits;
        this.applyInPersonVisitFilter();
        this.isLoading = false;
      },
      error: () => {
        this.inPersonVisitsError = 'Impossibile caricare le visite in presenza.';
        this.isLoading = false;
      }
    });
  }

  // ===== FILTRO PER STATO =====

  applyInPersonVisitFilter(): void {
    if (this.inPersonVisitStatusFilter === 'ALL') {
      this.filteredInPersonVisits = [...this.inPersonVisits];
    } else {
      this.filteredInPersonVisits = this.inPersonVisits.filter(v => v.status === this.inPersonVisitStatusFilter);
    }
  }

  onInPersonVisitStatusFilterChange(): void {
    this.applyInPersonVisitFilter();
    this.currentPage = 1;
  }

  // ===== STATISTICHE =====

  get inPersonVisiteTotali(): number {
    return this.inPersonVisits.length;
  }

  get inPersonVisitePrenotateOggi(): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    return this.inPersonVisits.filter(v => {
      const d = new Date(v.startAt);
      return d >= today && d < tomorrow && v.status === 'BOOKED';
    }).length;
  }

  get inPersonVisiteCompletate(): number {
    return this.inPersonVisits.filter(v => v.status === 'COMPLETED').length;
  }

  get inPersonVisiteCancellate(): number {
    return this.inPersonVisits.filter(v => v.status === 'CANCELLED').length;
  }

  // ===== HELPER VISITE IN PRESENZA =====

  getInPersonVisitPatientName(visit: InPersonVisitItem): string {
    const p = this.patients.find(pt => pt.id === visit.patientId);
    return p ? `${p.firstName} ${p.lastName}` : `Paziente #${visit.patientId}`;
  }

  getInPersonVisitDoctorName(visit: InPersonVisitItem): string {
    const d = this.doctors.find(doc => doc.id === visit.doctorId);
    return d ? `${d.firstName} ${d.lastName}` : `Medico #${visit.doctorId}`;
  }

  getInPersonVisitDepartmentName(code: string): string {
    const dept = this.departments.find(d => d.code === code);
    return dept ? dept.name : code;
  }

  getInPersonVisitStatusLabel(status: string): string {
    switch (status) {
      case 'BOOKED': return 'Prenotata';
      case 'COMPLETED': return 'Completata';
      case 'CANCELLED': return 'Annullata';
      default: return status;
    }
  }

  formatInPersonVisitDate(isoDate: string): string {
    return new Date(isoDate).toLocaleDateString('it-IT', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  // ===== CASCATA FORM NUOVA VISITA =====

  get inPersonVisitDepartmentsFiltered(): DepartmentItem[] {
    if (!this.inPersonVisitForm.facilityCode) return [];
    return this.departments.filter(d => d.facilityCode === this.inPersonVisitForm.facilityCode);
  }

  get inPersonVisitDoctorsFiltered(): DoctorItem[] {
    if (!this.inPersonVisitForm.department) return [];
    return this.doctors.filter(d => d.departmentCode === this.inPersonVisitForm.department);
  }

  onInPersonVisitFacilityChange(): void {
    this.inPersonVisitForm.department = '';
    this.inPersonVisitForm.doctorId = null;
    this.inPersonVisitForm.patientId = null;
    this.inPersonVisitForm.slotId = null;
    this.inPersonVisitPatientsFiltered = [];
    this.inPersonVisitSlotsFiltered = [];
  }

  onInPersonVisitDepartmentChange(): void {
    this.inPersonVisitForm.doctorId = null;
    this.inPersonVisitForm.patientId = null;
    this.inPersonVisitForm.slotId = null;
    this.inPersonVisitPatientsFiltered = [];
    this.inPersonVisitSlotsFiltered = [];
  }

  onInPersonVisitDoctorChange(): void {
    this.inPersonVisitForm.patientId = null;
    this.inPersonVisitForm.slotId = null;
    this.inPersonVisitPatientsFiltered = [];
    this.inPersonVisitSlotsFiltered = [];

    if (!this.inPersonVisitForm.doctorId) return;

    // Carica slot disponibili per il medico selezionato
    this.inPersonVisitsService.loadAvailableSlots(this.inPersonVisitForm.doctorId).subscribe({
      next: (slots) => {
        this.inPersonVisitSlotsFiltered = slots;
      },
      error: () => {
        this.inPersonVisitSlotsFiltered = [];
      }
    });

    // Carica tutti i pazienti (non servono consensi per visite in presenza)
    this.inPersonVisitPatientsFiltered = [...this.patients];
  }

  formatSlotLabel(slot: AvailableSlotItem): string {
    const start = new Date(slot.startAt);
    const end = new Date(slot.endAt);
    return start.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric' })
      + ' ' + start.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
      + ' - ' + end.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
  }

  // ===== MODALE NUOVA VISITA =====

  openAdminInPersonVisitModal(): void {
    this.inPersonVisitsError = '';
    this.showAdminInPersonVisitModal = true;
  }

  closeAdminInPersonVisitModal(): void {
    this.showAdminInPersonVisitModal = false;
    this.inPersonVisitsError = '';
    this.inPersonVisitForm = {
      facilityCode: '',
      department: '',
      doctorId: null,
      patientId: null,
      slotId: null,
      reason: ''
    };
    this.inPersonVisitPatientsFiltered = [];
    this.inPersonVisitSlotsFiltered = [];
  }

  submitInPersonVisit(): void {
    this.isLoading = true;
    this.inPersonVisitsError = '';

    if (!this.inPersonVisitForm.slotId) {
      this.inPersonVisitsError = 'Seleziona uno slot disponibile.';
      this.isLoading = false;
      return;
    }
    if (!this.inPersonVisitForm.patientId) {
      this.inPersonVisitsError = 'Seleziona un paziente.';
      this.isLoading = false;
      return;
    }

    const payload: VisitCreatePayload = {
      slotId: this.inPersonVisitForm.slotId,
      patientId: this.inPersonVisitForm.patientId,
      reason: this.inPersonVisitForm.reason?.trim() || null
    };

    this.inPersonVisitsService.createVisit(payload).subscribe({
      next: (visit) => {
        this.inPersonVisits = [...this.inPersonVisits, visit];
        this.applyInPersonVisitFilter();
        this.closeAdminInPersonVisitModal();
        this.inPersonVisitsSuccess = 'Visita prenotata con successo.';
        this.isLoading = false;
      },
      error: (err) => {
        this.inPersonVisitsError = err?.error?.message || 'Impossibile prenotare la visita.';
        this.isLoading = false;
      }
    });
  }

  // ===== MODALE COMPLETA VISITA =====

  openCompleteInPersonVisitModal(visit: InPersonVisitItem): void {
    this.selectedInPersonVisit = visit;
    this.inPersonVisitsError = '';
    this.showCompleteInPersonVisitModal = true;
  }

  closeCompleteInPersonVisitModal(): void {
    this.showCompleteInPersonVisitModal = false;
    this.selectedInPersonVisit = null;
  }

  confirmCompleteInPersonVisit(): void {
    if (!this.selectedInPersonVisit) return;
    this.isLoading = true;
    this.inPersonVisitsError = '';

    this.inPersonVisitsService.completeVisit(this.selectedInPersonVisit.id).subscribe({
      next: () => {
        const idx = this.inPersonVisits.findIndex(v => v.id === this.selectedInPersonVisit!.id);
        if (idx !== -1) {
          this.inPersonVisits[idx] = { ...this.inPersonVisits[idx], status: 'COMPLETED' };
          this.inPersonVisits = [...this.inPersonVisits];
        }
        this.applyInPersonVisitFilter();
        this.inPersonVisitsSuccess = 'Visita completata. Evento di pagamento generato.';
        this.closeCompleteInPersonVisitModal();
        this.isLoading = false;
      },
      error: (err) => {
        this.inPersonVisitsError = err?.error?.message || 'Impossibile completare la visita.';
        this.isLoading = false;
      }
    });
  }

  // ===== MODALE ANNULLA VISITA =====

  openCancelInPersonVisitModal(visit: InPersonVisitItem): void {
    this.selectedInPersonVisit = visit;
    this.inPersonVisitsError = '';
    this.showCancelInPersonVisitModal = true;
  }

  closeCancelInPersonVisitModal(): void {
    this.showCancelInPersonVisitModal = false;
    this.selectedInPersonVisit = null;
  }

  confirmCancelInPersonVisit(): void {
    if (!this.selectedInPersonVisit) return;
    this.isLoading = true;
    this.inPersonVisitsError = '';

    this.inPersonVisitsService.cancelVisit(this.selectedInPersonVisit.id).subscribe({
      next: () => {
        const idx = this.inPersonVisits.findIndex(v => v.id === this.selectedInPersonVisit!.id);
        if (idx !== -1) {
          this.inPersonVisits[idx] = { ...this.inPersonVisits[idx], status: 'CANCELLED' };
          this.inPersonVisits = [...this.inPersonVisits];
        }
        this.applyInPersonVisitFilter();
        this.closeCancelInPersonVisitModal();
        this.inPersonVisitsSuccess = 'Visita annullata con successo.';
        this.isLoading = false;
      },
      error: (err) => {
        this.inPersonVisitsError = err?.error?.message || 'Impossibile annullare la visita.';
        this.isLoading = false;
      }
    });
  }

  // ===== MODALE ELIMINA VISITA =====

  openDeleteInPersonVisitModal(visit: InPersonVisitItem): void {
    this.selectedInPersonVisit = visit;
    this.inPersonVisitsError = '';
    this.showDeleteInPersonVisitModal = true;
  }

  closeDeleteInPersonVisitModal(): void {
    this.showDeleteInPersonVisitModal = false;
    this.selectedInPersonVisit = null;
  }

  confirmDeleteInPersonVisit(): void {
    if (!this.selectedInPersonVisit) return;
    this.isLoading = true;
    this.inPersonVisitsError = '';

    this.inPersonVisitsService.deleteVisit(this.selectedInPersonVisit.id).subscribe({
      next: () => {
        this.inPersonVisits = this.inPersonVisits.filter(v => v.id !== this.selectedInPersonVisit!.id);
        this.applyInPersonVisitFilter();
        this.closeDeleteInPersonVisitModal();
        this.inPersonVisitsSuccess = 'Visita eliminata definitivamente.';
        this.isLoading = false;
      },
      error: (err) => {
        this.inPersonVisitsError = err?.error?.message || 'Impossibile eliminare la visita.';
        this.isLoading = false;
      }
    });
  }

  // ===== MODALE RIPIANIFICA VISITA =====

  openRescheduleInPersonVisitModal(visit: InPersonVisitItem): void {
    this.selectedInPersonVisit = visit;
    this.inPersonVisitsError = '';
    this.rescheduleSlotId = null;
    this.rescheduleSlotsFiltered = [];
    this.showRescheduleInPersonVisitModal = true;

    // Carica slot disponibili per lo stesso medico
    this.inPersonVisitsService.loadRescheduleSlots(visit.doctorId).subscribe({
      next: (slots) => {
        this.rescheduleSlotsFiltered = slots;
      },
      error: () => {
        this.rescheduleSlotsFiltered = [];
      }
    });
  }

  closeRescheduleInPersonVisitModal(): void {
    this.showRescheduleInPersonVisitModal = false;
    this.selectedInPersonVisit = null;
    this.rescheduleSlotId = null;
    this.rescheduleSlotsFiltered = [];
  }

  confirmRescheduleInPersonVisit(): void {
    if (!this.selectedInPersonVisit || !this.rescheduleSlotId) return;
    this.isLoading = true;
    this.inPersonVisitsError = '';

    this.inPersonVisitsService.rescheduleVisit(this.selectedInPersonVisit.id, this.rescheduleSlotId).subscribe({
      next: (updated) => {
        const idx = this.inPersonVisits.findIndex(v => v.id === updated.id);
        if (idx !== -1) {
          this.inPersonVisits[idx] = updated;
          this.inPersonVisits = [...this.inPersonVisits];
        }
        this.applyInPersonVisitFilter();
        this.closeRescheduleInPersonVisitModal();
        this.inPersonVisitsSuccess = 'Visita ripianificata con successo.';
        this.isLoading = false;
      },
      error: (err) => {
        this.inPersonVisitsError = err?.error?.message || 'Impossibile ripianificare la visita.';
        this.isLoading = false;
      }
    });
  }

  // ===== MODALE CAMBIO MEDICO =====

  openReassignInPersonVisitModal(visit: InPersonVisitItem): void {
    this.selectedInPersonVisit = visit;
    this.inPersonVisitsError = '';
    this.reassignDoctorId = null;
    this.reassignSlotId = null;
    this.reassignSlotsFiltered = [];
    this.showReassignInPersonVisitModal = true;
  }

  closeReassignInPersonVisitModal(): void {
    this.showReassignInPersonVisitModal = false;
    this.selectedInPersonVisit = null;
    this.reassignDoctorId = null;
    this.reassignSlotId = null;
    this.reassignSlotsFiltered = [];
  }

  onReassignDoctorChange(): void {
    this.reassignSlotId = null;
    this.reassignSlotsFiltered = [];
    if (!this.reassignDoctorId) return;

    this.inPersonVisitsService.loadRescheduleSlots(this.reassignDoctorId).subscribe({
      next: (slots) => {
        this.reassignSlotsFiltered = slots;
      },
      error: () => {
        this.reassignSlotsFiltered = [];
      }
    });
  }

  confirmReassignInPersonVisit(): void {
    if (!this.selectedInPersonVisit || !this.reassignDoctorId || !this.reassignSlotId) return;
    this.isLoading = true;
    this.inPersonVisitsError = '';

    this.inPersonVisitsService.reassignVisit(this.selectedInPersonVisit.id, this.reassignDoctorId, this.reassignSlotId).subscribe({
      next: (updated) => {
        const idx = this.inPersonVisits.findIndex(v => v.id === updated.id);
        if (idx !== -1) {
          this.inPersonVisits[idx] = updated;
          this.inPersonVisits = [...this.inPersonVisits];
        }
        this.applyInPersonVisitFilter();
        this.closeReassignInPersonVisitModal();
        this.inPersonVisitsSuccess = 'Medico riassegnato con successo.';
        this.isLoading = false;
      },
      error: (err) => {
        this.inPersonVisitsError = err?.error?.message || 'Impossibile riassegnare il medico.';
        this.isLoading = false;
      }
    });
  }

  // ===== PAGINAZIONE =====

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredInPersonVisits.length / this.pageSize));
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
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdmissionsService } from './admissions.service';
import {
  AdmissionItem,
  CapacityItem,
  DoctorItem,
  PatientItem,
  FacilityItem,
  DepartmentItem,
  AdmissionCreatePayload
} from './dtos/admissions.dto';

@Component({
  selector: 'app-admin-admissions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-admissions.component.html'
})
export class AdminAdmissionsComponent implements OnInit {

  // Stato generale
  isLoading = false;
  admissionsError = '';
  isCapacityCollapsed = true;

  // Dati principali
  admissions: AdmissionItem[] = [];
  departmentCapacities: CapacityItem[] = [];

  // Dati di supporto
  doctors: DoctorItem[] = [];
  patients: PatientItem[] = [];
  facilities: FacilityItem[] = [];
  departments: DepartmentItem[] = [];

  // Paginazione
  pageSize = 10;
  pageSizeOptions = [5, 10, 20, 50];
  currentPage = 1;

  // Modale nuovo ricovero
  showAdminAdmissionModal = false;
  admissionForm = {
    patientId: null as number | null,
    facilityCode: '',
    departmentCode: '',
    admissionType: 'INPATIENT' as 'INPATIENT' | 'DAY_HOSPITAL' | 'OBSERVATION',
    notes: '',
    attendingDoctorId: null as number | null
  };
  filteredAdmissionPatients: PatientItem[] = [];

  // Modale cambia referente
  showChangeReferentModal = false;
  changeReferentTarget: AdmissionItem | null = null;
  changeReferentDoctors: DoctorItem[] = [];
  changeReferentSelectedDoctorId: number | null = null;
  changeReferentError = '';

  // Modale dimissione
  showDischargeModal = false;
  dischargeTarget: AdmissionItem | null = null;
  dischargeError = '';

  // Modale note ricovero
  showAdmissionEditNoteModal = false;
  admissionEditNoteTarget: AdmissionItem | null = null;
  admissionEditNoteText = '';
  admissionEditNoteError = '';

  constructor(private admissionsService: AdmissionsService) {}

  ngOnInit(): void {
    this.loadAdminAdmissions();
  }

  // ===== CARICAMENTO DATI =====

  loadAdminAdmissions(): void {
    this.admissionsError = '';
    this.isLoading = true;

    this.admissionsService.loadPatients().subscribe({
      next: (patients) => this.patients = patients,
      error: () => this.patients = []
    });

    this.admissionsService.loadDoctors().subscribe({
      next: (doctors) => this.doctors = doctors,
      error: () => this.doctors = []
    });

    this.admissionsService.loadFacilities().subscribe({
      next: (facilities) => this.facilities = facilities,
      error: () => this.facilities = []
    });

    this.admissionsService.loadDepartments().subscribe({
      next: (departments) => this.departments = departments,
      error: () => this.departments = []
    });

    this.admissionsService.loadAdmissions().subscribe({
      next: (admissions) => {
        this.admissions = admissions;
        this.isLoading = false;
      },
      error: () => {
        this.admissionsError = 'Impossibile caricare i ricoveri.';
        this.isLoading = false;
      }
    });

    this.admissionsService.loadDepartmentCapacity().subscribe({
      next: (capacities) => this.departmentCapacities = capacities,
      error: () => { /* Fallimento silenzioso - non critico */ }
    });
  }

  // ===== STATISTICHE RICOVERI =====

  get ricoveriAttivi(): number {
    return this.admissions.filter(adm => adm.status === 'ACTIVE').length;
  }

  get ricoveriDayHospital(): number {
    return this.admissions.filter(adm => adm.admissionType === 'DAY_HOSPITAL' && adm.status === 'ACTIVE').length;
  }

  get ricoveriOrdinari(): number {
    return this.admissions.filter(adm => adm.admissionType === 'INPATIENT' && adm.status === 'ACTIVE').length;
  }

  get ricoveriOsservazione(): number {
    return this.admissions.filter(adm => adm.admissionType === 'OBSERVATION' && adm.status === 'ACTIVE').length;
  }

  get dimessiQuestoMese(): number {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    return this.admissions.filter(adm => {
      if (adm.status !== 'DISCHARGED' || !adm.dischargedAt) return false;
      const discharged = new Date(adm.dischargedAt);
      return discharged >= startOfMonth;
    }).length;
  }

  // ===== PAGINAZIONE =====

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.admissions.length / this.pageSize));
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

  // ===== LABEL HELPERS =====

  getPatientLabel(patientId: number | null | undefined): string {
    if (patientId == null) return '-';
    const patient = this.patients.find(item => item.id === patientId);
    return patient ? `${patient.firstName} ${patient.lastName}` : `Paziente ${patientId}`;
  }

  getDoctorLabel(doctorId: number | null | undefined): string {
    if (doctorId == null) return '-';
    const doctor = this.doctors.find(item => item.id === doctorId);
    return doctor ? `${doctor.firstName} ${doctor.lastName}` : `Medico ${doctorId}`;
  }

  getFacilityLabelByDepartment(departmentCode: string | null | undefined): string {
    if (!departmentCode) return '-';
    const department = this.departments.find(item => item.code === departmentCode);
    if (!department?.facilityCode) return '-';
    const facility = this.facilities.find(item => item.code === department.facilityCode);
    return facility?.name ?? department.facilityCode;
  }

  getDepartmentLabel(code: string | null | undefined): string {
    if (!code) return '-';
    const department = this.departments.find(item => item.code === code);
    return department?.name ?? code;
  }

  getAdmissionStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Attivo',
      DISCHARGED: 'Dimesso',
      CANCELLED: 'Annullato'
    };
    return labels[status] ?? status;
  }

  getAdmissionTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      INPATIENT: 'Ordinario',
      DAY_HOSPITAL: 'Day Hospital',
      OBSERVATION: 'Osservazione'
    };
    return labels[type] ?? type;
  }

  formatDate(value: string | null | undefined): string {
    if (!value) return '-';
    const normalizedValue = value.trim();
    if (/^\d{2}\/\d{2}\/\d{4}$/.test(normalizedValue)) return normalizedValue;
    const isoMatch = normalizedValue.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (isoMatch) return `${isoMatch[3]}/${isoMatch[2]}/${isoMatch[1]}`;
    const parsed = new Date(normalizedValue);
    if (Number.isNaN(parsed.getTime())) return normalizedValue;
    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    return `${day}/${month}/${parsed.getFullYear()}`;
  }

  private toUpperCamelCase(value: string): string {
    const trimmed = value?.trim();
    if (!trimmed) return value;
    const lower = trimmed.toLowerCase();
    return `${lower.charAt(0).toUpperCase()}${lower.slice(1)}`;
  }

  // ===== MODALE NUOVO RICOVERO =====

  openAdminAdmissionModal(): void {
    this.admissionsError = '';
    this.showAdminAdmissionModal = true;
  }

  closeAdminAdmissionModal(): void {
    this.showAdminAdmissionModal = false;
    this.admissionsError = '';
  }

  get filteredDepartments(): DepartmentItem[] {
    if (!this.admissionForm.facilityCode) return [];
    return this.departments.filter(dept => dept.facilityCode === this.admissionForm.facilityCode);
  }

  get filteredDoctors(): DoctorItem[] {
    if (!this.admissionForm.departmentCode) return [];
    return this.doctors.filter(doctor => doctor.departmentCode === this.admissionForm.departmentCode);
  }

  get selectedDepartmentAvailableBeds(): number | null {
    if (!this.admissionForm.departmentCode) return null;
    const capacity = this.departmentCapacities.find(
      c => c.departmentCode.toUpperCase() === this.admissionForm.departmentCode.toUpperCase()
    );
    return capacity?.availableBeds ?? null;
  }

  onAdmissionFacilityChange(): void {
    this.admissionForm.departmentCode = '';
    this.admissionForm.attendingDoctorId = null;
    this.admissionForm.patientId = null;
    this.filteredAdmissionPatients = [];
  }

  onAdmissionDepartmentChange(): void {
    this.admissionForm.attendingDoctorId = null;
    this.admissionForm.patientId = null;
    this.filteredAdmissionPatients = [];
  }

  onAdmissionDoctorChange(): void {
    this.admissionForm.patientId = null;
    this.filteredAdmissionPatients = [];

    if (!this.admissionForm.attendingDoctorId) return;

    this.admissionsService.loadPatientsWithConsent(this.admissionForm.attendingDoctorId).subscribe({
      next: (patientIds) => {
        this.filteredAdmissionPatients = this.patients.filter(p => patientIds.includes(p.id));
      },
      error: () => {
        this.filteredAdmissionPatients = [];
      }
    });
  }

  submitAdmission(): void {
    this.isLoading = true;
    this.admissionsError = '';

    if (!this.admissionForm.patientId) {
      this.admissionsError = 'Seleziona un paziente.';
      this.isLoading = false;
      return;
    }
    if (!this.admissionForm.facilityCode?.trim()) {
      this.admissionsError = 'Seleziona una struttura.';
      this.isLoading = false;
      return;
    }
    if (!this.admissionForm.departmentCode?.trim()) {
      this.admissionsError = 'Seleziona un reparto.';
      this.isLoading = false;
      return;
    }
    if (!this.admissionForm.attendingDoctorId) {
      this.admissionsError = 'Seleziona un medico referente.';
      this.isLoading = false;
      return;
    }

    const payload: AdmissionCreatePayload = {
      patientId: this.admissionForm.patientId,
      departmentCode: this.admissionForm.departmentCode,
      admissionType: this.admissionForm.admissionType,
      notes: this.admissionForm.notes || null,
      attendingDoctorId: this.admissionForm.attendingDoctorId
    };

    this.admissionsService.createAdmission(payload).subscribe({
      next: (admission) => {
        this.admissions = [...this.admissions, admission];
        this.admissionForm = {
          patientId: null,
          facilityCode: '',
          departmentCode: '',
          admissionType: 'INPATIENT',
          notes: '',
          attendingDoctorId: null
        };
        this.filteredAdmissionPatients = [];
        this.closeAdminAdmissionModal();
        this.isLoading = false;
      },
      error: (err) => {
        if (err?.status === 409 || err?.error?.detail?.toLowerCase().includes('bed') || err?.error?.detail?.toLowerCase().includes('posto')) {
          this.admissionsError = 'Nessun posto letto disponibile nel reparto selezionato.';
        } else if (err?.status === 400 && err?.error?.extra?.length) {
          const fieldErrors = err.error.extra.map((e: { campo: string; messaggio: string }) => `${e.campo}: ${e.messaggio}`).join('; ');
          this.admissionsError = `Errore di validazione: ${fieldErrors}`;
        } else {
          this.admissionsError = err?.error?.detail || err?.error?.title || err?.error?.message || 'Impossibile registrare il ricovero.';
        }
        this.isLoading = false;
      }
    });
  }

  // ===== MODALE CAMBIA REFERENTE =====

  openChangeReferentModal(admission: AdmissionItem): void {
    this.changeReferentTarget = admission;
    this.changeReferentSelectedDoctorId = admission.attendingDoctorId ?? null;
    this.changeReferentError = '';
    this.changeReferentDoctors = [];

    this.admissionsService.loadDoctorsWithConsent(admission.patientId).subscribe({
      next: (doctorIds) => {
        this.changeReferentDoctors = this.doctors.filter(d => doctorIds.includes(d.id));
        this.showChangeReferentModal = true;
      },
      error: () => {
        this.changeReferentDoctors = [...this.doctors];
        this.showChangeReferentModal = true;
      }
    });
  }

  closeChangeReferentModal(): void {
    this.showChangeReferentModal = false;
    this.changeReferentTarget = null;
    this.changeReferentDoctors = [];
    this.changeReferentSelectedDoctorId = null;
    this.changeReferentError = '';
  }

  submitChangeReferent(): void {
    if (!this.changeReferentTarget || !this.changeReferentSelectedDoctorId) {
      this.changeReferentError = 'Seleziona un medico referente.';
      return;
    }
    this.isLoading = true;
    this.admissionsService.updateAdmission(this.changeReferentTarget.id, {
      attendingDoctorId: this.changeReferentSelectedDoctorId
    }).subscribe({
      next: (updated) => {
        const idx = this.admissions.findIndex(a => a.id === updated.id);
        if (idx !== -1) {
          this.admissions[idx] = { ...this.admissions[idx], ...updated };
          this.admissions = [...this.admissions];
        }
        this.isLoading = false;
        this.closeChangeReferentModal();
      },
      error: () => {
        this.changeReferentError = 'Impossibile aggiornare il referente.';
        this.isLoading = false;
      }
    });
  }

  // ===== MODALE DIMISSIONE =====

  openDischargeModal(admission: AdmissionItem): void {
    this.dischargeTarget = admission;
    this.dischargeError = '';
    this.showDischargeModal = true;
  }

  closeDischargeModal(): void {
    this.showDischargeModal = false;
    this.dischargeTarget = null;
    this.dischargeError = '';
  }

  submitDischarge(): void {
    if (!this.dischargeTarget) return;
    this.isLoading = true;
    this.admissionsService.dischargeAdmission(this.dischargeTarget.id).subscribe({
      next: (updated) => {
        const idx = this.admissions.findIndex(a => a.id === updated.id);
        if (idx !== -1) {
          this.admissions[idx] = { ...this.admissions[idx], ...updated };
          this.admissions = [...this.admissions];
        }
        this.isLoading = false;
        this.closeDischargeModal();
      },
      error: () => {
        this.dischargeError = 'Impossibile dimettere il paziente.';
        this.isLoading = false;
      }
    });
  }

  // ===== MODALE NOTE RICOVERO =====

  openAdmissionEditNoteModal(admission: AdmissionItem): void {
    this.admissionEditNoteTarget = admission;
    this.admissionEditNoteText = admission.notes ?? '';
    this.admissionEditNoteError = '';
    this.showAdmissionEditNoteModal = true;
  }

  closeAdmissionEditNoteModal(): void {
    this.showAdmissionEditNoteModal = false;
    this.admissionEditNoteTarget = null;
    this.admissionEditNoteText = '';
    this.admissionEditNoteError = '';
  }

  submitAdmissionNote(): void {
    if (!this.admissionEditNoteTarget) return;
    this.isLoading = true;
    this.admissionsService.updateAdmission(this.admissionEditNoteTarget.id, {
      notes: this.admissionEditNoteText
    }).subscribe({
      next: (updated) => {
        const idx = this.admissions.findIndex(a => a.id === updated.id);
        if (idx !== -1) {
          this.admissions[idx] = { ...this.admissions[idx], ...updated };
          this.admissions = [...this.admissions];
        }
        this.isLoading = false;
        this.closeAdmissionEditNoteModal();
      },
      error: () => {
        this.admissionEditNoteError = 'Impossibile aggiornare le note.';
        this.isLoading = false;
      }
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import {
  DirectoryService,
  Facility,
  FacilityCreate,
  Department,
  DepartmentCreate,
  Doctor,
  DoctorCreate,
  DoctorUpdate,
  Patient,
  PatientCreate,
  PatientUpdate
} from './directory.service';
import { finalize, forkJoin } from 'rxjs';

type TabType = 'facilities' | 'departments' | 'doctors' | 'patients';

@Component({
  selector: 'app-directory-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './directory-page.component.html'
})
export class DirectoryPageComponent implements OnInit {
  activeTab: TabType = 'facilities';
  searchQuery = '';
  isLoading = false;

  // Liste dati (caricate da API)
  facilities: Facility[] = [];
  departments: Department[] = [];
  doctors: Doctor[] = [];
  patients: Patient[] = [];

  // Dati completi per statistiche (non filtrati)
  allDoctors: Doctor[] = [];
  allPatients: Patient[] = [];

  // Info paginazione
  facilitiesPage = { totalElements: 0, totalPages: 0, number: 0, size: 20 };
  departmentsPage = { totalElements: 0, totalPages: 0, number: 0, size: 20 };
  doctorsPage = { totalElements: 0, totalPages: 0, number: 0, size: 20 };
  patientsPage = { totalElements: 0, totalPages: 0, number: 0, size: 20 };

  // Stato modale
  showDepartmentModal = false;
  showDoctorModal = false;
  showFacilityModal = false;
  showTransferModal = false;
  showConfirmModal = false;
  showPatientDetailModal = false;
  showPatientModal = false;

  // Modelli form
  departmentForm: Partial<DepartmentCreate & { id?: number }> = {};
  doctorForm: Partial<DoctorCreate & DoctorUpdate & { id?: number; status?: string }> = {};
  facilityForm: Partial<FacilityCreate & { id?: number }> = {};
  patientForm: Partial<PatientCreate & PatientUpdate & { id?: number; status?: string }> = {};
  transferForm = { doctorId: 0, newDepartmentCode: '', doctorName: '' };
  confirmAction: { title: string; message: string; onConfirm: () => void } = { title: '', message: '', onConfirm: () => {} };
  selectedPatient: Patient | null = null;

  // Modalità modifica
  editMode = false;
  editId: number | null = null;

  // Stati form touched per feedback validazione
  facilityFormTouched = false;
  departmentFormTouched = false;
  doctorFormTouched = false;

  // Notifiche toast
  toasts: { message: string; type: 'success' | 'error' | 'info' }[] = [];

  constructor(
    private router: Router,
    private directoryService: DirectoryService
  ) {}

  ngOnInit(): void {
    this.loadAllData();
  }

  loadAllData(): void {
    this.isLoading = true;
    forkJoin({
      facilities: this.directoryService.getFacilities({ size: 100 }),
      departments: this.directoryService.getDepartments({ size: 100 }),
      doctors: this.directoryService.getDoctors({ size: 100 }),
      patients: this.directoryService.getPatients({ size: 100 })
    }).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: (data) => {
        this.facilities = data.facilities.content;
        this.facilitiesPage = data.facilities;
        this.departments = data.departments.content;
        this.departmentsPage = data.departments;
        this.doctors = data.doctors.content;
        this.allDoctors = [...data.doctors.content];
        this.doctorsPage = data.doctors;
        this.patients = data.patients.content;
        this.allPatients = [...data.patients.content];
        this.patientsPage = data.patients;
      },
      error: (err) => {
        console.error('Error loading directory data:', err);
        this.showToast('Errore nel caricamento dei dati', 'error');
      }
    });
  }

  loadFacilities(): void {
    this.isLoading = true;
    this.directoryService.getFacilities({ q: this.searchQuery || undefined, size: 100 })
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (page) => {
          this.facilities = page.content;
          this.facilitiesPage = page;
        },
        error: () => this.showToast('Errore nel caricamento delle strutture', 'error')
      });
  }

  loadDepartments(): void {
    this.isLoading = true;
    this.directoryService.getDepartments({ q: this.searchQuery || undefined, size: 100 })
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (page) => {
          this.departments = page.content;
          this.departmentsPage = page;
        },
        error: () => this.showToast('Errore nel caricamento dei reparti', 'error')
      });
  }

  loadDoctors(): void {
    this.isLoading = true;
    this.directoryService.getDoctors({ q: this.searchQuery || undefined, size: 100 })
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (page) => {
          this.doctors = page.content;
          this.doctorsPage = page;
          // Aggiorna allDoctors solo quando si carica senza filtro di ricerca
          if (!this.searchQuery) {
            this.allDoctors = [...page.content];
          }
        },
        error: () => this.showToast('Errore nel caricamento dei medici', 'error')
      });
  }

  loadPatients(): void {
    this.isLoading = true;
    this.directoryService.getPatients({ q: this.searchQuery || undefined, size: 100 })
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (page) => {
          this.patients = page.content;
          this.patientsPage = page;
          // Aggiorna allPatients solo quando si carica senza filtro di ricerca
          if (!this.searchQuery) {
            this.allPatients = [...page.content];
          }
        },
        error: () => this.showToast('Errore nel caricamento dei pazienti', 'error')
      });
  }

  onSearch(): void {
    switch (this.activeTab) {
      case 'facilities':
        this.loadFacilities();
        break;
      case 'departments':
        this.loadDepartments();
        break;
      case 'doctors':
        this.loadDoctors();
        break;
      case 'patients':
        this.loadPatients();
        break;
    }
  }

  setTab(tab: TabType): void {
    this.activeTab = tab;
    this.searchQuery = '';
  }

  goBack(): void {
    this.router.navigate(['/portal/admin']);
  }

  // Dati filtrati in base alla ricerca (filtro lato client per dati già caricati)
  get filteredDepartments(): Department[] {
    if (!this.searchQuery) return this.departments;
    const q = this.searchQuery.toLowerCase();
    return this.departments.filter(d =>
      d.name.toLowerCase().includes(q) ||
      d.code.toLowerCase().includes(q) ||
      d.facilityName?.toLowerCase().includes(q)
    );
  }

  get filteredDoctors(): Doctor[] {
    if (!this.searchQuery) return this.doctors;
    const q = this.searchQuery.toLowerCase().trim();
    const fullName = (d: Doctor) => `${d.firstName} ${d.lastName}`.toLowerCase();
    return this.doctors.filter(d =>
      d.firstName.toLowerCase().includes(q) ||
      d.lastName.toLowerCase().includes(q) ||
      fullName(d).includes(q) ||
      d.email.toLowerCase().includes(q) ||
      d.departmentName?.toLowerCase().includes(q)
    );
  }

  get filteredFacilities(): Facility[] {
    if (!this.searchQuery) return this.facilities;
    const q = this.searchQuery.toLowerCase();
    return this.facilities.filter(f =>
      f.name.toLowerCase().includes(q) ||
      f.code.toLowerCase().includes(q)
    );
  }

  get filteredPatients(): Patient[] {
    if (!this.searchQuery) return this.patients;
    const q = this.searchQuery.toLowerCase().trim();
    const fullName = (p: Patient) => `${p.firstName} ${p.lastName}`.toLowerCase();
    return this.patients.filter(p =>
      p.firstName.toLowerCase().includes(q) ||
      p.lastName.toLowerCase().includes(q) ||
      fullName(p).includes(q) ||
      p.email.toLowerCase().includes(q) ||
      p.fiscalCode?.toLowerCase().includes(q)
    );
  }

  // ========== AZIONI STRUTTURE ==========

  openFacilityModal(facility?: Facility): void {
    this.editMode = !!facility;
    this.editId = facility?.id || null;
    this.facilityForm = facility ? { ...facility } : {};
    this.showFacilityModal = true;
  }

  saveFacility(): void {
    this.isLoading = true;
    const dto: FacilityCreate = {
      code: this.facilityForm.code || '',
      name: this.facilityForm.name || '',
      address: this.facilityForm.address,
      phone: this.facilityForm.phone
    };

    const operation = this.editMode && this.editId
      ? this.directoryService.updateFacility(this.editId, dto)
      : this.directoryService.createFacility(dto);

    operation.pipe(finalize(() => this.isLoading = false)).subscribe({
      next: () => {
        this.showToast(this.editMode ? 'Struttura aggiornata con successo' : 'Struttura creata con successo', 'success');
        this.closeFacilityModal();
        this.loadFacilities();
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Errore nel salvataggio della struttura', 'error');
      }
    });
  }

  closeFacilityModal(): void {
    this.showFacilityModal = false;
    this.facilityForm = {};
    this.editMode = false;
    this.editId = null;
    this.facilityFormTouched = false;
  }

  deleteFacility(facility: Facility): void {
    const deptCount = this.getDepartmentCountForFacility(facility.code);
    if (deptCount > 0) {
      this.showToast(`Impossibile eliminare: la struttura ha ${deptCount} reparti associati.`, 'error');
      return;
    }
    this.confirmAction = {
      title: 'Elimina Struttura',
      message: `Sei sicuro di voler eliminare la struttura "${facility.name}"? Questa azione non può essere annullata.`,
      onConfirm: () => {
        this.isLoading = true;
        this.directoryService.deleteFacility(facility.id)
          .pipe(finalize(() => this.isLoading = false))
          .subscribe({
            next: () => {
              this.showToast('Struttura eliminata', 'success');
              this.showConfirmModal = false;
              this.loadFacilities();
            },
            error: () => this.showToast('Errore nell\'eliminazione della struttura', 'error')
          });
      }
    };
    this.showConfirmModal = true;
  }

  // ========== AZIONI REPARTI ==========

  openDepartmentModal(department?: Department): void {
    this.editMode = !!department;
    this.editId = department?.id || null;
    this.departmentForm = department ? { ...department } : { facilityCode: this.facilities[0]?.code };
    this.showDepartmentModal = true;
  }

  saveDepartment(): void {
    this.isLoading = true;
    const dto: DepartmentCreate = {
      code: this.departmentForm.code || '',
      name: this.departmentForm.name || '',
      facilityCode: this.departmentForm.facilityCode || '',
      capacity: this.departmentForm.capacity
    };

    const operation = this.editMode && this.editId
      ? this.directoryService.updateDepartment(this.editId, dto)
      : this.directoryService.createDepartment(dto);

    operation.pipe(finalize(() => this.isLoading = false)).subscribe({
      next: () => {
        this.showToast(this.editMode ? 'Reparto aggiornato con successo' : 'Reparto creato con successo', 'success');
        this.closeDepartmentModal();
        this.loadDepartments();
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Errore nel salvataggio del reparto', 'error');
      }
    });
  }

  closeDepartmentModal(): void {
    this.showDepartmentModal = false;
    this.departmentForm = {};
    this.editMode = false;
    this.editId = null;
    this.departmentFormTouched = false;
  }

  deleteDepartment(dept: Department): void {
    const doctorCount = dept.doctorCount || this.getDoctorCountForDepartment(dept.code);
    if (doctorCount > 0) {
      const doctorWord = doctorCount === 1 ? 'medico associato' : 'medici associati';
      this.showToast(`Impossibile eliminare: il reparto ha ${doctorCount} ${doctorWord}.`, 'error');
      return;
    }
    this.confirmAction = {
      title: 'Elimina Reparto',
      message: `Sei sicuro di voler eliminare il reparto "${dept.name}"? Questa azione non può essere annullata.`,
      onConfirm: () => {
        this.isLoading = true;
        this.directoryService.deleteDepartment(dept.id)
          .pipe(finalize(() => this.isLoading = false))
          .subscribe({
            next: () => {
              this.showToast('Reparto eliminato', 'success');
              this.showConfirmModal = false;
              this.loadDepartments();
            },
            error: () => this.showToast('Errore nell\'eliminazione del reparto', 'error')
          });
      }
    };
    this.showConfirmModal = true;
  }

  // ========== AZIONI MEDICI ==========

  openDoctorModal(doctor?: Doctor): void {
    this.editMode = !!doctor;
    this.editId = doctor?.id || null;
    this.doctorForm = doctor ? { ...doctor } : {
      departmentCode: this.departments[0]?.code,
      status: 'PENDING'
    };
    this.showDoctorModal = true;
  }

  saveDoctor(): void {
    this.isLoading = true;

    if (this.editMode && this.editId) {
      const dto: DoctorUpdate = {
        firstName: this.doctorForm.firstName,
        lastName: this.doctorForm.lastName,
        email: this.doctorForm.email,
        phone: this.doctorForm.phone,
        specialization: this.doctorForm.specialization,
        departmentCode: this.doctorForm.departmentCode
      };

      this.directoryService.updateDoctor(this.editId, dto)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.showToast('Medico aggiornato con successo', 'success');
            this.closeDoctorModal();
            this.loadDoctors();
          },
          error: (err) => {
            this.showToast(err.error?.message || 'Errore nell\'aggiornamento del medico', 'error');
          }
        });
    } else {
      const dto: DoctorCreate = {
        firstName: this.doctorForm.firstName || '',
        lastName: this.doctorForm.lastName || '',
        email: this.doctorForm.email || '',
        phone: this.doctorForm.phone,
        specialization: this.doctorForm.specialization,
        departmentCode: this.doctorForm.departmentCode || ''
      };

      this.directoryService.createDoctor(dto)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.showToast('Medico registrato. Credenziali Keycloak generate e inviate via email.', 'success');
            this.closeDoctorModal();
            this.loadDoctors();
            this.loadDepartments();
          },
          error: (err) => {
            this.showToast(err.error?.message || 'Errore nella creazione del medico', 'error');
          }
        });
    }
  }

  closeDoctorModal(): void {
    this.showDoctorModal = false;
    this.doctorForm = {};
    this.editMode = false;
    this.editId = null;
  }

  openTransferModal(doctor: Doctor): void {
    this.transferForm = {
      doctorId: doctor.id,
      newDepartmentCode: doctor.departmentCode,
      doctorName: `${doctor.firstName} ${doctor.lastName}`
    };
    this.showTransferModal = true;
  }

  transferDoctor(): void {
    this.isLoading = true;
    this.directoryService.transferDoctor(this.transferForm.doctorId, this.transferForm.newDepartmentCode)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (updatedDoctor) => {
          this.showToast(`${updatedDoctor.firstName} ${updatedDoctor.lastName} trasferito a ${updatedDoctor.departmentName}.`, 'success');
          this.closeTransferModal();
          this.loadDoctors();
          this.loadDepartments();
        },
        error: (err) => {
          this.showToast(err.error?.message || 'Errore nel trasferimento del medico', 'error');
        }
      });
  }

  closeTransferModal(): void {
    this.showTransferModal = false;
    this.transferForm = { doctorId: 0, newDepartmentCode: '', doctorName: '' };
  }

  activateDoctor(doctor: Doctor): void {
    this.isLoading = true;
    this.directoryService.activateDoctor(doctor.id)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (updated) => {
          this.showToast(`${updated.firstName} ${updated.lastName} è ora attivo sulla piattaforma.`, 'success');
          this.loadDoctors();
        },
        error: () => this.showToast('Errore nell\'attivazione del medico', 'error')
      });
  }

  resendActivationEmail(doctor: Doctor): void {
    this.directoryService.resendDoctorActivation(doctor.id).subscribe({
      next: () => this.showToast(`Email di attivazione reinviata a ${doctor.email}`, 'info'),
      error: () => this.showToast('Errore nel reinvio dell\'email di attivazione', 'error')
    });
  }

  disableDoctor(doctor: Doctor): void {
    this.confirmAction = {
      title: 'Disabilita Accesso',
      message: `Sei sicuro di voler disabilitare l'accesso per ${doctor.firstName} ${doctor.lastName}? Il medico non potrà più accedere alla piattaforma.`,
      onConfirm: () => {
        this.isLoading = true;
        this.directoryService.disableDoctor(doctor.id)
          .pipe(finalize(() => this.isLoading = false))
          .subscribe({
            next: () => {
              this.showToast('Accesso disabilitato', 'info');
              this.showConfirmModal = false;
              this.loadDoctors();
            },
            error: () => this.showToast('Errore nella disabilitazione dell\'accesso', 'error')
          });
      }
    };
    this.showConfirmModal = true;
  }

  // ========== AZIONI PAZIENTI ==========

  openPatientDetail(patient: Patient): void {
    this.selectedPatient = patient;
    this.showPatientDetailModal = true;
  }

  closePatientDetailModal(): void {
    this.showPatientDetailModal = false;
    this.selectedPatient = null;
  }

  openPatientModal(patient?: Patient): void {
    this.editMode = !!patient;
    this.editId = patient?.id || null;
    this.patientForm = patient ? { ...patient } : {};
    this.showPatientModal = true;
  }

  savePatient(): void {
    this.isLoading = true;

    if (this.editMode && this.editId) {
      const dto: PatientUpdate = {
        firstName: this.patientForm.firstName,
        lastName: this.patientForm.lastName,
        email: this.patientForm.email,
        phone: this.patientForm.phone,
        fiscalCode: this.patientForm.fiscalCode,
        birthDate: this.patientForm.birthDate,
        address: this.patientForm.address
      };

      this.directoryService.updatePatient(this.editId, dto)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.showToast('Paziente aggiornato con successo', 'success');
            this.closePatientModal();
            this.loadPatients();
          },
          error: (err) => {
            this.showToast(err.error?.message || 'Errore nell\'aggiornamento del paziente', 'error');
          }
        });
    } else {
      const dto: PatientCreate = {
        firstName: this.patientForm.firstName || '',
        lastName: this.patientForm.lastName || '',
        email: this.patientForm.email || '',
        phone: this.patientForm.phone,
        fiscalCode: this.patientForm.fiscalCode,
        birthDate: this.patientForm.birthDate,
        address: this.patientForm.address
      };

      this.directoryService.createPatient(dto)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.showToast('Paziente registrato con successo', 'success');
            this.closePatientModal();
            this.loadPatients();
          },
          error: (err) => {
            this.showToast(err.error?.message || 'Errore nella creazione del paziente', 'error');
          }
        });
    }
  }

  closePatientModal(): void {
    this.showPatientModal = false;
    this.patientForm = {};
    this.editMode = false;
    this.editId = null;
  }

  activatePatient(patient: Patient): void {
    this.isLoading = true;
    this.directoryService.activatePatient(patient.id)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (updated) => {
          this.showToast(`${updated.firstName} ${updated.lastName} è ora attivo sulla piattaforma.`, 'success');
          this.loadPatients();
        },
        error: () => this.showToast('Errore nell\'attivazione del paziente', 'error')
      });
  }

  resendPatientActivationEmail(patient: Patient): void {
    this.directoryService.resendPatientActivation(patient.id).subscribe({
      next: () => this.showToast(`Email di attivazione reinviata a ${patient.email}`, 'info'),
      error: () => this.showToast('Errore nel reinvio dell\'email di attivazione', 'error')
    });
  }

  disablePatient(patient: Patient): void {
    this.confirmAction = {
      title: 'Disabilita Accesso Paziente',
      message: `Sei sicuro di voler disabilitare l'accesso per ${patient.firstName} ${patient.lastName}? Il paziente non potrà più accedere alla piattaforma.`,
      onConfirm: () => {
        this.isLoading = true;
        this.directoryService.disablePatient(patient.id)
          .pipe(finalize(() => this.isLoading = false))
          .subscribe({
            next: () => {
              this.showToast('Accesso paziente disabilitato', 'info');
              this.showConfirmModal = false;
              this.loadPatients();
            },
            error: () => this.showToast('Errore nella disabilitazione dell\'accesso', 'error')
          });
      }
    };
    this.showConfirmModal = true;
  }

  getPatientStatusDescription(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'Il paziente ha completato la registrazione e può accedere alla piattaforma.';
      case 'PENDING': return 'Il paziente si è registrato ma non ha ancora confermato l\'email o completato il primo accesso.';
      case 'DISABLED': return 'L\'accesso alla piattaforma è stato disabilitato.';
      default: return '';
    }
  }

  // ========== METODI UTILITÀ ==========

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'bg-success';
      case 'PENDING': return 'bg-warning text-dark';
      case 'DISABLED': return 'bg-secondary';
      default: return 'bg-secondary';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'Attivo';
      case 'PENDING': return 'In attesa attivazione';
      case 'DISABLED': return 'Disabilitato';
      default: return status;
    }
  }

  getStatusDescription(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'Il medico ha completato l\'attivazione e può accedere alla piattaforma.';
      case 'PENDING': return 'Credenziali generate. In attesa che il medico completi il primo accesso e imposti la password.';
      case 'DISABLED': return 'L\'accesso alla piattaforma è stato disabilitato.';
      default: return '';
    }
  }

  getInitials(firstName: string, lastName: string): string {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  getDepartmentCountForFacility(facilityCode: string): number {
    return this.departments.filter(d => d.facilityCode === facilityCode).length;
  }

  getDoctorCountForDepartment(departmentCode: string): number {
    return this.doctors.filter(d => d.departmentCode === departmentCode).length;
  }

  showToast(message: string, type: 'success' | 'error' | 'info'): void {
    this.toasts.push({ message, type });
    setTimeout(() => {
      this.toasts.shift();
    }, 4000);
  }

  closeConfirmModal(): void {
    this.showConfirmModal = false;
  }

  // Statistiche - usa sempre i dati completi non filtrati
  get totalDoctors(): number {
    return this.allDoctors.length || this.doctorsPage.totalElements || this.doctors.length;
  }

  get activeDoctors(): number {
    return this.allDoctors.filter(d => d.status === 'ACTIVE').length;
  }

  get pendingDoctors(): number {
    return this.allDoctors.filter(d => d.status === 'PENDING').length;
  }

  get totalDepartments(): number {
    return this.departmentsPage.totalElements || this.departments.length;
  }

  get totalFacilities(): number {
    return this.facilitiesPage.totalElements || this.facilities.length;
  }

  get totalPatients(): number {
    return this.allPatients.length || this.patientsPage.totalElements || this.patients.length;
  }

  get activePatients(): number {
    return this.allPatients.filter(p => p.status === 'ACTIVE').length;
  }

  get pendingPatients(): number {
    return this.allPatients.filter(p => p.status === 'PENDING').length;
  }
}

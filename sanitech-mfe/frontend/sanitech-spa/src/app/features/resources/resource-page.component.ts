import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/auth/auth.service';

interface ResourceEndpoint {
  label: string;
  method: string;
  path: string;
  payload?: string;
}

interface SchedulingSlot {
  id: number;
  doctorId: number;
  date: string;
  time: string;
  status: string;
  modality: 'IN_PERSON' | 'REMOTE';
  notes?: string;
}

interface SchedulingAppointment {
  id: number;
  patientId: number;
  doctorId: number;
  slotId: number;
  reason: string;
  status: string;
}

interface DocumentItem {
  id: number;
  patientId: number;
  type: string;
  name: string;
  notes?: string;
  uploadedAt: string;
  uploadedBy?: 'PATIENT' | 'DOCTOR';
  viewedByCurrentUser?: boolean;
}

interface ConsentItem {
  id: number;
  patientId: number;
  consentType: string;
  accepted: boolean;
  signedAt: string;
}

interface PaymentItem {
  id: number;
  patientId: number;
  amount: number;
  currency: string;
  service: string;
  status: string;
  paidAt: string;
  receiptName?: string;
}

interface AdmissionItem {
  id: number;
  patientId: number;
  department: string;
  bedId: number;
  status: string;
  admittedAt: string;
  notes?: string;
}

interface NotificationItem {
  id: number;
  recipient: string;
  channel: string;
  subject: string;
  message: string;
  notes: string;
  status: string;
  sentAt: string;
}

interface PrescriptionItem {
  id: number;
  patientId: number;
  doctorId: number;
  drug: string;
  dosage: string;
  durationDays: number;
  status: string;
  notes?: string;
}

interface TelevisitItem {
  id: number;
  appointmentId: number;
  provider: string;
  status: string;
  token: string;
}

interface DoctorItem {
  id: number;
  firstName: string;
  lastName: string;
  speciality: string;
}

interface PatientItem {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

interface DepartmentItem {
  code: string;
  name: string;
}

interface SpecialityItem {
  code: string;
  name: string;
}

interface AuditItem {
  id: number;
  action: string;
  actor: string;
  timestamp: string;
}

@Component({
  selector: 'app-resource-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './resource-page.component.html'
})
export class ResourcePageComponent {
  title = '';
  description = '';
  endpoints: ResourceEndpoint[] = [];
  selectedEndpoint?: ResourceEndpoint;
  payload = '';
  responseBody = '';
  isLoading = false;
  pageSize = 5;
  pageSizeOptions = [5, 10, 20, 50];
  private pageState: Record<string, number> = {};
  mode:
    | 'api'
    | 'scheduling'
    | 'docs'
    | 'payments'
    | 'admissions'
    | 'notifications'
    | 'prescribing'
    | 'televisit'
    | 'admin-directory'
    | 'admin-audit'
    | 'admin-televisit' = 'api';
  slots: SchedulingSlot[] = [];
  appointments: SchedulingAppointment[] = [];
  schedulingError = '';
  bookingForm = {
    slotId: null as number | null,
    department: '',
    reason: ''
  };
  showAppointmentRescheduleModal = false;
  rescheduleAppointment: SchedulingAppointment | null = null;
  rescheduleAppointmentDate = '';
  rescheduleAppointmentReason = '';
  showAppointmentRejectModal = false;
  rejectAppointmentTarget: SchedulingAppointment | null = null;
  rejectAppointmentReason = '';
  showBookingModal = false;
  showDocumentModal = false;
  showConsentModal = false;
  slotForm = {
    date: '',
    time: '',
    modality: 'IN_PERSON' as 'IN_PERSON' | 'REMOTE',
    notes: ''
  };
  documents: DocumentItem[] = [];
  consents: ConsentItem[] = [];
  docsError = '';
  docForm = {
    patientId: null as number | null,
    type: 'REFERT',
    name: '',
    fileName: '',
    notes: ''
  };
  consentForm = {
    consentType: 'GDPR',
    accepted: true
  };
  editingConsentId: number | null = null;
  payments: PaymentItem[] = [];
  admissions: AdmissionItem[] = [];
  showAdmissionRescheduleModal = false;
  rescheduleAdmission: AdmissionItem | null = null;
  rescheduleForm = {
    date: '',
    reason: ''
  };
  rescheduleError = '';
  rescheduleSuccess = '';
  showAdmissionRejectModal = false;
  rejectAdmissionTarget: AdmissionItem | null = null;
  rejectAdmissionReason = '';
  paymentsError = '';
  paymentsSuccess = '';
  paymentForm = {
    paymentId: null as number | null,
    receiptName: '',
    service: '',
    amount: 0
  };
  admissionForm = {
    patientId: 1,
    department: 'CARD',
    bedId: 4
  };
  notifications: NotificationItem[] = [];
  notificationsError = '';
  notificationsSuccess = '';
  notificationForm = {
    recipient: '',
    channel: 'EMAIL',
    subject: '',
    message: '',
    notes: ''
  };
  showNotificationContactsModal = false;
  showNotificationPreferencesModal = false;
  notificationPrefs = {
    email: 'anna.conti@sanitech.example',
    phone: '+39 347 123 4567',
    channels: {
      email: true,
      sms: false
    },
    types: {
      appointments: true,
      documents: true,
      payments: false,
      admissions: false,
      prescriptions: false
    }
  };
  prescriptions: PrescriptionItem[] = [];
  prescribingError = '';
  showPrescriptionQuestionModal = false;
  showPrescriptionRejectModal = false;
  selectedPrescription: PrescriptionItem | null = null;
  prescriptionQuestion = '';
  rejectPrescriptionTarget: PrescriptionItem | null = null;
  rejectPrescriptionReason = '';
  showDeleteConfirmModal = false;
  deleteConfirmMessage = '';
  deleteConfirmTarget:
    | { type: 'appointment'; value: SchedulingAppointment }
    | { type: 'document'; value: DocumentItem }
    | { type: 'consent'; value: ConsentItem }
    | null = null;
  prescriptionForm = {
    patientId: 1,
    drug: '',
    dosage: '',
    durationDays: 10
  };
  televisits: TelevisitItem[] = [];
  televisitError = '';
  televisitForm = {
    appointmentId: 1,
    patientId: null as number | null,
    provider: 'LIVEKIT'
  };
  showSlotModal = false;
  doctors: DoctorItem[] = [];
  patients: PatientItem[] = [];
  departments: DepartmentItem[] = [];
  specialities: SpecialityItem[] = [];
  directoryError = '';
  doctorForm = {
    firstName: '',
    lastName: '',
    speciality: 'CARD'
  };
  patientForm = {
    firstName: '',
    lastName: '',
    email: ''
  };
  auditEvents: AuditItem[] = [];
  auditError = '';
  showPaymentQuestionModal = false;
  selectedPayment: PaymentItem | null = null;
  paymentQuestion = '';

  constructor(
    private route: ActivatedRoute,
    private api: ApiService,
    private auth: AuthService,
    private router: Router
  ) {
    const data = this.route.snapshot.data;
    this.title = data['title'] as string;
    this.description = data['description'] as string;
    this.mode =
      (data['view'] as
        | 'api'
        | 'scheduling'
        | 'docs'
        | 'payments'
        | 'admissions'
        | 'notifications'
        | 'prescribing'
        | 'televisit'
        | 'admin-directory'
        | 'admin-audit'
        | 'admin-televisit') ??
      'api';
    this.endpoints = (data['endpoints'] as ResourceEndpoint[]) ?? [];
    this.selectedEndpoint = this.endpoints[0];
    this.payload = this.selectedEndpoint?.payload ?? '';
    if (this.mode === 'scheduling') {
      this.loadScheduling();
    }
    if (this.mode === 'docs') {
      this.loadDocs();
      this.loadPatients();
    }
    if (this.mode === 'payments') {
      this.loadPayments();
      this.loadPatients();
    }
    if (this.mode === 'admissions') {
      this.loadAdmissions();
      this.loadPatients();
    }
    if (this.mode === 'notifications') {
      this.loadNotifications();
    }
    if (this.mode === 'prescribing') {
      this.loadPrescriptions();
      this.loadPatients();
      this.loadDoctors();
    }
    if (this.mode === 'televisit') {
      this.loadTelevisits();
      this.loadPatients();
    }
    if (this.mode === 'admin-directory') {
      this.loadDirectory();
    }
    if (this.mode === 'admin-audit') {
      this.loadAudit();
    }
    if (this.mode === 'admin-televisit') {
      this.loadAdminTelevisit();
    }
  }

  selectEndpoint(endpoint: ResourceEndpoint): void {
    this.selectedEndpoint = endpoint;
    this.payload = endpoint.payload ?? '';
    this.responseBody = '';
  }

  getPage(key: string): number {
    return this.pageState[key] ?? 1;
  }

  getTotalPages(total: number): number {
    return Math.max(1, Math.ceil(total / this.pageSize));
  }

  setPage(key: string, page: number, total: number): void {
    const totalPages = this.getTotalPages(total);
    const nextPage = Math.min(Math.max(1, page), totalPages);
    this.pageState[key] = nextPage;
  }

  getPageSliceStart(key: string): number {
    return (this.getPage(key) - 1) * this.pageSize;
  }

  getPageSliceEnd(key: string): number {
    return this.getPage(key) * this.pageSize;
  }

  resetPagination(): void {
    this.pageState = {};
  }

  execute(): void {
    if (!this.selectedEndpoint) {
      return;
    }
    let body: unknown;
    if (this.payload.trim()) {
      try {
        body = JSON.parse(this.payload);
      } catch {
        this.responseBody = 'Payload JSON non valido.';
        return;
      }
    }
    this.isLoading = true;
    this.responseBody = '';
    this.api.request(this.selectedEndpoint.method, this.selectedEndpoint.path, body).subscribe({
      next: (response) => {
        this.responseBody = JSON.stringify(response, null, 2);
        this.isLoading = false;
      },
      error: (error) => {
        this.responseBody = JSON.stringify(error, null, 2);
        this.isLoading = false;
      }
    });
  }

  loadScheduling(): void {
    this.isLoading = true;
    this.schedulingError = '';
    this.api.request<SchedulingSlot[]>('GET', '/api/slots').subscribe({
      next: (slots) => {
        this.slots = slots;
        this.isLoading = false;
      },
      error: () => {
        this.schedulingError = 'Impossibile caricare gli slot disponibili.';
        this.isLoading = false;
      }
    });
    this.api.request<SchedulingAppointment[]>('GET', '/api/appointments').subscribe({
      next: (appointments) => {
        this.appointments = appointments;
      },
      error: () => {
        this.schedulingError = 'Impossibile caricare gli appuntamenti.';
      }
    });
    this.api.request<DoctorItem[]>('GET', '/api/doctors').subscribe({
      next: (doctors) => {
        this.doctors = doctors;
      },
      error: () => {
        this.schedulingError = 'Impossibile caricare i medici.';
      }
    });
    this.api.request<SpecialityItem[]>('GET', '/api/specialities').subscribe({
      next: (specialities) => {
        this.specialities = specialities;
      },
      error: () => {
        this.schedulingError = 'Impossibile caricare le specializzazioni.';
      }
    });
    this.api.request<DepartmentItem[]>('GET', '/api/departments').subscribe({
      next: (departments) => {
        this.departments = departments;
      },
      error: () => {
        this.schedulingError = 'Impossibile caricare i reparti.';
      }
    });
  }

  loadDoctors(): void {
    this.api.request<DoctorItem[]>('GET', '/api/doctors').subscribe({
      next: (doctors) => {
        this.doctors = doctors;
      },
      error: () => {
        this.directoryError = 'Impossibile caricare i medici.';
      }
    });
  }

  submitBooking(): void {
    if (this.isPatient && !this.bookingForm.department) {
      this.schedulingError = 'Seleziona un reparto.';
      return;
    }
    if (!this.bookingForm.slotId) {
      this.schedulingError = 'Seleziona uno slot disponibile.';
      return;
    }
    if (!this.bookingForm.reason.trim()) {
      this.schedulingError = 'Inserisci il motivo della prenotazione.';
      return;
    }
    const slot = this.slots.find((item) => item.id === this.bookingForm.slotId);
    if (!slot) {
      this.schedulingError = 'Slot selezionato non valido.';
      return;
    }
    this.isLoading = true;
    this.schedulingError = '';
    this.api.request<SchedulingAppointment>('POST', '/api/appointments', {
      patientId: 1,
      doctorId: slot.doctorId,
      slotId: slot.id,
      reason: this.bookingForm.reason,
      status: 'PENDING'
    }).subscribe({
      next: (appointment) => {
        this.appointments = [...this.appointments, appointment];
        this.slots = this.slots.map((item) =>
          item.id === slot.id ? { ...item, status: 'BOOKED' } : item
        );
        this.bookingForm.reason = '';
        this.bookingForm.slotId = null;
        this.closeBookingModal();
        this.isLoading = false;
      },
      error: () => {
        this.schedulingError = 'Impossibile registrare la prenotazione.';
        this.isLoading = false;
      }
    });
  }

  getDoctorLabel(doctorId: number): string {
    const doctor = this.getDoctorById(doctorId);
    return doctor ? `${doctor.firstName} ${doctor.lastName}` : `Medico ${doctorId}`;
  }

  getSlotById(slotId: number): SchedulingSlot | undefined {
    return this.slots.find((slot) => slot.id === slotId);
  }

  isAppointmentReschedulable(appointment: SchedulingAppointment): boolean {
    const slot = this.getSlotById(appointment.slotId);
    if (!slot?.date || !slot.time) {
      return false;
    }
    const dateTimeValue = slot.date.includes('T') ? slot.date : `${slot.date}T${slot.time}`;
    const parsed = new Date(dateTimeValue);
    if (Number.isNaN(parsed.getTime())) {
      return false;
    }
    return parsed.getTime() >= Date.now();
  }

  isAdmissionReschedulable(admission: AdmissionItem): boolean {
    if (!admission.admittedAt) {
      return false;
    }
    const parsed = new Date(admission.admittedAt);
    if (Number.isNaN(parsed.getTime())) {
      return false;
    }
    return parsed.getTime() >= Date.now();
  }

  formatDate(value: string): string {
    if (!value) {
      return '-';
    }
    const normalizedValue = value.trim();
    if (/^\d{2}\/\d{2}\/\d{4}$/.test(normalizedValue)) {
      return normalizedValue;
    }
    const isoMatch = normalizedValue.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (isoMatch) {
      return `${isoMatch[3]}/${isoMatch[2]}/${isoMatch[1]}`;
    }
    const parsed = new Date(normalizedValue);
    if (Number.isNaN(parsed.getTime())) {
      return normalizedValue;
    }
    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    return `${day}/${month}/${parsed.getFullYear()}`;
  }

  formatDateTime(value: string): string {
    if (!value) {
      return '-';
    }
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    const year = parsed.getFullYear();
    const hours = String(parsed.getHours()).padStart(2, '0');
    const minutes = String(parsed.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }

  getDocumentTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      REFERT: 'Referto',
      REPORT: 'Referto',
      CERTIFICATE: 'Certificato',
      GENERIC: 'Documento generico'
    };
    return labels[type] ?? type;
  }

  isDocumentViewed(doc: DocumentItem): boolean {
    if (doc.viewedByCurrentUser) {
      return true;
    }
    if (this.isPatient && doc.patientId === 1) {
      return true;
    }
    return doc.id % 2 === 0;
  }

  getDocumentStatusLabel(doc: DocumentItem): string {
    return this.isDocumentViewed(doc) ? 'Visualizzato' : 'Da visualizzare';
  }

  getDocumentUploaderLabel(doc: DocumentItem): string {
    const uploader = doc.uploadedBy ?? 'DOCTOR';
    return uploader === 'PATIENT' ? 'Utente' : 'Medico';
  }

  isDocumentDeletable(doc: DocumentItem): boolean {
    const uploader = doc.uploadedBy ?? 'DOCTOR';
    if (this.isDoctor) {
      return uploader === 'DOCTOR';
    }
    return uploader === 'PATIENT';
  }

  getSpecialityLabel(code: string): string {
    const speciality = this.specialities.find((item) => item.code === code);
    return speciality ? speciality.name : code;
  }

  get portalHomeLabel(): string {
    if (this.auth.hasRole('ROLE_ADMIN')) {
      return 'Area riservata admin';
    }
    if (this.auth.hasRole('ROLE_DOCTOR')) {
      return 'Area riservata medico';
    }
    return 'Area riservata paziente';
  }

  get portalHomeRoute(): string {
    if (this.auth.hasRole('ROLE_ADMIN')) {
      return '/portal/admin';
    }
    if (this.auth.hasRole('ROLE_DOCTOR')) {
      return '/portal/doctor';
    }
    return '/portal/patient';
  }

  goToPortalHome(): void {
    this.router.navigateByUrl(this.portalHomeRoute);
  }

  get isPatient(): boolean {
    return this.auth.hasRole('ROLE_PATIENT');
  }

  get isDoctor(): boolean {
    return this.auth.hasRole('ROLE_DOCTOR');
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      AVAILABLE: 'Disponibile',
      BOOKED: 'Occupato',
      CONFIRMED: 'Confermato',
      PENDING: 'In attesa',
      ACTIVE: 'Attivo',
      REJECTED: 'Rifiutato'
    };
    return labels[status] ?? status;
  }

  editConsent(consent: ConsentItem): void {
    this.consentForm.consentType = consent.consentType;
    this.consentForm.accepted = consent.accepted;
    this.openConsentModal(consent.id);
  }

  getAdmissionStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Da confermare',
      CONFIRMED: 'Confermato',
      RESCHEDULED: 'Ripianificato',
      REJECTED: 'Rifiutato',
      DISCHARGED: 'Dimesso'
    };
    return labels[status] ?? status;
  }

  getPaymentStatusLabel(payment: PaymentItem): string {
    if (payment.status === 'CONFIRMED') {
      return 'Pagamento ricevuto';
    }
    if (payment.status === 'RECEIPT_UPLOADED' || (payment.status === 'PAID' && payment.receiptName)) {
      return 'In fase di approvazione';
    }
    if (payment.status === 'PAID') {
      return 'Ricevuta da caricare';
    }
    if (payment.status === 'PENDING' || payment.status === 'IN_ATTESA') {
      return 'Pagamento da effettuare';
    }
    if (payment.status === 'FAILED') {
      return 'Non riuscito';
    }
    return payment.status;
  }

  canMarkPaymentAsPaid(payment: PaymentItem): boolean {
    return payment.status === 'PENDING' || payment.status === 'IN_ATTESA';
  }

  canAttachPaymentReceipt(payment: PaymentItem): boolean {
    return payment.status === 'PAID' && !payment.receiptName;
  }

  get latestConfirmedAdmission(): AdmissionItem | null {
    const admissions = this.visibleAdmissions.filter((admission) => admission.status === 'CONFIRMED');
    if (!admissions.length) {
      return null;
    }
    return admissions.reduce((latest, admission) =>
      new Date(admission.admittedAt).getTime() > new Date(latest.admittedAt).getTime() ? admission : latest
    );
  }

  getAdmissionPaymentLabel(admission: AdmissionItem): string {
    const startDate = this.formatDate(admission.admittedAt);
    const endDate = this.formatDate(this.addDays(admission.admittedAt, 3));
    return `Ricovero da ${startDate} a ${endDate}`;
  }

  addDays(dateValue: string, days: number): string {
    const parsed = new Date(dateValue);
    if (Number.isNaN(parsed.getTime())) {
      return dateValue;
    }
    parsed.setDate(parsed.getDate() + days);
    return parsed.toISOString();
  }

  getConsentTypeLabel(consentType: string): string {
    const labels: Record<string, string> = {
      GDPR: 'Consenso GDPR',
      PRIVACY: 'Consenso privacy',
      THERAPY: 'Consenso terapia'
    };
    return labels[consentType] ?? consentType;
  }

  getCurrencyLabel(currency: string): string {
    const labels: Record<string, string> = {
      EUR: 'Euro',
      USD: 'Dollaro USA'
    };
    return labels[currency] ?? currency;
  }

  getDepartmentLabel(code: string): string {
    const labels: Record<string, string> = {
      CARD: 'Cardiologia',
      NEURO: 'Neurologia',
      DERM: 'Dermatologia',
      ORTHO: 'Ortopedia',
      PNEUMO: 'Pneumologia'
    };
    return labels[code] ?? code;
  }

  get confirmedAppointments(): SchedulingAppointment[] {
    return this.appointments.filter((appointment) => appointment.status === 'CONFIRMED');
  }

  get doctorReceivedAppointments(): SchedulingAppointment[] {
    if (!this.isDoctor) {
      return [];
    }
    return this.appointments.filter((appointment) => appointment.doctorId === this.currentDoctorId);
  }

  getAppointmentStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      PENDING: 'In attesa di conferma',
      IN_ATTESA: 'In attesa di conferma',
      CONFIRMED: 'Confermato',
      REJECTED: 'Rifiutato'
    };
    return labels[status] ?? status;
  }

  get pendingPayments(): PaymentItem[] {
    const pending = this.payments.filter(
      (payment) => payment.status === 'PENDING' || payment.status === 'IN_ATTESA'
    );
    if (this.isPatient) {
      return pending.filter((payment) => payment.patientId === 1);
    }
    return pending;
  }

  get availableSlots(): SchedulingSlot[] {
    if (this.isPatient) {
      if (!this.bookingForm.department) {
        return [];
      }
      return this.slots.filter(
        (slot) =>
          slot.status === 'AVAILABLE' &&
          this.getDoctorById(slot.doctorId)?.speciality === this.bookingForm.department
      );
    }
    return this.slots.filter((slot) => slot.status === 'AVAILABLE');
  }

  get visibleSlots(): SchedulingSlot[] {
    if (this.isDoctor) {
      return this.slots.filter((slot) => slot.doctorId === this.currentDoctorId && slot.status === 'AVAILABLE');
    }
    return this.slots.filter((slot) => slot.status === 'AVAILABLE');
  }

  get visibleConsents(): ConsentItem[] {
    if (this.isDoctor) {
      const patientIds = new Set(this.patients.map((patient) => patient.id));
      return this.consents.filter((consent) => patientIds.has(consent.patientId));
    }
    if (this.isPatient) {
      return this.consents.filter((consent) => consent.patientId === 1);
    }
    return this.consents;
  }

  get visiblePrescriptions(): PrescriptionItem[] {
    if (this.isPatient) {
      return this.prescriptions.filter((prescription) => prescription.patientId === 1);
    }
    return this.prescriptions;
  }

  get visibleAdmissions(): AdmissionItem[] {
    if (this.isDoctor) {
      const department = this.currentDoctorDepartment;
      if (!department) {
        return [];
      }
      return this.admissions.filter((admission) => admission.department === department);
    }
    if (this.isPatient) {
      return this.admissions.filter((admission) => admission.patientId === 1);
    }
    return this.admissions;
  }

  get doctorSlotsByDate(): Array<{ date: string; slots: SchedulingSlot[] }> {
    if (!this.isDoctor) {
      return [];
    }
    const grouped = this.visibleSlots.reduce<Record<string, SchedulingSlot[]>>((acc, slot) => {
      acc[slot.date] = acc[slot.date] ?? [];
      acc[slot.date].push(slot);
      return acc;
    }, {});
    return Object.entries(grouped)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, slots]) => ({
        date,
        slots: slots.sort((first, second) => first.time.localeCompare(second.time))
      }));
  }

  getNotificationStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      SENT: 'Inviata',
      DELIVERED: 'Consegnata',
      FAILED: 'Non consegnata'
    };
    return labels[status] ?? status;
  }

  getPrescriptionStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE: 'In attesa di conferma',
      PENDING: 'In attesa di conferma',
      CONFIRMED: 'Confermato',
      REJECTED: 'Rifiutato',
      SUSPENDED: 'Sospesa',
      COMPLETED: 'Conclusa'
    };
    return labels[status] ?? status;
  }

  getTelevisitStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      READY: 'Pronta',
      ACTIVE: 'In corso',
      COMPLETED: 'Conclusa'
    };
    return labels[status] ?? status;
  }

  getPatientLabel(patientId: number): string {
    const patient = this.patients.find((item) => item.id === patientId);
    return patient ? `${patient.firstName} ${patient.lastName}` : `Paziente ${patientId}`;
  }

  get currentDoctorId(): number {
    return this.doctors[0]?.id ?? 1;
  }

  get currentDoctorDepartment(): string {
    return this.doctors[0]?.speciality ?? '';
  }

  getDoctorById(doctorId: number): DoctorItem | undefined {
    return this.doctors.find((item) => item.id === doctorId);
  }

  getSlotLabel(slot: SchedulingSlot): string {
    return `${this.formatDate(slot.date)} • ${slot.time} • ${this.getDoctorLabel(slot.doctorId)} • ${this.getModalityLabel(
      slot.modality
    )}`;
  }

  getModalityLabel(modality: SchedulingSlot['modality']): string {
    return modality === 'REMOTE' ? 'Da remoto' : 'In presenza';
  }

  submitSlot(): void {
    if (!this.slotForm.date || !this.slotForm.time) {
      this.schedulingError = 'Inserisci data e ora dello slot.';
      return;
    }
    this.isLoading = true;
    this.schedulingError = '';
    this.api.request<SchedulingSlot>('POST', '/api/slots', {
      doctorId: this.currentDoctorId,
      date: this.slotForm.date,
      time: this.slotForm.time,
      modality: this.slotForm.modality,
      notes: this.slotForm.notes,
      status: 'AVAILABLE'
    }).subscribe({
      next: (slot) => {
        this.slots = [...this.slots, slot];
        this.slotForm.date = '';
        this.slotForm.time = '';
        this.slotForm.modality = 'IN_PERSON';
        this.slotForm.notes = '';
        this.showSlotModal = false;
        this.isLoading = false;
      },
      error: () => {
        this.schedulingError = 'Impossibile creare lo slot.';
        this.isLoading = false;
      }
    });
  }

  openSlotModal(): void {
    this.schedulingError = '';
    this.showSlotModal = true;
  }

  closeSlotModal(): void {
    this.showSlotModal = false;
  }

  confirmAppointment(appointment: SchedulingAppointment): void {
    if (appointment.status === 'CONFIRMED') {
      return;
    }
    this.appointments = this.appointments.map((item) =>
      item.id === appointment.id ? { ...item, status: 'CONFIRMED' } : item
    );
  }

  cancelAppointment(appointment: SchedulingAppointment): void {
    this.appointments = this.appointments.filter((item) => item.id !== appointment.id);
    this.slots = this.slots.map((slot) =>
      slot.id === appointment.slotId ? { ...slot, status: 'AVAILABLE' } : slot
    );
  }

  confirmPatientAppointment(appointment: SchedulingAppointment): void {
    if (appointment.status === 'CONFIRMED') {
      return;
    }
    this.appointments = this.appointments.map((item) =>
      item.id === appointment.id ? { ...item, status: 'CONFIRMED' } : item
    );
  }

  openAppointmentRescheduleModal(appointment: SchedulingAppointment): void {
    this.rescheduleAppointment = appointment;
    this.rescheduleAppointmentDate = '';
    this.rescheduleAppointmentReason = appointment.reason;
    this.schedulingError = '';
    this.showAppointmentRescheduleModal = true;
  }

  closeAppointmentRescheduleModal(): void {
    this.showAppointmentRescheduleModal = false;
    this.rescheduleAppointment = null;
    this.rescheduleAppointmentDate = '';
    this.rescheduleAppointmentReason = '';
  }

  submitAppointmentReschedule(): void {
    if (!this.rescheduleAppointment) {
      this.schedulingError = 'Seleziona un appuntamento da riprogrammare.';
      return;
    }
    if (!this.rescheduleAppointmentDate || !this.rescheduleAppointmentReason.trim()) {
      this.schedulingError = 'Inserisci nuova data e motivazione per la riprogrammazione.';
      return;
    }
    this.slots = this.slots.map((slot) =>
      slot.id === this.rescheduleAppointment?.slotId ? { ...slot, date: this.rescheduleAppointmentDate } : slot
    );
    this.appointments = this.appointments.map((item) =>
      item.id === this.rescheduleAppointment?.id
        ? { ...item, status: 'PENDING', reason: this.rescheduleAppointmentReason }
        : item
    );
    this.schedulingError = '';
    this.closeAppointmentRescheduleModal();
  }

  openAppointmentRejectModal(appointment: SchedulingAppointment): void {
    this.rejectAppointmentTarget = appointment;
    this.rejectAppointmentReason = '';
    this.schedulingError = '';
    this.showAppointmentRejectModal = true;
  }

  closeAppointmentRejectModal(): void {
    this.showAppointmentRejectModal = false;
    this.rejectAppointmentTarget = null;
    this.rejectAppointmentReason = '';
  }

  submitAppointmentRejection(): void {
    if (!this.rejectAppointmentTarget) {
      this.schedulingError = 'Seleziona un appuntamento da rifiutare.';
      return;
    }
    if (!this.rejectAppointmentReason.trim()) {
      this.schedulingError = 'Inserisci una motivazione per il rifiuto.';
      return;
    }
    this.appointments = this.appointments.map((item) =>
      item.id === this.rejectAppointmentTarget?.id
        ? { ...item, status: 'REJECTED', reason: this.rejectAppointmentReason }
        : item
    );
    this.closeAppointmentRejectModal();
  }

  openDeleteAppointmentConfirm(appointment: SchedulingAppointment): void {
    this.deleteConfirmTarget = { type: 'appointment', value: appointment };
    this.deleteConfirmMessage = 'Confermi l’eliminazione dell’appuntamento?';
    this.showDeleteConfirmModal = true;
  }

  openBookingModal(): void {
    this.showBookingModal = true;
  }

  closeBookingModal(): void {
    this.showBookingModal = false;
  }

  openDocumentModal(): void {
    this.docsError = '';
    this.showDocumentModal = true;
  }

  closeDocumentModal(): void {
    this.showDocumentModal = false;
  }

  openConsentModal(editingId: number | null = null): void {
    this.docsError = '';
    this.editingConsentId = editingId;
    this.showConsentModal = true;
  }

  closeConsentModal(): void {
    this.showConsentModal = false;
    this.editingConsentId = null;
  }

  openDeleteDocumentConfirm(doc: DocumentItem): void {
    this.deleteConfirmTarget = { type: 'document', value: doc };
    this.deleteConfirmMessage = 'Confermi l’eliminazione del documento?';
    this.showDeleteConfirmModal = true;
  }

  openDeleteConsentConfirm(consent: ConsentItem): void {
    this.deleteConfirmTarget = { type: 'consent', value: consent };
    this.deleteConfirmMessage = 'Confermi l’eliminazione del consenso?';
    this.showDeleteConfirmModal = true;
  }

  closeDeleteConfirmModal(): void {
    this.showDeleteConfirmModal = false;
    this.deleteConfirmTarget = null;
    this.deleteConfirmMessage = '';
  }

  confirmDelete(): void {
    if (!this.deleteConfirmTarget) {
      return;
    }
    if (this.deleteConfirmTarget.type === 'appointment') {
      this.cancelAppointment(this.deleteConfirmTarget.value);
    } else if (this.deleteConfirmTarget.type === 'document') {
      this.deleteDocument(this.deleteConfirmTarget.value);
    } else {
      this.deleteConsent(this.deleteConfirmTarget.value);
    }
    this.closeDeleteConfirmModal();
  }

  loadDocs(): void {
    this.isLoading = true;
    this.docsError = '';
    this.api.request<DocumentItem[]>('GET', '/api/docs').subscribe({
      next: (docs) => {
        this.documents = [...docs];
        this.isLoading = false;
      },
      error: () => {
        this.docsError = 'Impossibile caricare i documenti.';
        this.isLoading = false;
      }
    });
    this.api.request<ConsentItem[]>('GET', '/api/consents').subscribe({
      next: (consents) => {
        this.consents = [...consents];
      },
      error: () => {
        this.docsError = 'Impossibile caricare i consensi.';
      }
    });
  }

  submitDocument(): void {
    if (!this.docForm.fileName.trim()) {
      this.docsError = 'Seleziona un documento dal tuo dispositivo.';
      return;
    }
    this.isLoading = true;
    this.docsError = '';
    if (!this.docForm.name.trim()) {
      this.docForm.name = this.docForm.fileName;
    }
    const payload: Record<string, unknown> = {
      type: this.docForm.type,
      name: this.docForm.name,
      notes: this.docForm.notes,
      uploadedBy: this.isDoctor ? 'DOCTOR' : 'PATIENT'
    };
    if (this.isDoctor) {
      payload['patientId'] = this.docForm.patientId ?? this.patients[0]?.id ?? 1;
    }
    this.api.request<DocumentItem>('POST', '/api/docs', payload).subscribe({
      next: (doc) => {
        this.documents = [
          ...this.documents,
          { ...doc, viewedByCurrentUser: true, uploadedBy: this.isDoctor ? 'DOCTOR' : 'PATIENT' }
        ];
        this.docForm.name = '';
        this.docForm.fileName = '';
        this.docForm.notes = '';
        this.closeDocumentModal();
        this.isLoading = false;
      },
      error: () => {
        this.docsError = 'Impossibile caricare il documento.';
        this.isLoading = false;
      }
    });
  }

  submitConsent(): void {
    this.isLoading = true;
    this.docsError = '';
    const payload: Record<string, unknown> = {
      consentType: this.consentForm.consentType,
      accepted: this.consentForm.accepted
    };
    if (this.editingConsentId) {
      payload['id'] = this.editingConsentId;
    }
    this.api.request<ConsentItem>('POST', '/api/consents', payload).subscribe({
      next: (consent) => {
        if (this.editingConsentId) {
          this.consents = this.consents.map((item) =>
            item.id === this.editingConsentId ? { ...item, ...consent } : item
          );
        } else if (!this.consents.some((item) => item.id === consent.id)) {
          this.consents = [...this.consents, consent];
        }
        this.closeConsentModal();
        this.isLoading = false;
      },
      error: () => {
        this.docsError = 'Impossibile registrare il consenso.';
        this.isLoading = false;
      }
    });
  }

  loadPayments(): void {
    this.isLoading = true;
    this.paymentsError = '';
    this.paymentsSuccess = '';
    this.api.request<PaymentItem[]>('GET', '/api/payments').subscribe({
      next: (payments) => {
        this.payments = payments;
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = 'Impossibile caricare i pagamenti.';
        this.isLoading = false;
      }
    });
    this.api.request<AdmissionItem[]>('GET', '/api/admissions').subscribe({
      next: (admissions) => {
        this.admissions = admissions;
      },
      error: () => {
        this.paymentsError = 'Impossibile caricare i ricoveri.';
      }
    });
  }

  loadAdmissions(): void {
    this.api.request<AdmissionItem[]>('GET', '/api/admissions').subscribe({
      next: (admissions) => {
        this.admissions = admissions;
      },
      error: () => {
        this.paymentsError = 'Impossibile caricare i ricoveri.';
      }
    });
  }

  submitPayment(): void {
    if (!this.paymentForm.paymentId) {
      this.paymentsError = 'Seleziona un pagamento in attesa.';
      return;
    }
    if (!this.paymentForm.amount || this.paymentForm.amount <= 0) {
      this.paymentsError = 'Inserisci l’importo del pagamento.';
      return;
    }
    if (!this.paymentForm.receiptName.trim()) {
      this.paymentsError = 'Carica la ricevuta del pagamento.';
      return;
    }
    const pendingPayment = this.payments.find((payment) => payment.id === this.paymentForm.paymentId);
    if (
      !pendingPayment ||
      (pendingPayment.status !== 'PENDING' && pendingPayment.status !== 'IN_ATTESA')
    ) {
      this.paymentsError = 'Pagamento selezionato non valido.';
      return;
    }
    this.isLoading = true;
    this.paymentsError = '';
    this.api.request<PaymentItem>('POST', '/api/payments', {
      paymentId: this.paymentForm.paymentId,
      status: 'PAID',
      receiptName: this.paymentForm.receiptName,
      amount: this.paymentForm.amount
    }).subscribe({
      next: (payment) => {
        this.payments = this.payments.map((item) => (item.id === payment.id ? payment : item));
        this.paymentForm.paymentId = null;
        this.paymentForm.receiptName = '';
        this.paymentForm.service = '';
        this.paymentForm.amount = 0;
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = 'Impossibile registrare il pagamento.';
        this.isLoading = false;
      }
    });
  }

  submitAdmission(): void {
    this.isLoading = true;
    this.paymentsError = '';
    this.api.request<AdmissionItem>('POST', '/api/admissions', {
      patientId: this.admissionForm.patientId,
      department: this.admissionForm.department,
      bedId: this.admissionForm.bedId
    }).subscribe({
      next: (admission) => {
        this.admissions = [...this.admissions, admission];
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = 'Impossibile registrare il ricovero.';
        this.isLoading = false;
      }
    });
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.notificationsError = '';
    this.api.request<NotificationItem[]>('GET', '/api/notifications').subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.isLoading = false;
      },
      error: () => {
        this.notificationsError = 'Impossibile caricare le notifiche.';
        this.isLoading = false;
      }
    });
  }

  submitNotification(): void {
    if (
      !this.notificationForm.recipient.trim() ||
      !this.notificationForm.subject.trim() ||
      !this.notificationForm.message.trim()
    ) {
      this.notificationsError = 'Inserisci destinatario, oggetto e messaggio.';
      return;
    }
    this.isLoading = true;
    this.notificationsError = '';
    this.api.request<NotificationItem>('POST', '/api/notifications', {
      recipient: this.notificationForm.recipient,
      channel: this.notificationForm.channel,
      subject: this.notificationForm.subject,
      message: this.notificationForm.message,
      notes: this.notificationForm.notes
    }).subscribe({
      next: (notification) => {
        this.notifications = [...this.notifications, notification];
        this.notificationForm.subject = '';
        this.notificationForm.message = '';
        this.notificationForm.notes = '';
        this.isLoading = false;
      },
      error: () => {
        this.notificationsError = 'Impossibile inviare la notifica.';
        this.isLoading = false;
      }
    });
  }

  saveNotificationPreferences(): void {
    this.notificationsError = '';
    this.notificationsSuccess = '';
    if (!this.notificationPrefs.email.trim()) {
      this.notificationsError = 'Inserisci una email valida per ricevere le notifiche.';
      return;
    }
    if (!this.notificationPrefs.phone.trim()) {
      this.notificationsError = 'Inserisci un numero di telefono valido per ricevere le notifiche.';
      return;
    }
    const hasChannel = Object.values(this.notificationPrefs.channels).some((value) => value);
    if (!hasChannel) {
      this.notificationsError = 'Seleziona almeno un canale di notifica.';
      return;
    }
    const hasType = Object.values(this.notificationPrefs.types).some((value) => value);
    if (!hasType) {
      this.notificationsError = 'Seleziona almeno un tipo di notifica.';
      return;
    }
    this.notificationsSuccess = 'Preferenze notifiche aggiornate.';
    this.closeNotificationContactsModal();
    this.closeNotificationPreferencesModal();
  }

  openNotificationContactsModal(): void {
    this.notificationsError = '';
    this.notificationsSuccess = '';
    this.showNotificationContactsModal = true;
  }

  closeNotificationContactsModal(): void {
    this.showNotificationContactsModal = false;
  }

  openNotificationPreferencesModal(): void {
    this.notificationsError = '';
    this.notificationsSuccess = '';
    this.showNotificationPreferencesModal = true;
  }

  closeNotificationPreferencesModal(): void {
    this.showNotificationPreferencesModal = false;
  }

  openAdmissionRescheduleModal(admission: AdmissionItem): void {
    this.rescheduleAdmission = admission;
    this.rescheduleForm = {
      date: '',
      reason: admission.notes ?? ''
    };
    this.rescheduleError = '';
    this.rescheduleSuccess = '';
    this.showAdmissionRescheduleModal = true;
  }

  closeAdmissionRescheduleModal(): void {
    this.showAdmissionRescheduleModal = false;
    this.rescheduleAdmission = null;
  }

  openAdmissionRejectModal(admission: AdmissionItem): void {
    this.rejectAdmissionTarget = admission;
    this.rejectAdmissionReason = '';
    this.rescheduleError = '';
    this.showAdmissionRejectModal = true;
  }

  closeAdmissionRejectModal(): void {
    this.showAdmissionRejectModal = false;
    this.rejectAdmissionTarget = null;
    this.rejectAdmissionReason = '';
  }

  submitAdmissionRejection(): void {
    if (!this.rejectAdmissionTarget) {
      this.rescheduleError = 'Seleziona un ricovero da rifiutare.';
      return;
    }
    if (!this.rejectAdmissionReason.trim()) {
      this.rescheduleError = 'Inserisci una motivazione per il rifiuto.';
      return;
    }
    this.admissions = this.admissions.map((item) =>
      item.id === this.rejectAdmissionTarget?.id
        ? { ...item, status: 'REJECTED', notes: this.rejectAdmissionReason }
        : item
    );
    this.closeAdmissionRejectModal();
  }

  submitAdmissionReschedule(): void {
    this.rescheduleError = '';
    if (!this.rescheduleAdmission) {
      this.rescheduleError = 'Seleziona un ricovero da ripianificare.';
      return;
    }
    if (!this.rescheduleForm.date.trim() || !this.rescheduleForm.reason.trim()) {
      this.rescheduleError = 'Inserisci data e motivazione per la ripianificazione.';
      return;
    }
    this.admissions = this.admissions.map((item) =>
      item.id === this.rescheduleAdmission?.id
        ? {
            ...item,
            status: 'RESCHEDULED',
            admittedAt: this.rescheduleForm.date,
            notes: this.rescheduleForm.reason
          }
        : item
    );
    this.rescheduleSuccess = 'Richiesta di ripianificazione inviata.';
    this.closeAdmissionRescheduleModal();
  }

  confirmAdmission(admission: AdmissionItem): void {
    if (admission.status === 'CONFIRMED') {
      return;
    }
    this.admissions = this.admissions.map((item) =>
      item.id === admission.id ? { ...item, status: 'CONFIRMED' } : item
    );
  }

  rejectAdmission(admission: AdmissionItem): void {
    if (admission.status === 'CONFIRMED' || admission.status === 'REJECTED') {
      return;
    }
    this.openAdmissionRejectModal(admission);
  }

  markPaymentAsPaid(payment: PaymentItem): void {
    if (payment.status !== 'PENDING' && payment.status !== 'IN_ATTESA') {
      return;
    }
    this.payments = this.payments.map((item) => (item.id === payment.id ? { ...item, status: 'PAID' } : item));
  }

  openPaymentReceiptUpload(payment: PaymentItem, input: HTMLInputElement): void {
    if (!this.canAttachPaymentReceipt(payment)) {
      return;
    }
    input.click();
  }

  onPaymentReceiptSelected(event: Event, payment: PaymentItem): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.payments = this.payments.map((item) =>
      item.id === payment.id ? { ...item, receiptName: file.name, status: 'RECEIPT_UPLOADED' } : item
    );
    input.value = '';
  }

  openPaymentQuestionModal(payment: PaymentItem): void {
    this.selectedPayment = payment;
    this.paymentQuestion = '';
    this.paymentsError = '';
    this.showPaymentQuestionModal = true;
  }

  closePaymentQuestionModal(): void {
    this.showPaymentQuestionModal = false;
    this.selectedPayment = null;
    this.paymentQuestion = '';
  }

  submitPaymentQuestion(): void {
    if (!this.selectedPayment) {
      this.paymentsError = 'Seleziona un pagamento valido.';
      return;
    }
    if (!this.paymentQuestion.trim()) {
      this.paymentsError = 'Inserisci una domanda per la struttura.';
      return;
    }
    this.paymentsSuccess = `Richiesta inviata per il pagamento #${this.selectedPayment.id}.`;
    this.closePaymentQuestionModal();
  }

  confirmPayment(payment: PaymentItem): void {
    if (payment.status !== 'RECEIPT_UPLOADED') {
      return;
    }
    this.payments = this.payments.map((item) => (item.id === payment.id ? { ...item, status: 'CONFIRMED' } : item));
  }

  loadPrescriptions(): void {
    this.isLoading = true;
    this.prescribingError = '';
    this.api.request<PrescriptionItem[]>('GET', '/api/prescriptions').subscribe({
      next: (prescriptions) => {
        this.prescriptions = prescriptions;
        this.isLoading = false;
      },
      error: () => {
        this.prescribingError = 'Impossibile caricare le prescrizioni.';
        this.isLoading = false;
      }
    });
  }

  submitPrescription(): void {
    if (!this.prescriptionForm.drug.trim() || !this.prescriptionForm.dosage.trim()) {
      this.prescribingError = 'Inserisci farmaco e posologia.';
      return;
    }
    if (!this.prescriptionForm.durationDays) {
      this.prescribingError = 'Inserisci la durata della terapia.';
      return;
    }
    this.isLoading = true;
    this.prescribingError = '';
    this.api.request<PrescriptionItem>('POST', '/api/prescribing/prescriptions', {
      patientId: this.prescriptionForm.patientId,
      doctorId: this.currentDoctorId,
      drug: this.prescriptionForm.drug,
      dosage: this.prescriptionForm.dosage,
      durationDays: this.prescriptionForm.durationDays
    }).subscribe({
      next: (prescription) => {
        this.prescriptions = [...this.prescriptions, prescription];
        this.prescriptionForm.drug = '';
        this.prescriptionForm.dosage = '';
        this.prescriptionForm.durationDays = 10;
        this.isLoading = false;
      },
      error: () => {
        this.prescribingError = 'Impossibile registrare la prescrizione.';
        this.isLoading = false;
      }
    });
  }

  confirmPrescription(prescription: PrescriptionItem): void {
    this.prescribingError = '';
    this.prescriptions = this.prescriptions.map((item) =>
      item.id === prescription.id ? { ...item, status: 'CONFIRMED' } : item
    );
  }

  openPrescriptionRejectModal(prescription: PrescriptionItem): void {
    this.rejectPrescriptionTarget = prescription;
    this.rejectPrescriptionReason = '';
    this.prescribingError = '';
    this.showPrescriptionRejectModal = true;
  }

  closePrescriptionRejectModal(): void {
    this.showPrescriptionRejectModal = false;
    this.rejectPrescriptionTarget = null;
    this.rejectPrescriptionReason = '';
    this.prescribingError = '';
  }

  submitPrescriptionRejection(): void {
    if (!this.rejectPrescriptionTarget) {
      this.prescribingError = 'Seleziona una prescrizione valida.';
      return;
    }
    if (!this.rejectPrescriptionReason.trim()) {
      this.prescribingError = 'Inserisci una motivazione per il rifiuto.';
      return;
    }
    this.prescribingError = '';
    this.prescriptions = this.prescriptions.map((item) =>
      item.id === this.rejectPrescriptionTarget?.id
        ? { ...item, status: 'REJECTED', notes: this.rejectPrescriptionReason }
        : item
    );
    this.closePrescriptionRejectModal();
  }

  openPrescriptionQuestionModal(prescription: PrescriptionItem): void {
    this.selectedPrescription = prescription;
    this.prescriptionQuestion = '';
    this.prescribingError = '';
    this.showPrescriptionQuestionModal = true;
  }

  closePrescriptionQuestionModal(): void {
    this.showPrescriptionQuestionModal = false;
    this.selectedPrescription = null;
    this.prescriptionQuestion = '';
  }

  submitPrescriptionQuestion(): void {
    if (!this.selectedPrescription) {
      this.prescribingError = 'Seleziona una prescrizione valida.';
      return;
    }
    if (!this.prescriptionQuestion.trim()) {
      this.prescribingError = 'Inserisci una domanda per il medico.';
      return;
    }
    this.prescribingError = '';
    this.closePrescriptionQuestionModal();
  }

  viewDocument(doc: DocumentItem): void {
    this.docsError = '';
    const previewWindow = window.open('', '_blank', 'noopener');
    if (!previewWindow) {
      this.docsError = 'Impossibile aprire l’anteprima del documento.';
      return;
    }
    previewWindow.document.write(`<pre>${JSON.stringify(doc, null, 2)}</pre>`);
    previewWindow.document.close();
  }

  deleteDocument(doc: DocumentItem): void {
    this.documents = this.documents.filter((item) => item.id !== doc.id);
  }

  viewPaymentReceipt(payment: PaymentItem): void {
    this.paymentsError = '';
    if (!payment.receiptName) {
      this.paymentsError = 'Nessuna ricevuta disponibile per questo pagamento.';
      return;
    }
    const previewWindow = window.open('', '_blank', 'noopener');
    if (!previewWindow) {
      this.paymentsError = 'Impossibile aprire la ricevuta di pagamento.';
      return;
    }
    previewWindow.document.write(`<pre>Ricevuta: ${payment.receiptName}</pre>`);
    previewWindow.document.close();
  }

  deletePayment(payment: PaymentItem): void {
    this.payments = this.payments.filter((item) => item.id !== payment.id);
  }

  deleteConsent(consent: ConsentItem): void {
    this.consents = this.consents.filter((item) => item.id !== consent.id);
  }

  handleDocumentFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    const file = input?.files?.[0];
    if (!file) {
      return;
    }
    this.docForm.fileName = file.name;
    if (!this.docForm.name.trim()) {
      this.docForm.name = file.name;
    }
  }

  handlePaymentReceiptSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    const file = input?.files?.[0];
    if (!file) {
      return;
    }
    this.paymentForm.receiptName = file.name;
  }

  handlePaymentSelectionChange(paymentId: number | null): void {
    if (!paymentId) {
      this.paymentForm.amount = 0;
      this.paymentForm.service = '';
      return;
    }
    const pendingPayment = this.payments.find((payment) => payment.id === paymentId);
    if (!pendingPayment) {
      this.paymentForm.amount = 0;
      this.paymentForm.service = '';
      return;
    }
    this.paymentForm.amount = pendingPayment.amount;
    this.paymentForm.service = pendingPayment.service;
  }

  loadPatients(): void {
    this.api.request<PatientItem[]>('GET', '/api/patients').subscribe({
      next: (patients) => {
        this.patients = patients;
        if (!this.prescriptionForm.patientId && patients.length) {
          this.prescriptionForm.patientId = patients[0].id;
        }
        if (!this.docForm.patientId && patients.length) {
          this.docForm.patientId = patients[0].id;
        }
        if (!this.televisitForm.patientId && patients.length) {
          this.televisitForm.patientId = patients[0].id;
        }
      },
      error: () => {
        this.directoryError = 'Impossibile caricare i pazienti.';
      }
    });
  }

  loadTelevisits(): void {
    this.isLoading = true;
    this.televisitError = '';
    this.api.request<TelevisitItem[]>('GET', '/api/televisit').subscribe({
      next: (televisits) => {
        this.televisits = televisits;
        this.isLoading = false;
      },
      error: () => {
        this.televisitError = 'Impossibile caricare le sessioni.';
        this.isLoading = false;
      }
    });
  }

  submitTelevisit(): void {
    this.isLoading = true;
    this.televisitError = '';
    const appointmentId = this.televisitForm.patientId ?? this.televisitForm.appointmentId;
    if (!appointmentId) {
      this.televisitError = this.isDoctor
        ? 'Seleziona il paziente per la televisita.'
        : 'Inserisci l’identificativo dell’appuntamento.';
      this.isLoading = false;
      return;
    }
    this.api.request<TelevisitItem>('POST', '/api/televisit', {
      appointmentId,
      patientId: this.televisitForm.patientId,
      provider: this.televisitForm.provider
    }).subscribe({
      next: (televisit) => {
        this.televisits = [...this.televisits, televisit];
        this.televisitForm.patientId = this.patients[0]?.id ?? null;
        this.isLoading = false;
      },
      error: () => {
        this.televisitError = 'Impossibile avviare la sessione.';
        this.isLoading = false;
      }
    });
  }

  joinTelevisit(televisit: TelevisitItem): void {
    if (!televisit.token) {
      this.televisitError = 'Token televisita non disponibile.';
      return;
    }
    this.televisitError = '';
    window.open(`/televisit/${televisit.token}`, '_blank');
  }

  loadDirectory(): void {
    this.isLoading = true;
    this.directoryError = '';
    this.api.request<DoctorItem[]>('GET', '/api/admin/doctors').subscribe({
      next: (doctors) => {
        this.doctors = doctors;
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = 'Impossibile caricare i medici.';
        this.isLoading = false;
      }
    });
    this.api.request<PatientItem[]>('GET', '/api/admin/patients').subscribe({
      next: (patients) => {
        this.patients = patients;
      },
      error: () => {
        this.directoryError = 'Impossibile caricare i pazienti.';
      }
    });
    this.api.request<DepartmentItem[]>('GET', '/api/departments').subscribe({
      next: (departments) => {
        this.departments = departments;
      },
      error: () => {
        this.directoryError = 'Impossibile caricare i reparti.';
      }
    });
    this.api.request<SpecialityItem[]>('GET', '/api/specialities').subscribe({
      next: (specialities) => {
        this.specialities = specialities;
      },
      error: () => {
        this.directoryError = 'Impossibile caricare le specialità.';
      }
    });
  }

  submitDoctor(): void {
    if (!this.doctorForm.firstName.trim() || !this.doctorForm.lastName.trim()) {
      this.directoryError = 'Inserisci nome e cognome del medico.';
      return;
    }
    this.isLoading = true;
    this.directoryError = '';
    this.api.request<DoctorItem>('POST', '/api/admin/doctors', {
      firstName: this.doctorForm.firstName,
      lastName: this.doctorForm.lastName,
      speciality: this.doctorForm.speciality
    }).subscribe({
      next: (doctor) => {
        this.doctors = [...this.doctors, doctor];
        this.doctorForm.firstName = '';
        this.doctorForm.lastName = '';
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = 'Impossibile creare il medico.';
        this.isLoading = false;
      }
    });
  }

  submitPatient(): void {
    if (!this.patientForm.firstName.trim() || !this.patientForm.lastName.trim() || !this.patientForm.email.trim()) {
      this.directoryError = 'Inserisci nome, cognome ed email del paziente.';
      return;
    }
    this.isLoading = true;
    this.directoryError = '';
    this.api.request<PatientItem>('POST', '/api/admin/patients', {
      firstName: this.patientForm.firstName,
      lastName: this.patientForm.lastName,
      email: this.patientForm.email
    }).subscribe({
      next: (patient) => {
        this.patients = [...this.patients, patient];
        this.patientForm.firstName = '';
        this.patientForm.lastName = '';
        this.patientForm.email = '';
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = 'Impossibile creare il paziente.';
        this.isLoading = false;
      }
    });
  }

  loadAudit(): void {
    this.isLoading = true;
    this.auditError = '';
    this.api.request<AuditItem[]>('GET', '/api/audit').subscribe({
      next: (events) => {
        this.auditEvents = events;
        this.isLoading = false;
      },
      error: () => {
        this.auditError = 'Impossibile caricare l’audit trail.';
        this.isLoading = false;
      }
    });
  }

  loadAdminTelevisit(): void {
    this.televisitError = '';
    this.loadTelevisits();
    this.loadAdmissions();
  }
}

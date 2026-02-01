import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../../core/services/api.service';
import { AuthService } from '../../../../core/auth/auth.service';

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

interface PaymentPage {
  content: PaymentItem[];
}

interface AdmissionItem {
  id: number;
  patientId: number;
  department: string;
  bedId?: number;
  status: string;
  admittedAt: string;
  notes?: string;
  appointmentId?: number;
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

interface NotificationPage {
  content: NotificationItem[];
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
  patientQuestion?: string;
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
  departmentCode?: string;
  facilityCode?: string;
  email?: string;
  phone?: string;
}

interface DoctorApiItem {
  id: number;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  departmentCode?: string;
  facilityCode?: string;
  departments?: Array<{ code: string; name: string }>;
}

interface PatientItem {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
}

interface FacilityItem {
  id?: number;
  code: string;
  name: string;
}

interface DepartmentItem {
  id?: number;
  code: string;
  name: string;
  facilityCode?: string;
}

interface AuditItem {
  id: number;
  action: string;
  actor: string;
  timestamp: string;
}

interface AuditEventResponse {
  id: number;
  action: string;
  actorType: string;
  actorId: string;
  occurredAt: string;
}

interface PagedResponse<T> {
  content?: T[];
}

export class ResourcePageState {
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
  appointmentsLoadError = '';
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
  docFilterPatientId: number | null = null;
  appointmentPatientFilterId: number | null = null;
  admissionPatientFilterId: number | null = null;
  televisitPatientFilterId: number | null = null;
  consentPatientFilterId: number | null = null;
  prescriptionPatientFilterId: number | null = null;
  consentForm = {
    consentType: 'GDPR',
    accepted: true
  };
  editingConsentId: number | null = null;
  payments: PaymentItem[] = [];
  admissions: AdmissionItem[] = [];
  showAdmissionRescheduleModal = false;
  showAdmissionProposalModal = false;
  rescheduleAdmission: AdmissionItem | null = null;
  rescheduleForm = {
    date: '',
    reason: ''
  };
  showAdmissionNoteModal = false;
  admissionNoteTarget: AdmissionItem | null = null;
  rescheduleError = '';
  rescheduleSuccess = '';
  admissionProposalError = '';
  admissionProposalForm = {
    appointmentId: null as number | null,
    date: '',
    reason: ''
  };
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
  showAdminNotificationModal = false;
  showAdminPaymentModal = false;
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
  showPrescriptionMessageModal = false;
  selectedPrescription: PrescriptionItem | null = null;
  prescriptionQuestion = '';
  prescriptionMessageText = '';
  rejectPrescriptionTarget: PrescriptionItem | null = null;
  rejectPrescriptionReason = '';
  showDeleteConfirmModal = false;
  deleteConfirmMessage = '';
  deleteConfirmTarget:
    | { type: 'appointment'; value: SchedulingAppointment }
    | { type: 'document'; value: DocumentItem }
    | { type: 'consent'; value: ConsentItem }
    | { type: 'prescription'; value: PrescriptionItem }
    | null = null;
  showPrescriptionModal = false;
  editingPrescriptionId: number | null = null;
  prescriptionForm = {
    patientId: 1,
    drug: '',
    dosage: '',
    durationDays: 10
  };
  televisits: TelevisitItem[] = [];
  televisitError = '';
  showTelevisitModal = false;
  showTelevisitDeleteModal = false;
  showTelevisitRescheduleModal = false;
  selectedTelevisit: TelevisitItem | null = null;
  televisitDeleteReason = '';
  televisitRescheduleDate = '';
  televisitRescheduleTime = '';
  televisitForm = {
    appointmentId: 1,
    patientId: null as number | null,
    provider: 'LIVEKIT'
  };
  showAdminTelevisitModal = false;
  showAdminAdmissionModal = false;
  showSlotModal = false;
  doctors: DoctorItem[] = [];
  patients: PatientItem[] = [];
  facilities: FacilityItem[] = [];
  departments: DepartmentItem[] = [];
  directoryError = '';
  showDoctorModal = false;
  showPatientModal = false;
  showFacilityModal = false;
  showDepartmentModal = false;
  showEditDoctorModal = false;
  showEditPatientModal = false;
  editDoctorTarget: DoctorItem | null = null;
  editPatientTarget: PatientItem | null = null;
  doctorForm = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    departmentCode: ''
  };
  patientForm = {
    firstName: '',
    lastName: '',
    email: '',
    phone: ''
  };
  facilityForm = {
    code: '',
    name: ''
  };
  departmentForm = {
    code: '',
    name: '',
    facilityCode: ''
  };
  editDoctorForm = {
    email: '',
    phone: ''
  };
  editPatientForm = {
    email: '',
    phone: ''
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
      this.loadPatients();
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
      if (this.isAdmin) {
        this.loadPayments();
      }
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
    this.appointmentsLoadError = '';
    this.api.request<SchedulingSlot[] | PagedResponse<SchedulingSlot>>('GET', '/api/slots').subscribe({
      next: (slots) => {
        this.slots = this.normalizeList(slots);
        this.isLoading = false;
      },
      error: () => {
        this.schedulingError = 'Impossibile caricare gli slot disponibili.';
        this.isLoading = false;
      }
    });
    this.api.request<SchedulingAppointment[] | PagedResponse<SchedulingAppointment>>('GET', '/api/appointments').subscribe({
      next: (appointments) => {
        this.appointments = this.normalizeList(appointments);
        this.appointmentsLoadError = '';
      },
      error: (err: { error?: { detail?: string } }) => {
        const detail = err?.error?.detail;
        this.appointmentsLoadError = detail || 'Impossibile caricare gli appuntamenti.';
      }
    });
    this.api.request<DoctorItem[] | PagedResponse<DoctorApiItem>>('GET', '/api/doctors').subscribe({
      next: (doctors) => {
        this.doctors = this.normalizeDoctorList(doctors);
      },
      error: () => {
        this.schedulingError = 'Impossibile caricare i medici.';
      }
    });
    this.api.request<FacilityItem[]>('GET', '/api/facilities').subscribe({
      next: (facilities) => {
        this.facilities = facilities;
      },
      error: () => {
        this.schedulingError = 'Impossibile caricare le strutture.';
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
    this.api.request<DoctorItem[] | PagedResponse<DoctorApiItem>>('GET', '/api/doctors').subscribe({
      next: (doctors) => {
        this.doctors = this.normalizeDoctorList(doctors);
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

  getAppointmentById(appointmentId: number): SchedulingAppointment | undefined {
    return this.appointments.find((appointment) => appointment.id === appointmentId);
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

  get isAdmin(): boolean {
    return this.auth.hasRole('ROLE_ADMIN');
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
      PROPOSED: 'Proposta inviata',
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

  private toUpperCamelCase(value: string): string {
    const trimmed = value?.trim();
    if (!trimmed) {
      return value;
    }
    const lower = trimmed.toLowerCase();
    return `${lower.charAt(0).toUpperCase()}${lower.slice(1)}`;
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

  get filteredDoctorReceivedAppointments(): SchedulingAppointment[] {
    const appointments = this.doctorReceivedAppointments;
    if (!this.isDoctor || !this.appointmentPatientFilterId) {
      return appointments;
    }
    return appointments.filter((appointment) => appointment.patientId === this.appointmentPatientFilterId);
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
          this.getDoctorById(slot.doctorId)?.departmentCode === this.bookingForm.department
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

  get filteredDocuments(): DocumentItem[] {
    if (this.isDoctor && this.docFilterPatientId) {
      return this.documents.filter((doc) => doc.patientId === this.docFilterPatientId);
    }
    if (this.isPatient) {
      return this.documents.filter((doc) => doc.patientId === 1);
    }
    return this.documents;
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

  get filteredConsents(): ConsentItem[] {
    const consents = this.visibleConsents;
    if (!this.isDoctor || !this.consentPatientFilterId) {
      return consents;
    }
    return consents.filter((consent) => consent.patientId === this.consentPatientFilterId);
  }

  get visiblePrescriptions(): PrescriptionItem[] {
    if (this.isPatient) {
      return this.prescriptions.filter((prescription) => prescription.patientId === 1);
    }
    return this.prescriptions;
  }

  get filteredPrescriptions(): PrescriptionItem[] {
    const prescriptions = this.visiblePrescriptions;
    if (!this.isDoctor || !this.prescriptionPatientFilterId) {
      return prescriptions;
    }
    return prescriptions.filter((prescription) => prescription.patientId === this.prescriptionPatientFilterId);
  }

  get visibleAdmissions(): AdmissionItem[] {
    if (this.isDoctor) {
      const doctorAppointments = this.appointments.filter((appointment) => appointment.doctorId === this.currentDoctorId);
      if (!doctorAppointments.length) {
        return this.admissions;
      }
      const patientIds = new Set(doctorAppointments.map((appointment) => appointment.patientId));
      const appointmentIds = new Set(doctorAppointments.map((appointment) => appointment.id));
      return this.admissions.filter((admission) =>
        admission.appointmentId ? appointmentIds.has(admission.appointmentId) : patientIds.has(admission.patientId)
      );
    }
    if (this.isPatient) {
      return this.admissions.filter((admission) => admission.patientId === 1);
    }
    return this.admissions;
  }

  get filteredAdmissions(): AdmissionItem[] {
    const admissions = this.visibleAdmissions;
    if (!this.isDoctor || !this.admissionPatientFilterId) {
      return admissions;
    }
    return admissions.filter((admission) => admission.patientId === this.admissionPatientFilterId);
  }

  get filteredTelevisits(): TelevisitItem[] {
    if (!this.isDoctor || !this.televisitPatientFilterId) {
      return this.televisits;
    }
    return this.televisits.filter(
      (televisit) => this.getAppointmentById(televisit.appointmentId)?.patientId === this.televisitPatientFilterId
    );
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

  getPrescriptionDoctorStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Inviato al paziente',
      PENDING: 'Inviato al paziente',
      CONFIRMED: 'Confermato dal paziente',
      REJECTED: 'Rifiutato dal paziente'
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
    return this.doctors[0]?.departmentCode ?? '';
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

  getTelevisitAppointmentLabel(appointmentId: number): string {
    const appointment = this.getAppointmentById(appointmentId);
    if (!appointment) {
      return `Prenotazione ${appointmentId}`;
    }
    const slot = this.getSlotById(appointment.slotId);
    const dateLabel = slot ? `${this.formatDate(slot.date)} • ${slot.time}` : '-';
    return `${this.getPatientLabel(appointment.patientId)} • ${dateLabel}`;
  }

  getTelevisitDepartmentLabel(televisit: TelevisitItem): string {
    const appointment = this.getAppointmentById(televisit.appointmentId);
    if (!appointment) {
      return '-';
    }
    return this.getDepartmentLabel(this.getDoctorById(appointment.doctorId)?.departmentCode || '-');
  }

  getTelevisitPatientLabel(televisit: TelevisitItem): string {
    const appointment = this.getAppointmentById(televisit.appointmentId);
    if (!appointment) {
      return '-';
    }
    return this.getPatientLabel(appointment.patientId);
  }

  getTelevisitDateTimeLabel(televisit: TelevisitItem): string {
    const appointment = this.getAppointmentById(televisit.appointmentId);
    if (!appointment) {
      return '-';
    }
    const slot = this.getSlotById(appointment.slotId);
    if (!slot) {
      return '-';
    }
    return `${this.formatDate(slot.date)} • ${slot.time}`;
  }

  getTelevisitBookingStatusLabel(televisit: TelevisitItem): string {
    const appointment = this.getAppointmentById(televisit.appointmentId);
    if (!appointment) {
      return '-';
    }
    return this.getAppointmentStatusLabel(appointment.status);
  }

  getSlotDateTimeValue(slot?: SchedulingSlot): Date | null {
    if (!slot?.date || !slot.time) {
      return null;
    }
    const dateTimeValue = slot.date.includes('T') ? slot.date : `${slot.date}T${slot.time}`;
    const parsed = new Date(dateTimeValue);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  getAppointmentDateTimeValue(appointment: SchedulingAppointment): Date | null {
    const slot = this.getSlotById(appointment.slotId);
    return this.getSlotDateTimeValue(slot);
  }

  getAppointmentDateTimeLabel(appointment: SchedulingAppointment): string {
    const slot = this.getSlotById(appointment.slotId);
    if (!slot) {
      return '-';
    }
    return `${this.formatDate(slot.date)} • ${slot.time}`;
  }

  getAdmissionAppointmentLabel(admission: AdmissionItem): string {
    if (!admission.appointmentId) {
      return '-';
    }
    const appointment = this.getAppointmentById(admission.appointmentId);
    if (!appointment) {
      return `Visita ${admission.appointmentId}`;
    }
    return `${this.getPatientLabel(appointment.patientId)} • ${this.getAppointmentDateTimeLabel(appointment)}`;
  }

  get completedDoctorAppointments(): SchedulingAppointment[] {
    if (!this.isDoctor) {
      return [];
    }
    return this.appointments.filter((appointment) => {
      if (appointment.doctorId !== this.currentDoctorId || appointment.status !== 'CONFIRMED') {
        return false;
      }
      const dateTime = this.getAppointmentDateTimeValue(appointment);
      return dateTime ? dateTime.getTime() < Date.now() : false;
    });
  }

  get admissionProposalAppointment(): SchedulingAppointment | null {
    if (!this.admissionProposalForm.appointmentId) {
      return null;
    }
    return this.getAppointmentById(this.admissionProposalForm.appointmentId) ?? null;
  }

  canRescheduleTelevisit(televisit: TelevisitItem): boolean {
    const appointment = this.getAppointmentById(televisit.appointmentId);
    if (!appointment) {
      return false;
    }
    const slot = this.getSlotById(appointment.slotId);
    const parsed = this.getSlotDateTimeValue(slot);
    return parsed ? parsed.getTime() >= Date.now() : false;
  }

  submitSlot(): void {
    if (!this.slotForm.date || !this.slotForm.time) {
      this.schedulingError = 'Inserisci data e ora dello slot.';
      return;
    }
    this.isLoading = true;
    this.schedulingError = '';
    const startAt = new Date(`${this.slotForm.date}T${this.slotForm.time}:00`).toISOString();
    const endAt = new Date(new Date(`${this.slotForm.date}T${this.slotForm.time}:00`).getTime() + 30 * 60 * 1000).toISOString();
    const mode = this.slotForm.modality === 'REMOTE' ? 'TELEVISIT' : 'IN_PERSON';
    this.api.request<SchedulingSlot>('POST', '/api/slots', {
      doctorId: this.currentDoctorId,
      departmentCode: this.currentDoctorDepartment,
      mode,
      startAt,
      endAt
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
    } else if (this.deleteConfirmTarget.type === 'prescription') {
      this.deletePrescription(this.deleteConfirmTarget.value);
    } else {
      this.deleteConsent(this.deleteConfirmTarget.value);
    }
    this.closeDeleteConfirmModal();
  }

  loadDocs(): void {
    this.isLoading = true;
    this.docsError = '';
    this.api.request<DocumentItem[] | PagedResponse<DocumentItem>>('GET', '/api/docs').subscribe({
      next: (docs) => {
        this.documents = this.normalizeList(docs);
        this.isLoading = false;
      },
      error: () => {
        this.docsError = 'Impossibile caricare i documenti.';
        this.isLoading = false;
      }
    });
    const consentsPath = this.getConsentsPath();
    if (!consentsPath) {
      this.consents = [];
      return;
    }
    this.api.request<ConsentItem[]>('GET', consentsPath).subscribe({
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
    this.api.request<DocumentItem>('POST', '/api/docs/upload', payload).subscribe({
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
    const consentsPath = this.getConsentsPath();
    if (!consentsPath) {
      this.docsError = 'Operazione non disponibile per il profilo corrente.';
      return;
    }
    this.isLoading = true;
    this.docsError = '';
    const payload: Record<string, unknown> = {
      consentType: this.consentForm.consentType,
      accepted: this.consentForm.accepted
    };
    if (this.editingConsentId) {
      payload['id'] = this.editingConsentId;
    }
    this.api.request<ConsentItem>('POST', consentsPath, payload).subscribe({
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
    this.api.request<PaymentItem[] | PaymentPage>('GET', '/api/payments').subscribe({
      next: (payments) => {
        if (Array.isArray(payments)) {
          this.payments = payments;
        } else {
          this.payments = payments?.content ?? [];
        }
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = 'Impossibile caricare i pagamenti.';
        this.isLoading = false;
      }
    });
    if (!this.isPatient) {
      this.api.request<AdmissionItem[] | PagedResponse<AdmissionItem>>('GET', '/api/admissions').subscribe({
        next: (admissions) => {
          this.admissions = this.normalizeList(admissions);
        },
        error: () => {
          this.paymentsError = 'Impossibile caricare i ricoveri.';
        }
      });
    } else {
      this.admissions = [];
    }
  }

  loadAdmissions(): void {
    const endpoint = this.isPatient ? '/api/admissions/me' : '/api/admissions';
    this.api.request<AdmissionItem[] | PagedResponse<AdmissionItem>>('GET', endpoint).subscribe({
      next: (admissions) => {
        this.admissions = this.normalizeList(admissions);
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
        if (this.showAdminPaymentModal) {
          this.closeAdminPaymentModal();
        }
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
        if (this.showAdminAdmissionModal) {
          this.closeAdminAdmissionModal();
        }
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = 'Impossibile registrare il ricovero.';
        this.isLoading = false;
      }
    });
  }

  openAdmissionProposalModal(): void {
    if (!this.isDoctor) {
      return;
    }
    this.admissionProposalError = '';
    this.admissionProposalForm = {
      appointmentId: this.completedDoctorAppointments[0]?.id ?? null,
      date: '',
      reason: ''
    };
    this.showAdmissionProposalModal = true;
  }

  closeAdmissionProposalModal(): void {
    this.showAdmissionProposalModal = false;
    this.admissionProposalError = '';
  }

  openAdmissionNoteModal(admission: AdmissionItem): void {
    this.admissionNoteTarget = admission;
    this.showAdmissionNoteModal = true;
  }

  closeAdmissionNoteModal(): void {
    this.showAdmissionNoteModal = false;
    this.admissionNoteTarget = null;
  }

  submitAdmissionProposal(): void {
    if (!this.admissionProposalForm.appointmentId) {
      this.admissionProposalError = 'Seleziona una visita conclusa.';
      return;
    }
    if (!this.admissionProposalForm.date) {
      this.admissionProposalError = 'Inserisci una data proposta per il ricovero.';
      return;
    }
    if (!this.admissionProposalForm.reason.trim()) {
      this.admissionProposalError = 'Inserisci una motivazione per la proposta.';
      return;
    }
    const appointment = this.getAppointmentById(this.admissionProposalForm.appointmentId);
    if (!appointment) {
      this.admissionProposalError = 'Visita non trovata.';
      return;
    }
    const department =
      this.getDoctorById(appointment.doctorId)?.departmentCode ||
      this.currentDoctorDepartment ||
      'CARD';
    const nextId = Math.max(0, ...this.admissions.map((item) => item.id)) + 1;
    const notes = `Proposta da visita ${appointment.id}: ${appointment.reason}. ${this.admissionProposalForm.reason}`;
    const proposal: AdmissionItem = {
      id: nextId,
      patientId: appointment.patientId,
      department,
      status: 'PROPOSED',
      admittedAt: this.admissionProposalForm.date,
      notes,
      appointmentId: appointment.id
    };
    this.admissions = [...this.admissions, proposal];
    this.closeAdmissionProposalModal();
  }

  openAdminTelevisitModal(): void {
    this.televisitError = '';
    this.showAdminTelevisitModal = true;
  }

  closeAdminTelevisitModal(): void {
    this.showAdminTelevisitModal = false;
    this.televisitError = '';
  }

  openAdminAdmissionModal(): void {
    this.paymentsError = '';
    this.showAdminAdmissionModal = true;
  }

  closeAdminAdmissionModal(): void {
    this.showAdminAdmissionModal = false;
    this.paymentsError = '';
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.notificationsError = '';
    this.api.request<NotificationItem[] | NotificationPage>('GET', '/api/notifications').subscribe({
      next: (notifications) => {
        if (Array.isArray(notifications)) {
          this.notifications = notifications;
        } else {
          this.notifications = notifications?.content ?? [];
        }
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
    this.api.request<NotificationItem>('POST', '/api/admin/notifications', {
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
        if (this.showAdminNotificationModal) {
          this.closeAdminNotificationModal();
        }
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
    this.isLoading = true;
    this.api.request<PatientItem>('GET', '/api/patient/me').subscribe({
      next: (patient) => {
        this.notificationPrefs.email = patient.email;
        this.notificationPrefs.phone = patient.phone ?? '';
        this.isLoading = false;
        this.showNotificationContactsModal = true;
      },
      error: () => {
        this.notificationsError = 'Impossibile caricare i dati del profilo.';
        this.isLoading = false;
        this.showNotificationContactsModal = true;
      }
    });
  }

  closeNotificationContactsModal(): void {
    this.showNotificationContactsModal = false;
  }

  saveContactChanges(): void {
    this.notificationsError = '';
    this.notificationsSuccess = '';
    this.isLoading = true;
    this.api
      .request<PatientItem>('PATCH', '/api/patient/me', {
        phone: this.notificationPrefs.phone?.trim() || null
      })
      .subscribe({
        next: () => {
          this.notificationsSuccess = 'Contatto aggiornato con successo.';
          this.isLoading = false;
          this.closeNotificationContactsModal();
        },
        error: () => {
          this.notificationsError = 'Impossibile salvare le modifiche.';
          this.isLoading = false;
        }
      });
  }

  openNotificationPreferencesModal(): void {
    this.notificationsError = '';
    this.notificationsSuccess = '';
    this.showNotificationPreferencesModal = true;
  }

  closeNotificationPreferencesModal(): void {
    this.showNotificationPreferencesModal = false;
  }

  openAdminNotificationModal(): void {
    this.notificationsError = '';
    this.notificationsSuccess = '';
    this.showAdminNotificationModal = true;
  }

  closeAdminNotificationModal(): void {
    this.showAdminNotificationModal = false;
    this.notificationsError = '';
  }

  openAdminPaymentModal(): void {
    this.paymentsError = '';
    this.paymentsSuccess = '';
    if (!this.paymentForm.paymentId && this.payments.length) {
      this.paymentForm.paymentId = this.payments[0].id;
    }
    this.showAdminPaymentModal = true;
  }

  closeAdminPaymentModal(): void {
    this.showAdminPaymentModal = false;
    this.paymentsError = '';
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
    if (admission.status !== 'PROPOSED') {
      return;
    }
    this.admissions = this.admissions.map((item) =>
      item.id === admission.id ? { ...item, status: 'CONFIRMED' } : item
    );
  }

  rejectAdmission(admission: AdmissionItem): void {
    if (admission.status !== 'PROPOSED') {
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
    this.api.request<PrescriptionItem[] | PagedResponse<PrescriptionItem>>('GET', '/api/prescriptions').subscribe({
      next: (prescriptions) => {
        this.prescriptions = this.normalizeList(prescriptions);
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
    if (this.editingPrescriptionId) {
      this.prescriptions = this.prescriptions.map((item) =>
        item.id === this.editingPrescriptionId
          ? {
              ...item,
              patientId: this.prescriptionForm.patientId,
              drug: this.prescriptionForm.drug,
              dosage: this.prescriptionForm.dosage,
              durationDays: this.prescriptionForm.durationDays
            }
          : item
      );
      this.editingPrescriptionId = null;
      this.closePrescriptionModal();
      this.prescribingError = '';
      return;
    }
    this.isLoading = true;
    this.prescribingError = '';
    this.api.request<PrescriptionItem>('POST', '/api/doctor/prescriptions', {
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
        this.closePrescriptionModal();
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

  resendPrescription(prescription: PrescriptionItem): void {
    this.prescriptions = this.prescriptions.map((item) =>
      item.id === prescription.id ? { ...item, status: 'PENDING' } : item
    );
  }

  openTelevisitModal(): void {
    this.televisitError = '';
    if (this.isDoctor) {
      const confirmedAppointmentId = this.confirmedAppointments[0]?.id ?? null;
      this.televisitForm.appointmentId = confirmedAppointmentId ?? this.televisitForm.appointmentId;
    }
    this.showTelevisitModal = true;
  }

  closeTelevisitModal(): void {
    this.showTelevisitModal = false;
  }

  openTelevisitDeleteModal(televisit: TelevisitItem): void {
    this.selectedTelevisit = televisit;
    this.televisitDeleteReason = '';
    this.televisitError = '';
    this.showTelevisitDeleteModal = true;
  }

  closeTelevisitDeleteModal(): void {
    this.showTelevisitDeleteModal = false;
    this.selectedTelevisit = null;
    this.televisitDeleteReason = '';
  }

  submitTelevisitDelete(): void {
    if (!this.selectedTelevisit) {
      this.televisitError = 'Seleziona una televisita valida.';
      return;
    }
    if (!this.televisitDeleteReason.trim()) {
      this.televisitError = 'Inserisci una motivazione per l’eliminazione.';
      return;
    }
    this.televisits = this.televisits.filter((item) => item.id !== this.selectedTelevisit?.id);
    this.closeTelevisitDeleteModal();
  }

  openTelevisitRescheduleModal(televisit: TelevisitItem): void {
    this.selectedTelevisit = televisit;
    this.televisitRescheduleDate = '';
    this.televisitRescheduleTime = '';
    this.televisitError = '';
    this.showTelevisitRescheduleModal = true;
  }

  closeTelevisitRescheduleModal(): void {
    this.showTelevisitRescheduleModal = false;
    this.selectedTelevisit = null;
    this.televisitRescheduleDate = '';
    this.televisitRescheduleTime = '';
  }

  submitTelevisitReschedule(): void {
    if (!this.selectedTelevisit) {
      this.televisitError = 'Seleziona una televisita valida.';
      return;
    }
    if (!this.televisitRescheduleDate || !this.televisitRescheduleTime) {
      this.televisitError = 'Inserisci data e ora della televisita.';
      return;
    }
    const appointment = this.getAppointmentById(this.selectedTelevisit.appointmentId);
    if (!appointment) {
      this.televisitError = 'Prenotazione non trovata.';
      return;
    }
    this.slots = this.slots.map((slot) =>
      slot.id === appointment.slotId
        ? { ...slot, date: this.televisitRescheduleDate, time: this.televisitRescheduleTime }
        : slot
    );
    this.closeTelevisitRescheduleModal();
  }

  openPrescriptionMessageModal(prescription: PrescriptionItem): void {
    this.selectedPrescription = prescription;
    this.prescriptionMessageText = prescription.patientQuestion?.trim() || 'Nessuna domanda del paziente.';
    this.showPrescriptionMessageModal = true;
  }

  closePrescriptionMessageModal(): void {
    this.showPrescriptionMessageModal = false;
    this.prescriptionMessageText = '';
    this.selectedPrescription = null;
  }

  openPrescriptionModal(prescription?: PrescriptionItem): void {
    if (prescription) {
      this.editingPrescriptionId = prescription.id;
      this.prescriptionForm.patientId = prescription.patientId;
      this.prescriptionForm.drug = prescription.drug;
      this.prescriptionForm.dosage = prescription.dosage;
      this.prescriptionForm.durationDays = prescription.durationDays;
    } else {
      this.editingPrescriptionId = null;
      this.prescriptionForm.patientId = this.patients[0]?.id ?? 1;
      this.prescriptionForm.drug = '';
      this.prescriptionForm.dosage = '';
      this.prescriptionForm.durationDays = 10;
    }
    this.prescribingError = '';
    this.showPrescriptionModal = true;
  }

  closePrescriptionModal(): void {
    this.showPrescriptionModal = false;
    this.editingPrescriptionId = null;
  }

  openDeletePrescriptionConfirm(prescription: PrescriptionItem): void {
    this.deleteConfirmTarget = { type: 'prescription', value: prescription };
    this.deleteConfirmMessage = 'Confermi l’eliminazione della prescrizione?';
    this.showDeleteConfirmModal = true;
  }

  deletePrescription(prescription: PrescriptionItem): void {
    this.prescriptions = this.prescriptions.filter((item) => item.id !== prescription.id);
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
    if (this.isPatient) {
      return;
    }
    this.api.request<PatientItem[] | PagedResponse<PatientItem>>('GET', '/api/patients').subscribe({
      next: (patients) => {
        const normalizedPatients = this.normalizeList(patients);
        this.patients = normalizedPatients;
        if (!this.prescriptionForm.patientId && normalizedPatients.length) {
          this.prescriptionForm.patientId = normalizedPatients[0].id;
        }
        if (!this.docForm.patientId && normalizedPatients.length) {
          this.docForm.patientId = normalizedPatients[0].id;
        }
        if (!this.televisitForm.patientId && normalizedPatients.length) {
          this.televisitForm.patientId = normalizedPatients[0].id;
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
    this.api.request<TelevisitItem[] | PagedResponse<TelevisitItem>>('GET', '/api/televisits').subscribe({
      next: (televisits) => {
        this.televisits = this.normalizeList(televisits);
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
    const appointmentId = this.isDoctor
      ? this.televisitForm.appointmentId
      : this.televisitForm.patientId ?? this.televisitForm.appointmentId;
    if (!appointmentId) {
      this.televisitError = this.isDoctor
        ? 'Seleziona la prenotazione confermata.'
        : 'Inserisci l’identificativo dell’appuntamento.';
      this.isLoading = false;
      return;
    }
    const appointment = this.isDoctor ? this.getAppointmentById(appointmentId) : undefined;
    if (this.isDoctor && !appointment) {
      this.televisitError = 'Prenotazione non trovata.';
      this.isLoading = false;
      return;
    }
    const patientId = this.isDoctor ? appointment?.patientId ?? null : this.televisitForm.patientId;
    this.api.request<TelevisitItem>('POST', '/api/admin/televisits', {
      appointmentId,
      patientId,
      provider: this.televisitForm.provider
    }).subscribe({
      next: (televisit) => {
        this.televisits = [...this.televisits, televisit];
        this.televisitForm.patientId = this.patients[0]?.id ?? null;
        if (this.showAdminTelevisitModal) {
          this.closeAdminTelevisitModal();
        } else {
          this.closeTelevisitModal();
        }
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
    this.api.request<DoctorItem[] | PagedResponse<DoctorApiItem>>('GET', '/api/admin/doctors').subscribe({
      next: (doctors) => {
        this.doctors = this.normalizeDoctorList(doctors);
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = 'Impossibile caricare i medici.';
        this.isLoading = false;
      }
    });
    this.api.request<PatientItem[] | PagedResponse<PatientItem>>('GET', '/api/admin/patients').subscribe({
      next: (patients) => {
        this.patients = this.normalizeList(patients);
      },
      error: () => {
        this.directoryError = 'Impossibile caricare i pazienti.';
      }
    });
    this.api.request<FacilityItem[]>('GET', '/api/facilities').subscribe({
      next: (facilities) => {
        this.facilities = facilities;
      },
      error: () => {
        this.directoryError = 'Impossibile caricare le strutture.';
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
  }

  private normalizeList<T>(data?: T[] | PagedResponse<T>): T[] {
    if (!data) {
      return [];
    }
    if (Array.isArray(data)) {
      return data;
    }
    return data.content ?? [];
  }

  private normalizeDoctorList(data?: DoctorItem[] | PagedResponse<DoctorApiItem>): DoctorItem[] {
    return this.normalizeList(data).map((doctor) => {
      const apiDoctor = doctor as DoctorApiItem;
      const department =
        apiDoctor.departmentCode ??
        apiDoctor.departments?.[0]?.code ??
        '';
      const facility = apiDoctor.facilityCode ?? '';
      return {
        id: apiDoctor.id,
        firstName: apiDoctor.firstName,
        lastName: apiDoctor.lastName,
        departmentCode: department || undefined,
        facilityCode: facility || undefined,
        email: apiDoctor.email,
        phone: apiDoctor.phone
      };
    });
  }

  private get hasSchedulingAppointmentsAccess(): boolean {
    if (this.isAdmin) {
      return true;
    }
    if (this.isDoctor) {
      return Number.isFinite(Number(this.auth.getAccessTokenClaim('did')));
    }
    if (this.isPatient) {
      return Number.isFinite(Number(this.auth.getAccessTokenClaim('pid')));
    }
    return false;
  }

  openDoctorModal(): void {
    this.directoryError = '';
    if (!this.doctorForm.departmentCode && this.departments.length) {
      this.doctorForm.departmentCode = this.departments[0].code;
    }
    this.showDoctorModal = true;
  }

  closeDoctorModal(): void {
    this.showDoctorModal = false;
    this.directoryError = '';
  }

  openPatientModal(): void {
    this.directoryError = '';
    this.showPatientModal = true;
  }

  closePatientModal(): void {
    this.showPatientModal = false;
    this.directoryError = '';
  }

  openEditDoctorModal(doctor: DoctorItem): void {
    this.directoryError = '';
    this.editDoctorTarget = doctor;
    this.editDoctorForm.email = doctor.email ?? '';
    this.editDoctorForm.phone = doctor.phone ?? '';
    this.showEditDoctorModal = true;
  }

  closeEditDoctorModal(): void {
    this.showEditDoctorModal = false;
    this.editDoctorTarget = null;
    this.editDoctorForm.email = '';
    this.editDoctorForm.phone = '';
    this.directoryError = '';
  }

  openEditPatientModal(patient: PatientItem): void {
    this.directoryError = '';
    this.editPatientTarget = patient;
    this.editPatientForm.email = patient.email ?? '';
    this.editPatientForm.phone = patient.phone ?? '';
    this.showEditPatientModal = true;
  }

  closeEditPatientModal(): void {
    this.showEditPatientModal = false;
    this.editPatientTarget = null;
    this.editPatientForm.email = '';
    this.editPatientForm.phone = '';
    this.directoryError = '';
  }

  submitDoctor(): void {
    if (
      !this.doctorForm.firstName.trim() ||
      !this.doctorForm.lastName.trim() ||
      !this.doctorForm.email.trim() ||
      !this.doctorForm.departmentCode
    ) {
      this.directoryError = 'Inserisci nome, cognome, email e reparto del medico.';
      return;
    }
    this.isLoading = true;
    this.directoryError = '';
    this.api.request<DoctorItem>('POST', '/api/admin/doctors', {
      firstName: this.doctorForm.firstName,
      lastName: this.doctorForm.lastName,
      email: this.doctorForm.email,
      phone: this.doctorForm.phone?.trim() || undefined,
      departmentCode: this.doctorForm.departmentCode
    }).subscribe({
      next: (doctor) => {
        this.doctors = [...this.doctors, doctor];
        this.doctorForm.firstName = '';
        this.doctorForm.lastName = '';
        this.doctorForm.email = '';
        this.doctorForm.phone = '';
        this.doctorForm.departmentCode = this.departments[0]?.code ?? '';
        this.closeDoctorModal();
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
      email: this.patientForm.email,
      phone: this.patientForm.phone?.trim() || undefined,
      departmentCodes: []
    }).subscribe({
      next: (patient) => {
        this.patients = [...this.patients, patient];
        this.patientForm.firstName = '';
        this.patientForm.lastName = '';
        this.patientForm.email = '';
        this.patientForm.phone = '';
        this.closePatientModal();
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = 'Impossibile creare il paziente.';
        this.isLoading = false;
      }
    });
  }

  submitEditDoctor(): void {
    if (!this.editDoctorTarget) {
      return;
    }
    const email = this.editDoctorForm.email.trim();
    const phone = this.editDoctorForm.phone.trim();
    if (!email && !phone) {
      this.directoryError = 'Inserisci almeno un contatto tra email o telefono.';
      return;
    }
    this.isLoading = true;
    this.directoryError = '';
    this.api
      .request<DoctorItem>('PATCH', `/api/admin/doctors/${this.editDoctorTarget.id}`, {
        email,
        phone
      })
      .subscribe({
        next: (doctor) => {
          this.doctors = this.doctors.map((item) =>
            item.id === this.editDoctorTarget?.id
              ? {
                  ...item,
                  ...doctor,
                  email: doctor.email ?? email,
                  phone: doctor.phone ?? phone
                }
              : item
          );
          this.closeEditDoctorModal();
          this.isLoading = false;
        },
        error: () => {
          this.directoryError = 'Impossibile aggiornare i contatti del medico.';
          this.isLoading = false;
        }
      });
  }

  submitEditPatient(): void {
    if (!this.editPatientTarget) {
      return;
    }
    const email = this.editPatientForm.email.trim();
    const phone = this.editPatientForm.phone.trim();
    if (!email && !phone) {
      this.directoryError = 'Inserisci almeno un contatto tra email o telefono.';
      return;
    }
    this.isLoading = true;
    this.directoryError = '';
    this.api
      .request<PatientItem>('PATCH', `/api/admin/patients/${this.editPatientTarget.id}`, {
        email,
        phone
      })
      .subscribe({
        next: (patient) => {
          this.patients = this.patients.map((item) =>
            item.id === this.editPatientTarget?.id
              ? {
                  ...item,
                  ...patient,
                  email: patient.email ?? email,
                  phone: patient.phone ?? phone
                }
              : item
          );
          this.closeEditPatientModal();
          this.isLoading = false;
        },
        error: () => {
          this.directoryError = 'Impossibile aggiornare i contatti del paziente.';
          this.isLoading = false;
        }
      });
  }

  disableDoctorAccess(doctor: DoctorItem): void {
    this.isLoading = true;
    this.directoryError = '';
    this.api.request<void>('PATCH', `/api/admin/doctors/${doctor.id}/disable`).subscribe({
      next: () => {
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = "Impossibile disabilitare l'accesso del medico.";
        this.isLoading = false;
      }
    });
  }

  disablePatientAccess(patient: PatientItem): void {
    this.isLoading = true;
    this.directoryError = '';
    this.api.request<void>('PATCH', `/api/admin/patients/${patient.id}/disable`).subscribe({
      next: () => {
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = "Impossibile disabilitare l'accesso del paziente.";
        this.isLoading = false;
      }
    });
  }

  // Gestione Strutture (Facilities)
  openFacilityModal(): void {
    this.directoryError = '';
    this.facilityForm.code = '';
    this.facilityForm.name = '';
    this.showFacilityModal = true;
  }

  closeFacilityModal(): void {
    this.showFacilityModal = false;
    this.directoryError = '';
  }

  submitFacility(): void {
    if (!this.facilityForm.code.trim() || !this.facilityForm.name.trim()) {
      this.directoryError = 'Inserisci codice e nome della struttura.';
      return;
    }
    this.isLoading = true;
    this.directoryError = '';
    this.api.request<FacilityItem>('POST', '/api/admin/facilities', {
      code: this.facilityForm.code.trim().toUpperCase(),
      name: this.facilityForm.name.trim()
    }).subscribe({
      next: (facility) => {
        this.facilities = [...this.facilities, facility];
        this.facilityForm.code = '';
        this.facilityForm.name = '';
        this.closeFacilityModal();
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = 'Impossibile creare la struttura.';
        this.isLoading = false;
      }
    });
  }

  // Gestione Reparti (Departments)
  openDepartmentModal(): void {
    this.directoryError = '';
    this.departmentForm.code = '';
    this.departmentForm.name = '';
    if (!this.departmentForm.facilityCode && this.facilities.length) {
      this.departmentForm.facilityCode = this.facilities[0].code;
    }
    this.showDepartmentModal = true;
  }

  closeDepartmentModal(): void {
    this.showDepartmentModal = false;
    this.directoryError = '';
  }

  submitDepartment(): void {
    if (!this.departmentForm.code.trim() || !this.departmentForm.name.trim() || !this.departmentForm.facilityCode) {
      this.directoryError = 'Inserisci codice, nome e struttura del reparto.';
      return;
    }
    this.isLoading = true;
    this.directoryError = '';
    this.api.request<DepartmentItem>('POST', '/api/admin/departments', {
      code: this.departmentForm.code.trim().toUpperCase(),
      name: this.departmentForm.name.trim(),
      facilityCode: this.departmentForm.facilityCode
    }).subscribe({
      next: (department) => {
        this.departments = [...this.departments, department];
        this.departmentForm.code = '';
        this.departmentForm.name = '';
        this.departmentForm.facilityCode = this.facilities[0]?.code ?? '';
        this.closeDepartmentModal();
        this.isLoading = false;
      },
      error: () => {
        this.directoryError = 'Impossibile creare il reparto.';
        this.isLoading = false;
      }
    });
  }

  getFacilityLabel(code: string): string {
    const facility = this.facilities.find((item) => item.code === code);
    return facility?.name ?? code;
  }

  loadAudit(): void {
    this.isLoading = true;
    this.auditError = '';
    this.api.request<PagedResponse<AuditEventResponse>>('GET', '/api/audit/events').subscribe({
      next: (response) => {
        const events = response.content ?? [];
        this.auditEvents = events.map((event) => ({
          id: event.id,
          action: event.action,
          actor: `${event.actorType} ${event.actorId}`.trim(),
          timestamp: event.occurredAt
        }));
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

  private getConsentsPath(): string | null {
    if (this.isPatient) {
      return '/api/consents/me';
    }
    return null;
  }
}

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
  // Campi estesi per scenario admin
  appointmentId?: number;
  appointmentDate?: string;
  patientName?: string;
  patientEmail?: string;
  notificationAttempts?: NotificationAttempt[];
  failureReason?: string;
  createdAt?: string;
}

interface NotificationAttempt {
  id: number;
  channel: 'EMAIL' | 'SMS' | 'APP';
  sentAt: string;
  status: 'SENT' | 'DELIVERED' | 'FAILED';
  type: 'REMINDER' | 'OVERDUE' | 'ALTERNATIVE';
}

interface PaymentStats {
  totalPayments: number;
  completedWithin7Days: number;
  completedWithReminder: number;
  stillPending: number;
  percentWithin7Days: number;
  percentWithReminder: number;
  percentPending: number;
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

// Interfacce estese per Audit & Compliance dettagliato
interface AuditEventDetail {
  id: number;
  eventType: 'CONSENT_GRANTED' | 'CONSENT_REVOKED' | 'DOCUMENT_ACCESS' | 'PRESCRIPTION_VIEW' | 'APPOINTMENT_BOOKED' | 'APPOINTMENT_CANCELLED' | 'PROFILE_UPDATE';
  action: string;
  actorType: 'DOCTOR' | 'PATIENT' | 'ADMIN' | 'SYSTEM';
  actorId: string;
  actorName: string;
  actorRole?: string;
  subjectType: 'PATIENT' | 'DOCTOR' | 'ADMIN' | 'DOCUMENT' | 'PRESCRIPTION' | 'APPOINTMENT' | 'CONSENT';
  subjectId: string;
  subjectPatientId?: string;
  subjectPatientName?: string;
  subjectPatientFiscalCode?: string;
  resourceType?: string;
  resourceId?: string;
  resourceName?: string;
  consentScope?: 'DOCS' | 'PRESCRIPTIONS' | 'APPOINTMENTS' | 'PROFILE' | 'ALL';
  consentValidAtAccess?: boolean;
  serviceName: string;
  ipAddress?: string;
  outcome: 'SUCCESS' | 'DENIED' | 'ERROR';
  outcomeReason?: string;
  occurredAt: string;
  integrityHash: string;
}

interface ConsentSnapshot {
  id: number;
  patientId: string;
  patientFiscalCode: string;
  doctorId: string;
  doctorName: string;
  scope: 'DOCS' | 'PRESCRIPTIONS' | 'APPOINTMENTS' | 'PROFILE' | 'ALL';
  status: 'ACTIVE' | 'REVOKED' | 'EXPIRED';
  grantedAt: string;
  revokedAt?: string;
  expiresAt?: string;
}

interface AuditSearchCriteria {
  subjectType: 'PATIENT' | 'DOCTOR' | 'ADMIN';
  subjectIdentifier: string;
  fiscalCode: string; // legacy, per compatibilità
  dateFrom: string;
  dateTo: string;
  eventTypes: string[];
  actorTypes: string[];
  outcomes: string[];
}

interface AuditSubjectFound {
  id: string;
  name: string;
  identifier: string;
  identifierLabel: string;
  role?: string;
  type: 'PATIENT' | 'DOCTOR' | 'ADMIN';
}

interface AuditReportSummary {
  totalEvents: number;
  documentAccesses: number;
  consentChanges: number;
  prescriptionViews: number;
  appointmentEvents: number;
  deniedAccesses: number;
  uniqueActors: number;
  dateRange: { from: string; to: string };
  patientInfo: { id: string; name: string; fiscalCode: string };
  generatedAt: string;
  integrityHash: string;
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
    | 'admin-televisit'
    | 'admin-admissions' = 'api';
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

  // Audit & Compliance - Stato esteso
  auditSearchCriteria: AuditSearchCriteria = {
    subjectType: 'PATIENT',
    subjectIdentifier: '',
    fiscalCode: '',
    dateFrom: '',
    dateTo: '',
    eventTypes: [],
    actorTypes: [],
    outcomes: []
  };
  auditDetailedEvents: AuditEventDetail[] = [];
  auditFilteredEvents: AuditEventDetail[] = [];
  auditConsents: ConsentSnapshot[] = [];
  auditReportSummary: AuditReportSummary | null = null;
  auditSelectedEvent: AuditEventDetail | null = null;
  auditSearchPerformed = false;
  auditExporting = false;
  auditPatientFound: { id: string; name: string; fiscalCode: string } | null = null;
  auditSubjectFound: AuditSubjectFound | null = null;

  // Tipi di evento disponibili per il filtro
  auditEventTypeOptions = [
    { value: 'CONSENT_GRANTED', label: 'Consenso concesso' },
    { value: 'CONSENT_REVOKED', label: 'Consenso revocato' },
    { value: 'DOCUMENT_ACCESS', label: 'Accesso documento' },
    { value: 'PRESCRIPTION_VIEW', label: 'Visualizzazione prescrizione' },
    { value: 'APPOINTMENT_BOOKED', label: 'Appuntamento prenotato' },
    { value: 'APPOINTMENT_CANCELLED', label: 'Appuntamento cancellato' },
    { value: 'PROFILE_UPDATE', label: 'Aggiornamento profilo' }
  ];

  auditActorTypeOptions = [
    { value: 'DOCTOR', label: 'Medico' },
    { value: 'PATIENT', label: 'Paziente' },
    { value: 'ADMIN', label: 'Amministratore' },
    { value: 'SYSTEM', label: 'Sistema' }
  ];

  auditOutcomeOptions = [
    { value: 'SUCCESS', label: 'Successo' },
    { value: 'DENIED', label: 'Negato' },
    { value: 'ERROR', label: 'Errore' }
  ];

  showPaymentQuestionModal = false;
  selectedPayment: PaymentItem | null = null;
  paymentQuestion = '';

  // Admin Payments - Filtri e statistiche
  paymentStatusFilter: 'ALL' | 'PENDING' | 'PAID' | 'CONFIRMED' | 'FAILED' = 'ALL';
  paymentDaysFilter: number | null = null; // null = tutti, 7 = >7 giorni, 14 = >14 giorni
  selectedPaymentIds: Set<number> = new Set();
  showBulkReminderModal = false;
  showAlternativePaymentModal = false;
  alternativePaymentTarget: PaymentItem | null = null;
  alternativePaymentForm = {
    method: 'BONIFICO' as 'BONIFICO' | 'SEDE',
    notes: ''
  };
  paymentStats: PaymentStats = {
    totalPayments: 0,
    completedWithin7Days: 0,
    completedWithReminder: 0,
    stillPending: 0,
    percentWithin7Days: 94,
    percentWithReminder: 4,
    percentPending: 2
  };

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
        | 'admin-televisit'
        | 'admin-admissions') ??
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
        this.loadPatients();
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
    if (this.mode === 'admin-admissions') {
      this.loadAdminAdmissions();
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
        const normalized = this.normalizeDoctorList(doctors);
        this.doctors = normalized.length > 0 ? normalized : this.getMockDoctors();
      },
      error: () => {
        if (this.isAdmin) {
          this.doctors = this.getMockDoctors();
        } else {
          this.directoryError = 'Impossibile caricare i medici.';
        }
      }
    });
  }

  private getMockDoctors(): DoctorItem[] {
    return [
      { id: 1, firstName: 'Marco', lastName: 'Belli', departmentCode: 'CARD', facilityCode: 'HQ', email: 'marco.belli@sanitech.it', phone: '+39 02 1234567' },
      { id: 2, firstName: 'Giovanni', lastName: 'Martini', departmentCode: 'PNEUMO', facilityCode: 'HQ', email: 'giovanni.martini@sanitech.it', phone: '+39 02 1234568' },
      { id: 3, firstName: 'Laura', lastName: 'Bianchi', departmentCode: 'PNEUMO', facilityCode: 'HQ', email: 'laura.bianchi@sanitech.it', phone: '+39 02 1234569' },
      { id: 4, firstName: 'Roberto', lastName: 'Esposito', departmentCode: 'NEURO', facilityCode: 'HQ', email: 'roberto.esposito@sanitech.it', phone: '+39 02 1234570' },
      { id: 5, firstName: 'Francesca', lastName: 'Russo', departmentCode: 'ORTO', facilityCode: 'HQ', email: 'francesca.russo@sanitech.it', phone: '+39 02 1234571' }
    ];
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

  getTodayAt(hours: number, minutes: number): string {
    const today = new Date();
    today.setHours(hours, minutes, 0, 0);
    return today.toISOString();
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

    // Per demo/screenshot: carica sempre i mock per admin
    if (this.isAdmin) {
      this.payments = this.generateMockPaymentsForAdmin();
      this.isLoading = false;
      return;
    }

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

  private enrichPaymentsForAdmin(): void {
    const mockPatients = [
      { id: 1, name: 'Anna Conti', email: 'anna.conti@email.it' },
      { id: 2, name: 'Marco Rossi', email: 'marco.rossi@email.it' },
      { id: 3, name: 'Giulia Bianchi', email: 'giulia.bianchi@email.it' },
      { id: 4, name: 'Luca Verdi', email: 'luca.verdi@email.it' },
      { id: 5, name: 'Sara Neri', email: 'sara.neri@email.it' }
    ];

    this.payments = this.payments.map((p, i) => {
      const patient = mockPatients[p.patientId - 1] || mockPatients[i % mockPatients.length];
      const daysAgo = Math.floor(Math.random() * 30) + 1;
      const appointmentDate = new Date();
      appointmentDate.setDate(appointmentDate.getDate() - daysAgo);

      return {
        ...p,
        patientName: patient.name,
        patientEmail: patient.email,
        appointmentId: 1000 + p.id,
        appointmentDate: appointmentDate.toISOString(),
        notificationAttempts: p.status === 'PENDING' && daysAgo > 7 ? [
          {
            id: 1,
            channel: 'EMAIL' as const,
            sentAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
            status: 'DELIVERED' as const,
            type: 'REMINDER' as const
          }
        ] : [],
        failureReason: p.status === 'FAILED' ? 'Carta rifiutata dalla banca' : undefined
      };
    });
  }

  private generateMockPaymentsForAdmin(): PaymentItem[] {
    const now = new Date();
    const mockData: PaymentItem[] = [];

    const patients = [
      { id: 1, name: 'Anna Conti', email: 'anna.conti@email.it' },
      { id: 2, name: 'Marco Rossi', email: 'marco.rossi@email.it' },
      { id: 3, name: 'Giulia Bianchi', email: 'giulia.bianchi@email.it' },
      { id: 4, name: 'Luca Verdi', email: 'luca.verdi@email.it' },
      { id: 5, name: 'Sara Neri', email: 'sara.neri@email.it' },
      { id: 6, name: 'Paolo Ferrari', email: 'paolo.ferrari@email.it' },
      { id: 7, name: 'Elena Gallo', email: 'elena.gallo@email.it' },
      { id: 8, name: 'Francesco Marino', email: 'francesco.marino@email.it' },
      { id: 9, name: 'Chiara Romano', email: 'chiara.romano@email.it' },
      { id: 10, name: 'Alessandro Greco', email: 'alessandro.greco@email.it' }
    ];

    const services = [
      'Visita cardiologica',
      'Ecografia addominale',
      'Visita ortopedica',
      'Analisi del sangue',
      'Visita dermatologica',
      'Radiografia toracica',
      'Visita oculistica',
      'Elettrocardiogramma',
      'Visita neurologica',
      'TAC cranio'
    ];

    // Genera 23 pagamenti PENDING (come da scenario)
    for (let i = 1; i <= 23; i++) {
      const patient = patients[(i - 1) % patients.length];
      const daysAgo = Math.floor(Math.random() * 25) + 8; // tra 8 e 32 giorni fa
      const appointmentDate = new Date(now);
      appointmentDate.setDate(appointmentDate.getDate() - daysAgo);

      const hasReminder = daysAgo > 14;
      const notificationAttempts: NotificationAttempt[] = [];

      if (hasReminder) {
        notificationAttempts.push({
          id: i * 100,
          channel: 'EMAIL',
          sentAt: new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000).toISOString(),
          status: 'DELIVERED',
          type: 'REMINDER'
        });
      }

      mockData.push({
        id: i,
        patientId: patient.id,
        patientName: patient.name,
        patientEmail: patient.email,
        amount: Math.floor(Math.random() * 150) + 20,
        currency: 'EUR',
        service: services[(i - 1) % services.length],
        status: 'PENDING',
        paidAt: '',
        appointmentId: 1000 + i,
        appointmentDate: appointmentDate.toISOString(),
        createdAt: appointmentDate.toISOString(),
        notificationAttempts
      });
    }

    // Aggiungi 3 pagamenti FAILED
    for (let i = 24; i <= 26; i++) {
      const patient = patients[(i - 1) % patients.length];
      const daysAgo = Math.floor(Math.random() * 10) + 5;
      const appointmentDate = new Date(now);
      appointmentDate.setDate(appointmentDate.getDate() - daysAgo);

      mockData.push({
        id: i,
        patientId: patient.id,
        patientName: patient.name,
        patientEmail: patient.email,
        amount: Math.floor(Math.random() * 200) + 50,
        currency: 'EUR',
        service: services[(i - 1) % services.length],
        status: 'FAILED',
        paidAt: '',
        appointmentId: 1000 + i,
        appointmentDate: appointmentDate.toISOString(),
        createdAt: appointmentDate.toISOString(),
        failureReason: 'Carta rifiutata dalla banca',
        notificationAttempts: [{
          id: i * 100,
          channel: 'EMAIL',
          sentAt: new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString(),
          status: 'DELIVERED',
          type: 'REMINDER'
        }]
      });
    }

    // Aggiungi alcuni pagamenti CONFIRMED per statistiche
    for (let i = 27; i <= 35; i++) {
      const patient = patients[(i - 1) % patients.length];
      const daysAgo = Math.floor(Math.random() * 20) + 1;
      const appointmentDate = new Date(now);
      appointmentDate.setDate(appointmentDate.getDate() - daysAgo);
      const paidDate = new Date(appointmentDate);
      paidDate.setDate(paidDate.getDate() + Math.floor(Math.random() * 5) + 1);

      mockData.push({
        id: i,
        patientId: patient.id,
        patientName: patient.name,
        patientEmail: patient.email,
        amount: Math.floor(Math.random() * 180) + 30,
        currency: 'EUR',
        service: services[(i - 1) % services.length],
        status: 'CONFIRMED',
        paidAt: paidDate.toISOString(),
        appointmentId: 1000 + i,
        appointmentDate: appointmentDate.toISOString(),
        createdAt: appointmentDate.toISOString(),
        notificationAttempts: []
      });
    }

    return mockData;
  }

  submitPayment(): void {
    if (!this.paymentForm.paymentId) {
      this.paymentsError = 'Seleziona un pagamento in attesa.';
      return;
    }
    if (!this.paymentForm.amount || this.paymentForm.amount <= 0) {
      this.paymentsError = "Inserisci l'importo del pagamento.";
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
    if (this.isAdmin) {
      this.notifications = this.getMockAdminNotifications();
      this.isLoading = false;
      return;
    }
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

  private getMockAdminNotifications(): NotificationItem[] {
    const baseDate = new Date();
    baseDate.setHours(19, 0, 0, 0);
    return [
      {
        id: 1001,
        recipient: 'mario.rossi@email.it',
        channel: 'EMAIL',
        subject: 'Cambio appuntamento - Conversione in televisita',
        message: 'Il suo appuntamento del 03/02 con Dr. Martini è stato convertito in televisita. Link: https://tv.sanitech.it/s/abc123',
        notes: 'Emergenza sostituzione Dr. Martini',
        status: 'SENT',
        sentAt: new Date(baseDate.getTime() - 5 * 60000).toISOString()
      },
      {
        id: 1002,
        recipient: 'giulia.bianchi@email.it',
        channel: 'EMAIL',
        subject: 'Cambio appuntamento - Riassegnazione medico',
        message: 'Il suo appuntamento per spirometria del 04/02 è stato riassegnato alla Dott.ssa Bianchi. Stesso orario confermato.',
        notes: 'Riassegnazione per indisponibilità Dr. Martini',
        status: 'SENT',
        sentAt: new Date(baseDate.getTime() - 12 * 60000).toISOString()
      },
      {
        id: 1003,
        recipient: 'luca.verdi@email.it',
        channel: 'EMAIL',
        subject: 'Cambio appuntamento - Conversione in televisita',
        message: 'Il suo follow-up del 03/02 con Dr. Martini è stato convertito in televisita. Istruzioni di collegamento inviate.',
        notes: 'Follow-up pneumologico',
        status: 'SENT',
        sentAt: new Date(baseDate.getTime() - 18 * 60000).toISOString()
      },
      {
        id: 1004,
        recipient: 'anna.conti@email.it',
        channel: 'EMAIL',
        subject: 'Cambio medico referente ricovero',
        message: 'Il suo medico referente per il ricovero in Pneumologia è ora la Dott.ssa Bianchi. Continuità assistenziale garantita.',
        notes: 'Aggiornamento ricovero attivo',
        status: 'SENT',
        sentAt: new Date(baseDate.getTime() - 25 * 60000).toISOString()
      },
      {
        id: 1005,
        recipient: 'paolo.neri@email.it',
        channel: 'EMAIL',
        subject: 'Cambio appuntamento - Riassegnazione medico',
        message: 'La sua auscultazione del 05/02 è stata riassegnata alla Dott.ssa Bianchi per indisponibilità del Dr. Martini.',
        notes: 'Visita che richiede presenza fisica',
        status: 'SENT',
        sentAt: new Date(baseDate.getTime() - 30 * 60000).toISOString()
      },
      {
        id: 1006,
        recipient: 'francesca.romano@email.it',
        channel: 'EMAIL',
        subject: 'Cambio medico referente ricovero',
        message: 'La Dott.ssa Bianchi è ora la sua nuova referente per il ricovero in corso. Il team infermieristico è stato informato.',
        notes: 'Paziente ricoverato - cambio referente',
        status: 'SENT',
        sentAt: new Date(baseDate.getTime() - 35 * 60000).toISOString()
      },
      {
        id: 1007,
        recipient: 'marco.ferrari@email.it',
        channel: 'EMAIL',
        subject: 'Cambio appuntamento - Conversione in televisita',
        message: 'La consulenza del 04/02 è stata convertita in televisita. Riceverà il link 30 minuti prima.',
        notes: 'Consulenza non urgente',
        status: 'SENT',
        sentAt: new Date(baseDate.getTime() - 40 * 60000).toISOString()
      },
      {
        id: 1008,
        recipient: 'elena.costa@email.it',
        channel: 'EMAIL',
        subject: 'Cambio medico referente ricovero',
        message: 'Aggiornamento sul suo ricovero: la Dott.ssa Bianchi ha preso in carico la sua cartella clinica.',
        notes: 'Terzo paziente ricoverato',
        status: 'SENT',
        sentAt: new Date(baseDate.getTime() - 45 * 60000).toISOString()
      }
    ];
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

  confirmAdminPayment(payment: PaymentItem): void {
    if (payment.status === 'CONFIRMED') return;
    this.payments = this.payments.map((p) =>
      p.id === payment.id ? { ...p, status: 'CONFIRMED', paidAt: new Date().toISOString() } : p
    );
    this.paymentsSuccess = `Pagamento #${payment.id} confermato con successo.`;
  }

  sendSingleReminder(payment: PaymentItem): void {
    if (payment.status === 'CONFIRMED') return;
    const newAttempt: NotificationAttempt = {
      id: Date.now(),
      channel: 'EMAIL',
      sentAt: new Date().toISOString(),
      status: 'SENT',
      type: payment.status === 'FAILED' ? 'ALTERNATIVE' : 'OVERDUE'
    };
    this.payments = this.payments.map((p) =>
      p.id === payment.id
        ? { ...p, notificationAttempts: [...(p.notificationAttempts || []), newAttempt] }
        : p
    );
    this.paymentsSuccess = `Sollecito inviato a ${payment.patientName || 'paziente'}.`;
  }

  deleteAdminPayment(payment: PaymentItem): void {
    this.payments = this.payments.filter((p) => p.id !== payment.id);
    this.selectedPaymentIds.delete(payment.id);
    this.paymentsSuccess = `Pagamento #${payment.id} eliminato.`;
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

  // Admin Payments - Filtri e gestione
  get filteredPayments(): PaymentItem[] {
    let result = this.payments;

    // Filtro per stato
    if (this.paymentStatusFilter !== 'ALL') {
      result = result.filter((p) => p.status === this.paymentStatusFilter);
    }

    // Filtro per giorni di ritardo
    if (this.paymentDaysFilter) {
      const now = new Date();
      result = result.filter((p) => {
        if (!p.appointmentDate) return false;
        const appointmentDate = new Date(p.appointmentDate);
        const diffDays = Math.floor((now.getTime() - appointmentDate.getTime()) / (1000 * 60 * 60 * 24));
        return diffDays > this.paymentDaysFilter!;
      });
    }

    return result;
  }

  getPaymentDaysOverdue(payment: PaymentItem): number {
    if (!payment.appointmentDate) return 0;
    const now = new Date();
    const appointmentDate = new Date(payment.appointmentDate);
    return Math.max(0, Math.floor((now.getTime() - appointmentDate.getTime()) / (1000 * 60 * 60 * 24)));
  }

  togglePaymentSelection(paymentId: number): void {
    if (this.selectedPaymentIds.has(paymentId)) {
      this.selectedPaymentIds.delete(paymentId);
    } else {
      this.selectedPaymentIds.add(paymentId);
    }
  }

  toggleAllPaymentsSelection(): void {
    const pendingPayments = this.filteredPayments.filter((p) => p.status === 'PENDING');
    if (this.selectedPaymentIds.size === pendingPayments.length) {
      this.selectedPaymentIds.clear();
    } else {
      pendingPayments.forEach((p) => this.selectedPaymentIds.add(p.id));
    }
  }

  isPaymentSelected(paymentId: number): boolean {
    return this.selectedPaymentIds.has(paymentId);
  }

  get selectedPaymentsCount(): number {
    return this.selectedPaymentIds.size;
  }

  openBulkReminderModal(): void {
    if (this.selectedPaymentIds.size === 0) {
      this.paymentsError = 'Seleziona almeno un pagamento per inviare i solleciti.';
      return;
    }
    this.paymentsError = '';
    this.showBulkReminderModal = true;
  }

  closeBulkReminderModal(): void {
    this.showBulkReminderModal = false;
  }

  sendBulkReminders(): void {
    const count = this.selectedPaymentIds.size;
    // Simula l'invio aggiungendo un tentativo di notifica
    this.payments = this.payments.map((p) => {
      if (this.selectedPaymentIds.has(p.id)) {
        const newAttempt: NotificationAttempt = {
          id: Date.now(),
          channel: 'EMAIL',
          sentAt: new Date().toISOString(),
          status: 'SENT',
          type: 'OVERDUE'
        };
        return {
          ...p,
          notificationAttempts: [...(p.notificationAttempts || []), newAttempt]
        };
      }
      return p;
    });
    this.paymentsSuccess = `Solleciti inviati con successo a ${count} pazienti.`;
    this.selectedPaymentIds.clear();
    this.closeBulkReminderModal();
  }

  openAlternativePaymentModal(payment: PaymentItem): void {
    this.alternativePaymentTarget = payment;
    this.alternativePaymentForm = { method: 'BONIFICO', notes: '' };
    this.paymentsError = '';
    this.showAlternativePaymentModal = true;
  }

  closeAlternativePaymentModal(): void {
    this.showAlternativePaymentModal = false;
    this.alternativePaymentTarget = null;
  }

  sendAlternativePaymentNotification(): void {
    if (!this.alternativePaymentTarget) return;

    const newAttempt: NotificationAttempt = {
      id: Date.now(),
      channel: 'EMAIL',
      sentAt: new Date().toISOString(),
      status: 'SENT',
      type: 'ALTERNATIVE'
    };

    this.payments = this.payments.map((p) =>
      p.id === this.alternativePaymentTarget!.id
        ? { ...p, notificationAttempts: [...(p.notificationAttempts || []), newAttempt] }
        : p
    );

    const methodLabel = this.alternativePaymentForm.method === 'BONIFICO' ? 'bonifico bancario' : 'pagamento in sede';
    this.paymentsSuccess = `Sollecito con istruzioni per ${methodLabel} inviato al paziente.`;
    this.closeAlternativePaymentModal();
  }

  getNotificationAttemptsCount(payment: PaymentItem): number {
    return payment.notificationAttempts?.length || 0;
  }

  getLastNotificationDate(payment: PaymentItem): string {
    if (!payment.notificationAttempts?.length) return '-';
    const lastAttempt = payment.notificationAttempts[payment.notificationAttempts.length - 1];
    return this.formatDateTime(lastAttempt.sentAt);
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
        this.patients = normalizedPatients.length > 0 ? normalizedPatients : this.getMockPatients();
        if (!this.prescriptionForm.patientId && this.patients.length) {
          this.prescriptionForm.patientId = this.patients[0].id;
        }
        if (!this.docForm.patientId && this.patients.length) {
          this.docForm.patientId = this.patients[0].id;
        }
        if (!this.televisitForm.patientId && this.patients.length) {
          this.televisitForm.patientId = this.patients[0].id;
        }
      },
      error: () => {
        if (this.isAdmin) {
          this.patients = this.getMockPatients();
        } else {
          this.directoryError = 'Impossibile caricare i pazienti.';
        }
      }
    });
  }

  private getMockPatients(): PatientItem[] {
    return [
      { id: 1, firstName: 'Mario', lastName: 'Rossi', email: 'mario.rossi@email.it', phone: '+39 333 1234567' },
      { id: 2, firstName: 'Giulia', lastName: 'Bianchi', email: 'giulia.bianchi@email.it', phone: '+39 333 2345678' },
      { id: 3, firstName: 'Luca', lastName: 'Verdi', email: 'luca.verdi@email.it', phone: '+39 333 3456789' },
      { id: 4, firstName: 'Anna', lastName: 'Conti', email: 'anna.conti@email.it', phone: '+39 333 4567890' },
      { id: 5, firstName: 'Paolo', lastName: 'Neri', email: 'paolo.neri@email.it', phone: '+39 333 5678901' },
      { id: 6, firstName: 'Francesca', lastName: 'Romano', email: 'francesca.romano@email.it', phone: '+39 333 6789012' },
      { id: 7, firstName: 'Marco', lastName: 'Ferrari', email: 'marco.ferrari@email.it', phone: '+39 333 7890123' },
      { id: 8, firstName: 'Elena', lastName: 'Costa', email: 'elena.costa@email.it', phone: '+39 333 8901234' }
    ];
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
    this.auditSearchPerformed = false;
    this.auditDetailedEvents = [];
    this.auditFilteredEvents = [];
    this.auditConsents = [];
    this.auditReportSummary = null;
    this.auditSelectedEvent = null;
    this.auditPatientFound = null;
    this.auditSubjectFound = null;
    this.auditError = '';
    const today = new Date();
    const ninetyDaysAgo = new Date(today.getTime() - 90 * 24 * 60 * 60 * 1000);
    this.auditSearchCriteria.dateTo = today.toISOString().split('T')[0];
    this.auditSearchCriteria.dateFrom = ninetyDaysAgo.toISOString().split('T')[0];
    this.auditSearchCriteria.subjectType = 'PATIENT';
    this.auditSearchCriteria.subjectIdentifier = '';
  }

  get auditSubjectLabel(): string {
    switch (this.auditSearchCriteria.subjectType) {
      case 'PATIENT': return 'Codice Fiscale Paziente';
      case 'DOCTOR': return 'Codice Fiscale Medico';
      case 'ADMIN': return 'Codice Fiscale Amministratore';
      default: return 'Identificativo';
    }
  }

  get auditSubjectPlaceholder(): string {
    switch (this.auditSearchCriteria.subjectType) {
      case 'PATIENT': return 'Es: SPSGPP65M15H501X';
      case 'DOCTOR': return 'Es: BNCMRC75A01H501Z';
      case 'ADMIN': return 'Es: RSSMRA80B15H501Y';
      default: return 'Inserisci identificativo';
    }
  }

  get auditSubjectFoundTitle(): string {
    if (!this.auditSubjectFound) return 'Soggetto Identificato';
    switch (this.auditSubjectFound.type) {
      case 'PATIENT': return 'Paziente Identificato';
      case 'DOCTOR': return 'Medico Identificato';
      case 'ADMIN': return 'Amministratore Identificato';
      default: return 'Soggetto Identificato';
    }
  }

  searchAuditEvents(): void {
    if (!this.auditSearchCriteria.subjectIdentifier.trim()) {
      this.auditError = `Inserisci l'identificativo per avviare la ricerca.`;
      return;
    }
    this.isLoading = true;
    this.auditError = '';
    this.auditSearchPerformed = false;
    setTimeout(() => {
      const identifier = this.auditSearchCriteria.subjectIdentifier.toUpperCase().trim();
      const subjectType = this.auditSearchCriteria.subjectType;

      if (identifier.length === 16) {
        this.auditSubjectFound = this.createMockSubject(subjectType, identifier);
        this.auditDetailedEvents = this.generateMockAuditEventsForSubject(subjectType, identifier);
        this.auditConsents = subjectType === 'PATIENT' ? this.generateMockConsents(identifier) : [];
        this.applyAuditFilters();
        this.generateAuditSummary();
        this.auditSearchPerformed = true;
      } else {
        this.auditError = `Nessun ${this.getSubjectTypeLabel(subjectType).toLowerCase()} trovato con l'identificativo specificato.`;
        this.auditSubjectFound = null;
      }
      this.isLoading = false;
    }, 800);
  }

  private getSubjectTypeLabel(type: string): string {
    switch (type) {
      case 'PATIENT': return 'Paziente';
      case 'DOCTOR': return 'Medico';
      case 'ADMIN': return 'Amministratore';
      default: return 'Soggetto';
    }
  }

  private createMockSubject(type: 'PATIENT' | 'DOCTOR' | 'ADMIN', identifier: string): AuditSubjectFound {
    switch (type) {
      case 'PATIENT':
        return { id: 'PAT-2847', name: identifier === 'SPSGPP65M15H501X' ? 'Giuseppe Esposito' : 'Paziente Demo', identifier, identifierLabel: 'Codice Fiscale', type };
      case 'DOCTOR':
        return { id: 'DOC-112', name: identifier === 'BNCMRC75A01H501Z' ? 'Dr. Marco Bianchi' : 'Medico Demo', identifier, identifierLabel: 'Codice Fiscale', role: 'Medico di base', type };
      case 'ADMIN':
        return { id: 'ADM-001', name: identifier === 'RSSMRA80B15H501Y' ? 'Mario Rossi' : 'Admin Demo', identifier, identifierLabel: 'Codice Fiscale', role: 'Amministratore Sistema', type };
      default:
        return { id: 'UNK-000', name: 'Sconosciuto', identifier, identifierLabel: 'Identificativo', type };
    }
  }

  private generateMockAuditEventsForSubject(subjectType: 'PATIENT' | 'DOCTOR' | 'ADMIN', identifier: string): AuditEventDetail[] {
    switch (subjectType) {
      case 'PATIENT': return this.generateMockAuditEvents(identifier);
      case 'DOCTOR': return this.generateMockDoctorAuditEvents(identifier);
      case 'ADMIN': return this.generateMockAdminAuditEvents(identifier);
      default: return [];
    }
  }

  private generateMockDoctorAuditEvents(fiscalCode: string): AuditEventDetail[] {
    const baseDate = new Date();
    const events: AuditEventDetail[] = [];
    let eventId = 1;
    const doctorName = fiscalCode === 'BNCMRC75A01H501Z' ? 'Dr. Marco Bianchi' : 'Medico Demo';
    const patients = [{ id: 'PAT-2847', name: 'Giuseppe Esposito' }, { id: 'PAT-1234', name: 'Maria Verdi' }, { id: 'PAT-5678', name: 'Luigi Bianchi' }];

    // Accessi a documenti di vari pazienti
    for (let i = 0; i < 35; i++) {
      const daysAgo = Math.floor(Math.random() * 89) + 1;
      const accessDate = new Date(baseDate.getTime() - daysAgo * 24 * 60 * 60 * 1000);
      const patient = patients[Math.floor(Math.random() * patients.length)];
      const docTypes = ['Referto analisi', 'ECG', 'Radiografia', 'Ecografia', 'Prescrizione'];
      const docType = docTypes[Math.floor(Math.random() * docTypes.length)];
      events.push({ id: eventId++, eventType: 'DOCUMENT_ACCESS', action: `Visualizzazione documento: ${docType}`, actorType: 'DOCTOR', actorId: 'DOC-112', actorName: doctorName, actorRole: 'Medico di base', subjectType: 'DOCUMENT', subjectId: `DOC-${5000 + i}`, subjectPatientId: patient.id, subjectPatientName: patient.name, subjectPatientFiscalCode: 'XXXXXXXXXXXXX', resourceType: 'DOCUMENT', resourceId: `DOC-${5000 + i}`, resourceName: docType, consentScope: 'DOCS', consentValidAtAccess: true, serviceName: 'document-service', ipAddress: `192.168.1.${Math.floor(Math.random() * 254) + 1}`, outcome: 'SUCCESS', occurredAt: accessDate.toISOString(), integrityHash: this.generateHash() });
    }

    // Login e operazioni amministrative
    for (let i = 0; i < 12; i++) {
      const daysAgo = Math.floor(Math.random() * 89) + 1;
      const eventDate = new Date(baseDate.getTime() - daysAgo * 24 * 60 * 60 * 1000);
      events.push({ id: eventId++, eventType: 'PROFILE_UPDATE', action: 'Accesso al portale medico', actorType: 'DOCTOR', actorId: 'DOC-112', actorName: doctorName, actorRole: 'Medico di base', subjectType: 'DOCTOR', subjectId: 'DOC-112', serviceName: 'auth-service', ipAddress: `192.168.1.${Math.floor(Math.random() * 254) + 1}`, outcome: 'SUCCESS', occurredAt: eventDate.toISOString(), integrityHash: this.generateHash() });
    }

    return events.sort((a, b) => new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime());
  }

  private generateMockAdminAuditEvents(fiscalCode: string): AuditEventDetail[] {
    const baseDate = new Date();
    const events: AuditEventDetail[] = [];
    let eventId = 1;
    const adminName = fiscalCode === 'RSSMRA80B15H501Y' ? 'Mario Rossi' : 'Admin Demo';

    // Operazioni amministrative
    const adminActions = [
      { action: 'Creazione nuovo medico', eventType: 'PROFILE_UPDATE' as const },
      { action: 'Modifica permessi utente', eventType: 'PROFILE_UPDATE' as const },
      { action: 'Visualizzazione audit trail', eventType: 'DOCUMENT_ACCESS' as const },
      { action: 'Generazione report compliance', eventType: 'DOCUMENT_ACCESS' as const },
      { action: 'Configurazione sistema notifiche', eventType: 'PROFILE_UPDATE' as const },
      { action: 'Aggiornamento struttura sanitaria', eventType: 'PROFILE_UPDATE' as const }
    ];

    for (let i = 0; i < 28; i++) {
      const daysAgo = Math.floor(Math.random() * 89) + 1;
      const eventDate = new Date(baseDate.getTime() - daysAgo * 24 * 60 * 60 * 1000);
      const adminAction = adminActions[Math.floor(Math.random() * adminActions.length)];
      events.push({ id: eventId++, eventType: adminAction.eventType, action: adminAction.action, actorType: 'ADMIN', actorId: 'ADM-001', actorName: adminName, actorRole: 'Amministratore Sistema', subjectType: 'ADMIN', subjectId: 'ADM-001', serviceName: 'admin-service', ipAddress: `10.0.0.${Math.floor(Math.random() * 254) + 1}`, outcome: 'SUCCESS', occurredAt: eventDate.toISOString(), integrityHash: this.generateHash() });
    }

    // Accessi a dati sensibili
    for (let i = 0; i < 8; i++) {
      const daysAgo = Math.floor(Math.random() * 89) + 1;
      const eventDate = new Date(baseDate.getTime() - daysAgo * 24 * 60 * 60 * 1000);
      events.push({ id: eventId++, eventType: 'DOCUMENT_ACCESS', action: 'Accesso a log di sistema', actorType: 'ADMIN', actorId: 'ADM-001', actorName: adminName, actorRole: 'Amministratore Sistema', subjectType: 'DOCUMENT', subjectId: `LOG-${1000 + i}`, resourceType: 'SYSTEM_LOG', resourceId: `LOG-${1000 + i}`, resourceName: 'Log di sistema', serviceName: 'audit-service', ipAddress: `10.0.0.${Math.floor(Math.random() * 254) + 1}`, outcome: 'SUCCESS', occurredAt: eventDate.toISOString(), integrityHash: this.generateHash() });
    }

    return events.sort((a, b) => new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime());
  }

  private generateMockAuditEvents(fiscalCode: string): AuditEventDetail[] {
    const baseDate = new Date();
    const events: AuditEventDetail[] = [];
    let eventId = 1;
    const doctors = [{ id: 'DOC-112', name: 'Dr. Marco Bianchi', role: 'Medico di base' }, { id: 'DOC-245', name: 'Dr.ssa Elena Rossi', role: 'Cardiologo' }, { id: 'DOC-378', name: 'Dr. Antonio Ferrara', role: 'Endocrinologo' }];
    const patientName = fiscalCode === 'SPSGPP65M15H501X' ? 'Giuseppe Esposito' : 'Paziente Demo';
    doctors.forEach((doctor, idx) => {
      const consentDate = new Date(baseDate.getTime() - (180 - idx * 30) * 24 * 60 * 60 * 1000);
      events.push({ id: eventId++, eventType: 'CONSENT_GRANTED', action: 'Consenso concesso per accesso documenti', actorType: 'PATIENT', actorId: 'PAT-2847', actorName: patientName, subjectType: 'CONSENT', subjectId: `CNS-${1000 + idx}`, subjectPatientId: 'PAT-2847', subjectPatientName: patientName, subjectPatientFiscalCode: fiscalCode, resourceType: 'DOCTOR', resourceId: doctor.id, resourceName: doctor.name, consentScope: 'DOCS', serviceName: 'consent-service', outcome: 'SUCCESS', occurredAt: consentDate.toISOString(), integrityHash: this.generateHash() });
    });
    const documentTypes = ['Referto analisi sangue', 'ECG completo', 'Radiografia torace', 'Ecografia addome', 'Referto visita specialistica', 'Prescrizione farmaci', 'Piano terapeutico', 'Certificato medico'];
    for (let i = 0; i < 47; i++) {
      const daysAgo = Math.floor(Math.random() * 89) + 1;
      const accessDate = new Date(baseDate.getTime() - daysAgo * 24 * 60 * 60 * 1000);
      const doctor = doctors[Math.floor(Math.random() * doctors.length)];
      const docType = documentTypes[Math.floor(Math.random() * documentTypes.length)];
      events.push({ id: eventId++, eventType: 'DOCUMENT_ACCESS', action: `Visualizzazione documento: ${docType}`, actorType: 'DOCTOR', actorId: doctor.id, actorName: doctor.name, actorRole: doctor.role, subjectType: 'DOCUMENT', subjectId: `DOC-${5000 + i}`, subjectPatientId: 'PAT-2847', subjectPatientName: patientName, subjectPatientFiscalCode: fiscalCode, resourceType: 'DOCUMENT', resourceId: `DOC-${5000 + i}`, resourceName: docType, consentScope: 'DOCS', consentValidAtAccess: true, serviceName: 'document-service', ipAddress: `192.168.1.${Math.floor(Math.random() * 254) + 1}`, outcome: 'SUCCESS', occurredAt: accessDate.toISOString(), integrityHash: this.generateHash() });
    }
    for (let i = 0; i < 8; i++) {
      const daysAgo = Math.floor(Math.random() * 89) + 1;
      const eventDate = new Date(baseDate.getTime() - daysAgo * 24 * 60 * 60 * 1000);
      const doctor = doctors[Math.floor(Math.random() * doctors.length)];
      events.push({ id: eventId++, eventType: 'PRESCRIPTION_VIEW', action: 'Visualizzazione prescrizione farmacologica', actorType: 'DOCTOR', actorId: doctor.id, actorName: doctor.name, actorRole: doctor.role, subjectType: 'PRESCRIPTION', subjectId: `PRE-${3000 + i}`, subjectPatientId: 'PAT-2847', subjectPatientName: patientName, subjectPatientFiscalCode: fiscalCode, resourceType: 'PRESCRIPTION', resourceId: `PRE-${3000 + i}`, resourceName: 'Prescrizione terapia', consentValidAtAccess: true, serviceName: 'prescription-service', outcome: 'SUCCESS', occurredAt: eventDate.toISOString(), integrityHash: this.generateHash() });
    }
    return events.sort((a, b) => new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime());
  }

  private generateMockConsents(fiscalCode: string): ConsentSnapshot[] {
    const baseDate = new Date();
    return [{ id: 1001, patientId: 'PAT-2847', patientFiscalCode: fiscalCode, doctorId: 'DOC-112', doctorName: 'Dr. Marco Bianchi', scope: 'DOCS', status: 'ACTIVE', grantedAt: new Date(baseDate.getTime() - 180 * 24 * 60 * 60 * 1000).toISOString() }, { id: 1002, patientId: 'PAT-2847', patientFiscalCode: fiscalCode, doctorId: 'DOC-245', doctorName: 'Dr.ssa Elena Rossi', scope: 'DOCS', status: 'ACTIVE', grantedAt: new Date(baseDate.getTime() - 150 * 24 * 60 * 60 * 1000).toISOString() }, { id: 1003, patientId: 'PAT-2847', patientFiscalCode: fiscalCode, doctorId: 'DOC-378', doctorName: 'Dr. Antonio Ferrara', scope: 'DOCS', status: 'ACTIVE', grantedAt: new Date(baseDate.getTime() - 120 * 24 * 60 * 60 * 1000).toISOString() }];
  }

  private generateHash(): string { const chars = 'abcdef0123456789'; let hash = ''; for (let i = 0; i < 64; i++) { hash += chars.charAt(Math.floor(Math.random() * chars.length)); } return hash; }

  applyAuditFilters(): void {
    let filtered = [...this.auditDetailedEvents];
    if (this.auditSearchCriteria.dateFrom) { const fromDate = new Date(this.auditSearchCriteria.dateFrom); filtered = filtered.filter(e => new Date(e.occurredAt) >= fromDate); }
    if (this.auditSearchCriteria.dateTo) { const toDate = new Date(this.auditSearchCriteria.dateTo); toDate.setHours(23, 59, 59, 999); filtered = filtered.filter(e => new Date(e.occurredAt) <= toDate); }
    if (this.auditSearchCriteria.eventTypes.length > 0) { filtered = filtered.filter(e => this.auditSearchCriteria.eventTypes.includes(e.eventType)); }
    if (this.auditSearchCriteria.actorTypes.length > 0) { filtered = filtered.filter(e => this.auditSearchCriteria.actorTypes.includes(e.actorType)); }
    if (this.auditSearchCriteria.outcomes.length > 0) { filtered = filtered.filter(e => this.auditSearchCriteria.outcomes.includes(e.outcome)); }
    this.auditFilteredEvents = filtered;
    this.generateAuditSummary();
  }

  private generateAuditSummary(): void {
    const events = this.auditFilteredEvents;
    const uniqueActors = new Set(events.map(e => e.actorId));
    this.auditReportSummary = { totalEvents: events.length, documentAccesses: events.filter(e => e.eventType === 'DOCUMENT_ACCESS').length, consentChanges: events.filter(e => e.eventType === 'CONSENT_GRANTED' || e.eventType === 'CONSENT_REVOKED').length, prescriptionViews: events.filter(e => e.eventType === 'PRESCRIPTION_VIEW').length, appointmentEvents: events.filter(e => e.eventType === 'APPOINTMENT_BOOKED' || e.eventType === 'APPOINTMENT_CANCELLED').length, deniedAccesses: events.filter(e => e.outcome === 'DENIED').length, uniqueActors: uniqueActors.size, dateRange: { from: this.auditSearchCriteria.dateFrom, to: this.auditSearchCriteria.dateTo }, patientInfo: this.auditPatientFound || { id: '', name: '', fiscalCode: '' }, generatedAt: new Date().toISOString(), integrityHash: this.generateHash() };
  }

  selectAuditEvent(event: AuditEventDetail): void { this.auditSelectedEvent = event; }
  closeAuditEventDetail(): void { this.auditSelectedEvent = null; }
  toggleEventTypeFilter(eventType: string): void { const idx = this.auditSearchCriteria.eventTypes.indexOf(eventType); if (idx >= 0) { this.auditSearchCriteria.eventTypes.splice(idx, 1); } else { this.auditSearchCriteria.eventTypes.push(eventType); } if (this.auditSearchPerformed) { this.applyAuditFilters(); } }
  toggleActorTypeFilter(actorType: string): void { const idx = this.auditSearchCriteria.actorTypes.indexOf(actorType); if (idx >= 0) { this.auditSearchCriteria.actorTypes.splice(idx, 1); } else { this.auditSearchCriteria.actorTypes.push(actorType); } if (this.auditSearchPerformed) { this.applyAuditFilters(); } }
  toggleOutcomeFilter(outcome: string): void { const idx = this.auditSearchCriteria.outcomes.indexOf(outcome); if (idx >= 0) { this.auditSearchCriteria.outcomes.splice(idx, 1); } else { this.auditSearchCriteria.outcomes.push(outcome); } if (this.auditSearchPerformed) { this.applyAuditFilters(); } }
  isEventTypeSelected(eventType: string): boolean { return this.auditSearchCriteria.eventTypes.includes(eventType); }
  isActorTypeSelected(actorType: string): boolean { return this.auditSearchCriteria.actorTypes.includes(actorType); }
  isOutcomeSelected(outcome: string): boolean { return this.auditSearchCriteria.outcomes.includes(outcome); }
  getEventTypeLabel(eventType: string): string { return this.auditEventTypeOptions.find(o => o.value === eventType)?.label ?? eventType; }
  getActorTypeLabel(actorType: string): string { return this.auditActorTypeOptions.find(o => o.value === actorType)?.label ?? actorType; }
  getOutcomeLabel(outcome: string): string { return this.auditOutcomeOptions.find(o => o.value === outcome)?.label ?? outcome; }
  getOutcomeBadgeClass(outcome: string): string { switch (outcome) { case 'SUCCESS': return 'bg-success'; case 'DENIED': return 'bg-danger'; case 'ERROR': return 'bg-warning text-dark'; default: return 'bg-secondary'; } }
  getEventTypeBadgeClass(eventType: string): string { switch (eventType) { case 'CONSENT_GRANTED': return 'bg-success'; case 'CONSENT_REVOKED': return 'bg-warning text-dark'; case 'DOCUMENT_ACCESS': return 'bg-primary'; case 'PRESCRIPTION_VIEW': return 'bg-info'; case 'APPOINTMENT_BOOKED': return 'bg-info'; default: return 'bg-secondary'; } }
  formatAuditDate(isoDate: string): string { return new Date(isoDate).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' }); }
  formatAuditDateShort(isoDate: string): string { return new Date(isoDate).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric' }); }
  getConsentForEvent(event: AuditEventDetail): ConsentSnapshot | null { if (!event.actorId || event.actorType !== 'DOCTOR') return null; return this.auditConsents.find(c => c.doctorId === event.actorId && c.status === 'ACTIVE') || null; }

  exportAuditReport(): void {
    if (!this.auditReportSummary) return;
    this.auditExporting = true;
    setTimeout(() => {
      const summary = this.auditReportSummary!;
      let content = `REPORT AUDIT & COMPLIANCE\n====================================\nPAZIENTE: ${summary.patientInfo.name} (${summary.patientInfo.fiscalCode})\nPERIODO: ${this.formatAuditDateShort(summary.dateRange.from)} - ${this.formatAuditDateShort(summary.dateRange.to)}\n\nRIEPILOGO:\n- Totale eventi: ${summary.totalEvents}\n- Accessi documenti: ${summary.documentAccesses}\n- Modifiche consensi: ${summary.consentChanges}\n- Accessi negati: ${summary.deniedAccesses}\n\nCONSENSI ATTIVI:\n`;
      this.auditConsents.forEach(c => { content += `- ${c.doctorName} (${c.scope}) dal ${this.formatAuditDateShort(c.grantedAt)}\n`; });
      content += `\nDETTAGLIO EVENTI:\n`;
      this.auditFilteredEvents.forEach(e => { content += `[${this.formatAuditDate(e.occurredAt)}] ${e.action} - ${e.actorName} - ${e.outcome}\n`; });
      content += `\nHash integrità: ${summary.integrityHash}\nGenerato: ${this.formatAuditDate(summary.generatedAt)}\n`;
      const blob = new Blob([content], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `audit_report_${this.auditPatientFound?.fiscalCode}_${new Date().toISOString().split('T')[0]}.txt`;
      link.click();
      window.URL.revokeObjectURL(url);
      this.auditExporting = false;
    }, 1500);
  }

  clearAuditSearch(): void { this.auditSearchCriteria = { subjectType: 'PATIENT', subjectIdentifier: '', fiscalCode: '', dateFrom: '', dateTo: '', eventTypes: [], actorTypes: [], outcomes: [] }; this.auditDetailedEvents = []; this.auditFilteredEvents = []; this.auditConsents = []; this.auditReportSummary = null; this.auditSelectedEvent = null; this.auditSearchPerformed = false; this.auditPatientFound = null; this.auditSubjectFound = null; this.auditError = ''; this.loadAudit(); }

  loadAdminTelevisit(): void {
    this.televisitError = '';
    // Carica dati mock sincronamente per garantire disponibilità immediata
    this.patients = this.getMockPatients();
    this.doctors = this.getMockDoctors();
    this.departments = this.getMockDepartments();
    this.loadMockTelevisitsForScenario();
  }

  loadAdminAdmissions(): void {
    this.paymentsError = '';
    // Carica dati mock sincronamente per garantire disponibilità immediata
    this.patients = this.getMockPatients();
    this.doctors = this.getMockDoctors();
    this.departments = this.getMockDepartments();
    this.loadMockAdmissionsForScenario();
  }

  private loadMockTelevisitsForScenario(): void {
    this.isLoading = true;
    const baseDate = new Date();
    baseDate.setHours(19, 30, 0, 0);

    this.slots = this.getMockSlotsForScenario();
    this.appointments = this.getMockAppointmentsForScenario();

    this.televisits = [
      {
        id: 501,
        appointmentId: 101,
        provider: 'LIVEKIT',
        status: 'SCHEDULED',
        token: 'tv-abc123-rossi'
      },
      {
        id: 502,
        appointmentId: 103,
        provider: 'LIVEKIT',
        status: 'SCHEDULED',
        token: 'tv-def456-verdi'
      },
      {
        id: 503,
        appointmentId: 107,
        provider: 'LIVEKIT',
        status: 'SCHEDULED',
        token: 'tv-ghi789-ferrari'
      },
      {
        id: 504,
        appointmentId: 108,
        provider: 'LIVEKIT',
        status: 'SCHEDULED',
        token: 'tv-jkl012-greco'
      },
      {
        id: 505,
        appointmentId: 109,
        provider: 'LIVEKIT',
        status: 'COMPLETED',
        token: 'tv-mno345-completed'
      }
    ];
    this.isLoading = false;
  }

  private loadMockAdmissionsForScenario(): void {
    this.isLoading = true;
    const baseDate = new Date();

    this.admissions = [
      {
        id: 401,
        patientId: 4,
        department: 'PNEUMO',
        bedId: 12,
        status: 'ACTIVE',
        admittedAt: new Date(baseDate.getTime() - 2 * 24 * 60 * 60000).toISOString(),
        notes: 'Medico referente: Dott.ssa Bianchi (subentrata a Dr. Martini)',
        appointmentId: 104
      },
      {
        id: 402,
        patientId: 6,
        department: 'PNEUMO',
        bedId: 14,
        status: 'ACTIVE',
        admittedAt: new Date(baseDate.getTime() - 3 * 24 * 60 * 60000).toISOString(),
        notes: 'Medico referente: Dott.ssa Bianchi (subentrata a Dr. Martini)',
        appointmentId: 106
      },
      {
        id: 403,
        patientId: 8,
        department: 'PNEUMO',
        bedId: 16,
        status: 'ACTIVE',
        admittedAt: new Date(baseDate.getTime() - 1 * 24 * 60 * 60000).toISOString(),
        notes: 'Medico referente: Dott.ssa Bianchi (subentrata a Dr. Martini)',
        appointmentId: 110
      },
      {
        id: 404,
        patientId: 10,
        department: 'PNEUMO',
        bedId: 18,
        status: 'ACTIVE',
        admittedAt: new Date(baseDate.getTime() - 4 * 24 * 60 * 60000).toISOString(),
        notes: 'Medico referente: Dott.ssa Bianchi (subentrata a Dr. Martini)',
        appointmentId: 111
      },
      {
        id: 405,
        patientId: 3,
        department: 'CARD',
        bedId: 5,
        status: 'CONFIRMED',
        admittedAt: new Date(baseDate.getTime() - 5 * 24 * 60 * 60000).toISOString(),
        notes: 'Ricovero cardiologico programmato'
      },
      {
        id: 406,
        patientId: 5,
        department: 'NEURO',
        bedId: 8,
        status: 'DISCHARGED',
        admittedAt: new Date(baseDate.getTime() - 10 * 24 * 60 * 60000).toISOString(),
        notes: 'Dimesso il 28/01'
      }
    ];
    this.isLoading = false;
  }

  private getMockSlotsForScenario(): SchedulingSlot[] {
    const baseDate = new Date();
    const tomorrow = new Date(baseDate);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dayAfter = new Date(baseDate);
    dayAfter.setDate(dayAfter.getDate() + 2);
    const twoDaysAfter = new Date(baseDate);
    twoDaysAfter.setDate(twoDaysAfter.getDate() + 3);

    return [
      { id: 201, doctorId: 2, date: tomorrow.toISOString().split('T')[0], time: '09:00', status: 'BOOKED', modality: 'REMOTE', notes: 'Convertito in televisita' },
      { id: 202, doctorId: 2, date: tomorrow.toISOString().split('T')[0], time: '10:00', status: 'BOOKED', modality: 'IN_PERSON', notes: 'Spirometria - riassegnato a Bianchi' },
      { id: 203, doctorId: 2, date: tomorrow.toISOString().split('T')[0], time: '11:00', status: 'BOOKED', modality: 'REMOTE', notes: 'Convertito in televisita' },
      { id: 204, doctorId: 2, date: tomorrow.toISOString().split('T')[0], time: '14:00', status: 'BOOKED', modality: 'IN_PERSON', notes: 'Ricovero' },
      { id: 205, doctorId: 2, date: dayAfter.toISOString().split('T')[0], time: '09:00', status: 'BOOKED', modality: 'IN_PERSON', notes: 'Auscultazione - riassegnato a Bianchi' },
      { id: 206, doctorId: 2, date: dayAfter.toISOString().split('T')[0], time: '10:30', status: 'BOOKED', modality: 'IN_PERSON', notes: 'Ricovero' },
      { id: 207, doctorId: 2, date: dayAfter.toISOString().split('T')[0], time: '14:00', status: 'BOOKED', modality: 'REMOTE', notes: 'Convertito in televisita' },
      { id: 208, doctorId: 2, date: dayAfter.toISOString().split('T')[0], time: '15:30', status: 'BOOKED', modality: 'REMOTE', notes: 'Convertito in televisita' },
      { id: 209, doctorId: 2, date: twoDaysAfter.toISOString().split('T')[0], time: '09:00', status: 'BOOKED', modality: 'IN_PERSON', notes: 'Spirometria - riassegnato a Bianchi' },
      { id: 210, doctorId: 2, date: twoDaysAfter.toISOString().split('T')[0], time: '11:00', status: 'BOOKED', modality: 'IN_PERSON', notes: 'Ricovero' },
      { id: 211, doctorId: 2, date: twoDaysAfter.toISOString().split('T')[0], time: '14:00', status: 'BOOKED', modality: 'REMOTE', notes: 'Convertito in televisita' },
      { id: 212, doctorId: 2, date: twoDaysAfter.toISOString().split('T')[0], time: '16:00', status: 'BOOKED', modality: 'IN_PERSON', notes: 'Auscultazione - riassegnato a Bianchi' }
    ];
  }

  private getMockAppointmentsForScenario(): SchedulingAppointment[] {
    return [
      { id: 101, patientId: 1, doctorId: 3, slotId: 201, reason: 'Follow-up pneumologico', status: 'CONFIRMED' },
      { id: 102, patientId: 2, doctorId: 3, slotId: 202, reason: 'Spirometria di controllo', status: 'CONFIRMED' },
      { id: 103, patientId: 3, doctorId: 3, slotId: 203, reason: 'Controllo post-trattamento', status: 'CONFIRMED' },
      { id: 104, patientId: 4, doctorId: 3, slotId: 204, reason: 'Valutazione ricovero', status: 'CONFIRMED' },
      { id: 105, patientId: 5, doctorId: 3, slotId: 205, reason: 'Auscultazione cardiopolmonare', status: 'CONFIRMED' },
      { id: 106, patientId: 6, doctorId: 3, slotId: 206, reason: 'Ricovero programmato', status: 'CONFIRMED' },
      { id: 107, patientId: 7, doctorId: 3, slotId: 207, reason: 'Consulenza pneumologica', status: 'CONFIRMED' },
      { id: 108, patientId: 1, doctorId: 3, slotId: 208, reason: 'Secondo follow-up', status: 'CONFIRMED' },
      { id: 109, patientId: 2, doctorId: 3, slotId: 209, reason: 'Spirometria', status: 'CONFIRMED' },
      { id: 110, patientId: 8, doctorId: 3, slotId: 210, reason: 'Ricovero urgente', status: 'CONFIRMED' },
      { id: 111, patientId: 3, doctorId: 3, slotId: 211, reason: 'Televisita controllo', status: 'CONFIRMED' },
      { id: 112, patientId: 5, doctorId: 3, slotId: 212, reason: 'Auscultazione finale', status: 'CONFIRMED' }
    ];
  }

  private loadDepartments(): void {
    this.api.request<DepartmentItem[]>('GET', '/api/departments').subscribe({
      next: (departments) => {
        if (departments.length > 0) {
          this.departments = departments;
        } else {
          this.departments = this.getMockDepartments();
        }
      },
      error: () => {
        this.departments = this.getMockDepartments();
      }
    });
  }

  private getMockDepartments(): DepartmentItem[] {
    return [
      { code: 'PNEUMO', name: 'Pneumologia', facilityCode: 'HQ' },
      { code: 'CARD', name: 'Cardiologia', facilityCode: 'HQ' },
      { code: 'NEURO', name: 'Neurologia', facilityCode: 'HQ' },
      { code: 'ORTO', name: 'Ortopedia', facilityCode: 'HQ' },
      { code: 'DERM', name: 'Dermatologia', facilityCode: 'HQ' }
    ];
  }

  private getConsentsPath(): string | null {
    if (this.isPatient) {
      return '/api/consents/me';
    }
    return null;
  }
}

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

// Allineato a PaymentOrderDto backend
interface PaymentItem {
  id: number;
  appointmentId: number;
  patientId: number;
  patientEmail?: string;
  patientName?: string;
  amountCents: number;
  currency: string;
  method: 'CARD' | 'BANK_TRANSFER' | 'CASH';
  provider: string;
  providerReference?: string;
  status: 'CREATED' | 'CAPTURED' | 'FAILED' | 'CANCELLED' | 'REFUNDED';
  description?: string;
  createdAt: string;
  updatedAt?: string;
  // Campi calcolati per UI
  amount?: number; // amountCents / 100
  service?: string; // alias per description
  // Campi per tracciamento solleciti (gestiti lato frontend)
  notificationAttempts?: { sentAt: string }[];
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

// Allineato a ServicePerformedDto backend
interface ServicePerformedItem {
  id: number;
  serviceType: 'MEDICAL_VISIT' | 'HOSPITALIZATION';
  sourceType: 'TELEVISIT' | 'ADMISSION';
  sourceId: number;
  patientId: number;
  patientSubject?: string;
  patientName?: string;
  patientEmail?: string;
  departmentCode?: string;
  description?: string;
  amountCents: number;
  currency: string;
  status: 'PENDING' | 'PAID' | 'FREE' | 'CANCELLED';
  performedAt: string;
  startedAt?: string;
  daysCount?: number;
  reminderCount: number;
  lastReminderAt?: string;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
  // Campi calcolati per UI
  amount?: number; // amountCents / 100
}

interface ServicePerformedStats {
  totalServices: number;
  paidWithin7Days: number;
  paidWithReminder: number;
  stillPending: number;
  percentWithin7Days: number;
  percentWithReminder: number;
  percentPending: number;
  filteredCount: number;
}

// Allineato a AdmissionDto backend
interface AdmissionItem {
  id: number;
  patientId: number;
  departmentCode: string;
  admissionType: 'INPATIENT' | 'DAY_HOSPITAL' | 'OBSERVATION';
  status: 'ACTIVE' | 'DISCHARGED' | 'CANCELLED';
  admittedAt: string;
  dischargedAt?: string;
  notes?: string;
  attendingDoctorId?: number;
  // Campo legacy per compatibilità template
  department?: string;
}

// Allineato a NotificationDto backend
interface NotificationItem {
  id: number;
  recipientType: 'DOCTOR' | 'PATIENT' | 'ADMIN';
  recipientId: string;
  channel: 'EMAIL' | 'IN_APP';
  toAddress?: string;
  subject: string;
  body: string;
  status: 'PENDING' | 'SENT' | 'FAILED';
  createdAt: string;
  sentAt?: string;
  errorMessage?: string;
  // Campi calcolati per UI
  recipient?: string; // alias per toAddress o recipientId
  message?: string; // alias per body
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

// Allineato a TelevisitDto backend
interface TelevisitItem {
  id: number;
  roomName: string;
  department: string;
  doctorSubject: string;
  patientSubject: string;
  scheduledAt: string;
  status: 'CREATED' | 'SCHEDULED' | 'ACTIVE' | 'ENDED' | 'CANCELED';
  // Campi legacy per compatibilità template (popolati da mapping)
  provider?: string;
  token?: string;
}

interface LiveKitTokenResponse {
  roomName: string;
  livekitUrl: string;
  token: string;
  expiresInSeconds: number;
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

// Allineato a CapacityDto backend
interface CapacityItem {
  departmentCode: string;
  totalBeds: number;
  occupiedBeds: number;
  availableBeds: number;
  updatedAt: string;
}

interface AuditItem {
  id: number;
  action: string;
  actor: string;
  timestamp: string;
}

// DTO che mappa esattamente il backend AuditEventDto
interface AuditEventResponse {
  id: number;
  occurredAt: string;
  source: string;
  actorType: string;
  actorId: string;
  action: string;
  resourceType?: string;
  resourceId?: string;
  outcome: string;
  ip?: string;
  traceId?: string;
  details?: Record<string, unknown>;
}

// Risposta paginata Spring Boot
interface SpringPage<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// DTO per lookup pazienti da directory
interface PatientDirectoryDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  fiscalCode: string;
  birthDate?: string;
  address?: string;
  status: string;
}

// DTO per lookup medici da directory
interface DoctorDirectoryDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  specialization?: string;
  departmentCode?: string;
  departmentName?: string;
  facilityCode?: string;
  status: string;
}

// DTO per consensi da API
interface ConsentApiDto {
  id: number;
  patientId: number;
  doctorId: number;
  doctorName?: string;
  scope: string;
  status: string;
  grantedAt: string;
  revokedAt?: string;
  expiresAt?: string;
}

// Interfacce estese per Audit & Compliance dettagliato
interface AuditEventDetail {
  id: number;
  eventType: 'CONSENT_GRANTED' | 'CONSENT_REVOKED' | 'DOCUMENT_ACCESS' | 'PRESCRIPTION_VIEW' | 'APPOINTMENT_BOOKED' | 'APPOINTMENT_CANCELLED' | 'PROFILE_UPDATE';
  action: string;
  actorType: 'USER' | 'SERVICE' | 'SYSTEM' | 'ANONYMOUS';
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
  outcome: 'SUCCESS' | 'DENIED' | 'FAILURE';
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
    | 'admin-admissions'
    | 'admin-notifications'
    | 'admin-payments' = 'api';
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
    amount: 0,
    doctorId: null as number | null,
    patientId: null as number | null,
    performedAt: ''
  };
  filteredPatientsForPayment: PatientItem[] = [];
  // ServicePerformed (prestazioni sanitarie)
  servicesPerformed: ServicePerformedItem[] = [];
  filteredServices: ServicePerformedItem[] = [];
  serviceStats: ServicePerformedStats = {
    totalServices: 0,
    paidWithin7Days: 0,
    paidWithReminder: 0,
    stillPending: 0,
    percentWithin7Days: 0,
    percentWithReminder: 0,
    percentPending: 0,
    filteredCount: 0
  };
  serviceStatusFilter: 'ALL' | 'PENDING' | 'PAID' | 'FREE' | 'CANCELLED' = 'ALL';
  serviceDaysFilter: number | null = null;
  selectedServiceIds = new Set<number>();
  servicesError = '';
  servicesSuccess = '';
  showServiceEditModal = false;
  showServiceFreeModal = false;
  showServiceDetailModal = false;
  selectedService: ServicePerformedItem | null = null;
  selectedServiceDetail: ServicePerformedItem | null = null;
  serviceFreeReason = '';
  serviceEditForm = {
    amountCents: 0,
    patientName: '',
    patientEmail: '',
    notes: ''
  };
  admissionForm = {
    patientId: null as number | null,
    facilityCode: '',
    departmentCode: '',
    admissionType: 'INPATIENT' as 'INPATIENT' | 'DAY_HOSPITAL' | 'OBSERVATION',
    notes: '',
    attendingDoctorId: null as number | null
  };
  filteredAdmissionPatients: PatientItem[] = [];
  notifications: NotificationItem[] = [];
  notificationsError = '';
  notificationsSuccess = '';
  showAdminNotificationModal = false;
  showNotificationDetailModal = false;
  selectedNotification: NotificationItem | null = null;
  showAdminPaymentModal = false;
  notificationForm = {
    recipient: '',
    channel: 'EMAIL',
    recipientType: 'PATIENT' as 'PATIENT' | 'DOCTOR' | 'ADMIN',
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
    doctorId: null as number | null,
    patientId: null as number | null,
    facilityCode: '',
    department: '',
    scheduledAt: ''
  };
  televisitPatientsFiltered: PatientItem[] = [];
  showAdminTelevisitModal = false;
  showEndTelevisitModal = false;
  showDeleteTelevisitModal = false;
  showCancelTelevisitModal = false;
  selectedTelevisitForAction: TelevisitItem | null = null;
  // Overlay televisit room
  showTelevisitRoomOverlay = false;
  televisitRoomConfig: {
    id: string;
    room: string;
    url: string;
    token: string;
  } | null = null;
  showAdminAdmissionModal = false;
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
  // Modale note editabile
  showAdmissionEditNoteModal = false;
  admissionEditNoteTarget: AdmissionItem | null = null;
  admissionEditNoteText = '';
  admissionEditNoteError = '';
  showSlotModal = false;
  doctors: DoctorItem[] = [];
  patients: PatientItem[] = [];
  facilities: FacilityItem[] = [];
  departments: DepartmentItem[] = [];
  departmentCapacities: CapacityItem[] = [];
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
  auditError = '';

  // Audit & Compliance - Stato semplificato
  auditDetailedEvents: AuditEventDetail[] = [];
  auditFilteredEvents: AuditEventDetail[] = [];
  auditSearchText = '';
  auditSelectedEvent: AuditEventDetail | null = null;

  auditOutcomeOptions = [
    { value: 'SUCCESS', label: 'Successo' },
    { value: 'DENIED', label: 'Negato' },
    { value: 'FAILURE', label: 'Fallito' }
  ];

  showPaymentQuestionModal = false;
  selectedPayment: PaymentItem | null = null;
  paymentQuestion = '';

  // Admin Payments - Filtri e statistiche (allineati a PaymentStatus backend)
  paymentStatusFilter: 'ALL' | 'CREATED' | 'CAPTURED' | 'FAILED' | 'CANCELLED' | 'REFUNDED' = 'ALL';
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
        | 'admin-admissions'
        | 'admin-notifications'
        | 'admin-payments') ??
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
        this.loadServicesPerformed();
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
    if (this.mode === 'admin-notifications') {
      this.loadNotifications();
      this.loadPatients();
    }
    if (this.mode === 'admin-payments') {
      this.loadPayments();
      this.loadPatients();
    }
  }

  selectEndpoint(endpoint: ResourceEndpoint): void {
    this.selectedEndpoint = endpoint;
    this.payload = endpoint.payload ?? '';
    this.responseBody = '';
  }

  refresh(): void {
    if (this.mode === 'scheduling') {
      this.loadScheduling();
    }
    if (this.mode === 'docs') {
      this.loadDocs();
    }
    if (this.mode === 'payments') {
      this.loadPayments();
    }
    if (this.mode === 'admissions') {
      this.loadAdmissions();
    }
    if (this.mode === 'notifications') {
      this.loadNotifications();
      if (this.isAdmin) {
        this.loadServicesPerformed();
      }
    }
    if (this.mode === 'prescribing') {
      this.loadPrescriptions();
    }
    if (this.mode === 'televisit') {
      this.loadTelevisits();
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
    if (this.mode === 'admin-notifications') {
      this.loadNotifications();
    }
    if (this.mode === 'admin-payments') {
      this.loadPayments();
    }
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

  getDoctorLabel(doctorId: number | null | undefined): string {
    if (doctorId == null) return '-';
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
      return 'Area riservata';
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

  getPaymentStatusLabel(payment: PaymentItem): string {
    const labels: Record<string, string> = {
      CREATED: 'In attesa di pagamento',
      CAPTURED: 'Pagamento ricevuto',
      FAILED: 'Non riuscito',
      CANCELLED: 'Annullato',
      REFUNDED: 'Rimborsato'
    };
    return labels[payment.status] ?? payment.status;
  }

  canMarkPaymentAsPaid(payment: PaymentItem): boolean {
    return payment.status === 'CREATED';
  }

  canAttachPaymentReceipt(payment: PaymentItem): boolean {
    return payment.status === 'CAPTURED';
  }

  get latestConfirmedAdmission(): AdmissionItem | null {
    // Admission backend usa 'DISCHARGED' per indicare completato, 'ACTIVE' per in corso
    const admissions = this.visibleAdmissions.filter((admission) => admission.status === 'DISCHARGED' || admission.status === 'ACTIVE');
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
    // Backend usa 'CREATED' per pagamenti in attesa
    const pending = this.payments.filter(
      (payment) => payment.status === 'CREATED'
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
      // Filtra ricoveri per patientId (backend non ha appointmentId)
      return this.admissions.filter((admission) => patientIds.has(admission.patientId));
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
    // Filtra per nome paziente
    const patient = this.patients.find(p => p.id === this.televisitPatientFilterId);
    if (!patient) return this.televisits;
    const patientFullName = `${patient.firstName} ${patient.lastName}`;
    return this.televisits.filter(
      (televisit) => televisit.patientSubject === patientFullName
    );
  }

  get televisitDepartmentsFiltered(): DepartmentItem[] {
    if (!this.televisitForm.facilityCode) {
      return [];
    }
    return this.departments.filter(d => d.facilityCode === this.televisitForm.facilityCode);
  }

  get televisitDoctorsFiltered(): DoctorItem[] {
    if (!this.televisitForm.department) {
      return [];
    }
    return this.doctors.filter(d => d.departmentCode === this.televisitForm.department);
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
      PENDING: 'In coda',
      SENT: 'Inviata',
      DELIVERED: 'Consegnata',
      FAILED: 'Non consegnata'
    };
    return labels[status] ?? status;
  }

  truncateText(text: string | undefined, maxLength: number = 50): string {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
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
      CREATED: 'Creata',
      SCHEDULED: 'Programmata',
      ACTIVE: 'In corso',
      ENDED: 'Conclusa',
      CANCELED: 'Annullata'
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

  get currentDoctorName(): string {
    const doctor = this.doctors[0];
    return doctor ? `${doctor.firstName} ${doctor.lastName}` : '';
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
    return this.getDepartmentLabel(televisit.department);
  }

  getTelevisitPatientLabel(televisit: TelevisitItem): string {
    // Il backend restituisce patientSubject, possiamo cercare il paziente per subject
    // oppure mostrare direttamente il subject abbreviato
    if (!televisit.patientSubject) {
      return '-';
    }
    // Mostra il subject troncato (può essere un UUID o un identificatore)
    return televisit.patientSubject.length > 20
      ? televisit.patientSubject.substring(0, 17) + '...'
      : televisit.patientSubject;
  }

  getTelevisitDoctorLabel(televisit: TelevisitItem): string {
    if (!televisit.doctorSubject) {
      return '-';
    }
    return televisit.doctorSubject.length > 20
      ? televisit.doctorSubject.substring(0, 17) + '...'
      : televisit.doctorSubject;
  }

  getTelevisitDateTimeLabel(televisit: TelevisitItem): string {
    if (!televisit.scheduledAt) {
      return '-';
    }
    return this.formatDateTime(televisit.scheduledAt);
  }

  getTelevisitBookingStatusLabel(televisit: TelevisitItem): string {
    // Restituisce lo stato della televisita direttamente
    return this.getTelevisitStatusLabel(televisit.status);
  }

  copyTelevisitLink(televisit: TelevisitItem): void {
    const link = `${window.location.origin}/televisit/room?id=${televisit.id}&room=${televisit.roomName}`;
    navigator.clipboard.writeText(link).then(() => {
      this.showToast('Link copiato negli appunti', 'success');
    }).catch(() => {
      this.showToast('Errore durante la copia del link', 'danger');
    });
  }

  /**
   * Avvia la sessione televisita e apre la room LiveKit.
   * - Per sessioni CREATED/SCHEDULED: chiama start, poi token, poi naviga
   * - Per sessioni ACTIVE: salta start, richiede solo token e naviga
   */
  startTelevisitSession(televisit: TelevisitItem): void {
    // Blocca solo se già conclusa, annullata o in uno stato non valido
    if (televisit.status === 'ENDED' || televisit.status === 'CANCELED') {
      this.showToast('La televisita non può essere avviata in questo stato', 'warning');
      return;
    }

    this.isLoading = true;
    this.televisitError = '';

    // Se già ACTIVE, salta lo step di start e vai direttamente al token
    if (televisit.status === 'ACTIVE') {
      this.fetchTokenAndNavigate(televisit);
      return;
    }

    // Step 1: Avvia la sessione (cambia stato in ACTIVE)
    this.api.request<TelevisitItem>('POST', `/api/televisits/${televisit.id}/start`).subscribe({
      next: (updatedTelevisit) => {
        // Aggiorna lo stato locale
        this.televisits = this.televisits.map(tv =>
          tv.id === televisit.id ? { ...tv, status: 'ACTIVE' as const } : tv
        );

        // Step 2: Ottieni il token LiveKit e naviga
        this.fetchTokenAndNavigate(televisit);
      },
      error: (err) => {
        this.isLoading = false;
        this.televisitError = err?.error?.message || 'Errore nell\'avvio della televisita.';
        this.showToast('Errore nell\'avvio della televisita', 'danger');
      }
    });
  }

  /**
   * Richiede il token LiveKit e mostra l'overlay della room.
   */
  private fetchTokenAndNavigate(televisit: TelevisitItem): void {
    this.api.request<LiveKitTokenResponse>('POST', `/api/televisits/${televisit.id}/token/doctor`).subscribe({
      next: (tokenResponse) => {
        this.isLoading = false;
        // Mostra l'overlay della room invece di navigare
        this.televisitRoomConfig = {
          id: String(televisit.id),
          room: tokenResponse.roomName,
          url: tokenResponse.livekitUrl,
          token: tokenResponse.token
        };
        this.showTelevisitRoomOverlay = true;
      },
      error: (err) => {
        this.isLoading = false;
        this.televisitError = 'Errore nel recupero del token LiveKit.';
        this.showToast('Errore nel recupero del token LiveKit', 'danger');
      }
    });
  }

  /**
   * Chiude l'overlay della room televisit.
   */
  closeTelevisitRoomOverlay(): void {
    this.showTelevisitRoomOverlay = false;
    this.televisitRoomConfig = null;
  }

  /**
   * Apre il modale per eliminare una televisita.
   */
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
    this.api.request<void>('DELETE', `/api/admin/televisits/${this.selectedTelevisitForAction.id}?force=true`).subscribe({
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

  /**
   * Apre il modale per concludere una televisita.
   */
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
    this.api.request<TelevisitItem>('POST', `/api/televisits/${this.selectedTelevisitForAction.id}/end`).subscribe({
      next: (updatedTelevisit) => {
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

  /**
   * Apre il modale per annullare una televisita.
   */
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
    this.api.request<TelevisitItem>('POST', `/api/televisits/${this.selectedTelevisitForAction.id}/cancel`).subscribe({
      next: (updatedTelevisit) => {
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

  private showToast(message: string, type: 'success' | 'danger' | 'warning' | 'info'): void {
    // Crea un toast temporaneo per il feedback
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
    // Backend AdmissionDto non ha appointmentId, mostra info paziente/reparto
    const patientLabel = this.getPatientLabel(admission.patientId);
    const dept = admission.departmentCode || admission.department || '-';
    return `${patientLabel} • ${dept}`;
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
    // Può essere ripianificata se è in stato CREATED e la data è nel futuro
    if (televisit.status !== 'CREATED') {
      return false;
    }
    if (!televisit.scheduledAt) {
      return false;
    }
    const parsed = new Date(televisit.scheduledAt);
    return parsed.getTime() >= Date.now();
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
        const rawPayments = Array.isArray(payments) ? payments : (payments?.content ?? []);
        this.payments = rawPayments.map((p) => this.mapPaymentFromBackend(p));
        this.updatePaymentStats();
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
        error: () => {}
      });
    } else {
      this.admissions = [];
    }
  }

  // Mappa PaymentOrderDto backend -> PaymentItem frontend
  private mapPaymentFromBackend(p: PaymentItem): PaymentItem {
    return {
      ...p,
      amount: p.amountCents ? p.amountCents / 100 : p.amount,
      service: p.description || p.service
    };
  }

  // Aggiorna statistiche pagamenti
  private updatePaymentStats(): void {
    const total = this.payments.length;
    if (total === 0) {
      this.paymentStats = { totalPayments: 0, completedWithin7Days: 0, completedWithReminder: 0, stillPending: 0, percentWithin7Days: 0, percentWithReminder: 0, percentPending: 0 };
      return;
    }
    const captured = this.payments.filter((p) => p.status === 'CAPTURED').length;
    const pending = this.payments.filter((p) => p.status === 'CREATED').length;
    const failed = this.payments.filter((p) => p.status === 'FAILED').length;
    this.paymentStats = {
      totalPayments: total, completedWithin7Days: captured, completedWithReminder: failed, stillPending: pending,
      percentWithin7Days: Math.round((captured / total) * 100), percentWithReminder: Math.round((failed / total) * 100), percentPending: Math.round((pending / total) * 100)
    };
  }

  // Conferma pagamento (admin)
  confirmAdminPayment(payment: PaymentItem): void {
    if (payment.status === 'CAPTURED') return;
    this.isLoading = true;
    this.paymentsError = '';
    this.api.request<PaymentItem>('POST', `/api/admin/payments/${payment.id}/capture`).subscribe({
      next: (updated) => {
        this.payments = this.payments.map((p) => p.id === updated.id ? this.mapPaymentFromBackend(updated) : p);
        this.updatePaymentStats();
        this.paymentsSuccess = `Pagamento #${payment.id} confermato con successo.`;
        this.isLoading = false;
      },
      error: () => { this.paymentsError = `Impossibile confermare il pagamento #${payment.id}.`; this.isLoading = false; }
    });
  }

  // Invia sollecito singolo
  sendSingleReminder(payment: PaymentItem): void {
    if (payment.status === 'CAPTURED') return;
    this.isLoading = true;
    this.paymentsError = '';
    this.api.request<void>('POST', `/api/admin/payments/${payment.id}/reminder`).subscribe({
      next: () => { this.paymentsSuccess = `Sollecito inviato a ${payment.patientName || payment.patientEmail || 'paziente'}.`; this.isLoading = false; },
      error: () => { this.paymentsError = `Impossibile inviare sollecito per il pagamento #${payment.id}.`; this.isLoading = false; }
    });
  }

  // Elimina pagamento (admin)
  deleteAdminPayment(payment: PaymentItem): void {
    this.isLoading = true;
    this.paymentsError = '';
    this.api.request<PaymentItem>('POST', `/api/admin/payments/${payment.id}/cancel`).subscribe({
      next: () => {
        this.payments = this.payments.filter((p) => p.id !== payment.id);
        this.selectedPaymentIds.delete(payment.id);
        this.updatePaymentStats();
        this.paymentsSuccess = `Pagamento #${payment.id} annullato.`;
        this.isLoading = false;
      },
      error: () => { this.paymentsError = `Impossibile annullare il pagamento #${payment.id}.`; this.isLoading = false; }
    });
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

  // ========== GESTIONE PRESTAZIONI (ServicePerformed) ==========

  loadServicesPerformed(): void {
    this.isLoading = true;
    this.servicesError = '';
    this.servicesSuccess = '';

    // Carica statistiche
    this.api.request<ServicePerformedStats>('GET', '/api/admin/services/stats').subscribe({
      next: (stats) => {
        this.serviceStats = stats;
      },
      error: () => {}
    });

    // Carica lista prestazioni
    const statusParam = this.serviceStatusFilter !== 'ALL' ? `?status=${this.serviceStatusFilter}` : '';
    this.api.request<{ content: ServicePerformedItem[] } | ServicePerformedItem[]>('GET', `/api/admin/services${statusParam}`).subscribe({
      next: (services) => {
        const rawServices = Array.isArray(services) ? services : (services?.content ?? []);
        this.servicesPerformed = rawServices.map((s) => this.mapServiceFromBackend(s));
        this.updateFilteredServices();
        this.isLoading = false;
      },
      error: () => {
        this.servicesError = 'Impossibile caricare le prestazioni.';
        this.isLoading = false;
      }
    });
  }

  private mapServiceFromBackend(s: ServicePerformedItem): ServicePerformedItem {
    return {
      ...s,
      amount: s.amountCents ? s.amountCents / 100 : 0
    };
  }

  private updateFilteredServices(): void {
    let filtered = [...this.servicesPerformed];

    // Filtra per stato
    if (this.serviceStatusFilter !== 'ALL') {
      filtered = filtered.filter(s => s.status === this.serviceStatusFilter);
    }

    // Filtra per giorni dalla prestazione
    if (this.serviceDaysFilter !== null) {
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - this.serviceDaysFilter);
      filtered = filtered.filter(s => new Date(s.performedAt) < cutoffDate);
    }

    this.filteredServices = filtered;
    this.serviceStats = { ...this.serviceStats, filteredCount: filtered.length };
  }

  onServiceStatusFilterChange(): void {
    this.updateFilteredServices();
  }

  onServiceDaysFilterChange(): void {
    this.updateFilteredServices();
  }

  toggleServiceSelection(id: number): void {
    if (this.selectedServiceIds.has(id)) {
      this.selectedServiceIds.delete(id);
    } else {
      this.selectedServiceIds.add(id);
    }
  }

  isServiceSelected(id: number): boolean {
    return this.selectedServiceIds.has(id);
  }

  toggleAllServicesSelection(): void {
    const pendingServices = this.filteredServices.filter(s => s.status === 'PENDING');
    if (this.selectedServiceIds.size === pendingServices.length) {
      this.selectedServiceIds.clear();
    } else {
      pendingServices.forEach(s => this.selectedServiceIds.add(s.id));
    }
  }

  // Segna come pagato
  markServiceAsPaid(service: ServicePerformedItem): void {
    if (service.status === 'PAID') return;
    this.isLoading = true;
    this.servicesError = '';
    this.api.request<ServicePerformedItem>('POST', `/api/admin/services/${service.id}/paid`).subscribe({
      next: (updated) => {
        this.servicesPerformed = this.servicesPerformed.map(s => s.id === updated.id ? this.mapServiceFromBackend(updated) : s);
        this.updateFilteredServices();
        this.servicesSuccess = `Prestazione #${service.id} segnata come pagata.`;
        this.isLoading = false;
        this.loadServicesPerformed();
      },
      error: () => {
        this.servicesError = `Impossibile confermare il pagamento della prestazione #${service.id}.`;
        this.isLoading = false;
      }
    });
  }

  // Apri modal per convertire in gratuito
  openServiceFreeModal(service: ServicePerformedItem): void {
    this.selectedService = service;
    this.serviceFreeReason = '';
    this.showServiceFreeModal = true;
  }

  closeServiceFreeModal(): void {
    this.showServiceFreeModal = false;
    this.selectedService = null;
    this.serviceFreeReason = '';
  }

  // Converti in gratuito
  confirmMarkServiceAsFree(): void {
    if (!this.selectedService) return;
    this.isLoading = true;
    this.servicesError = '';
    const reasonParam = this.serviceFreeReason ? `?reason=${encodeURIComponent(this.serviceFreeReason)}` : '';
    this.api.request<ServicePerformedItem>('POST', `/api/admin/services/${this.selectedService.id}/free${reasonParam}`).subscribe({
      next: (updated) => {
        this.servicesPerformed = this.servicesPerformed.map(s => s.id === updated.id ? this.mapServiceFromBackend(updated) : s);
        this.updateFilteredServices();
        this.servicesSuccess = `Prestazione #${this.selectedService!.id} convertita in gratuita.`;
        this.closeServiceFreeModal();
        this.isLoading = false;
        this.loadServicesPerformed();
      },
      error: () => {
        this.servicesError = `Impossibile convertire la prestazione #${this.selectedService!.id} in gratuita.`;
        this.isLoading = false;
      }
    });
  }

  // Apri modal per modifica
  openServiceEditModal(service: ServicePerformedItem): void {
    this.selectedService = service;
    this.serviceEditForm = {
      amountCents: service.amountCents,
      patientName: service.patientName || '',
      patientEmail: service.patientEmail || '',
      notes: service.notes || ''
    };
    this.showServiceEditModal = true;
  }

  closeServiceEditModal(): void {
    this.showServiceEditModal = false;
    this.selectedService = null;
  }

  openServiceDetailModal(service: ServicePerformedItem): void {
    this.selectedServiceDetail = service;
    this.showServiceDetailModal = true;
  }

  closeServiceDetailModal(): void {
    this.showServiceDetailModal = false;
    this.selectedServiceDetail = null;
  }

  getDepartmentNameByCode(code: string | undefined): string {
    if (!code) return '-';
    const dept = this.departments.find(d => d.code === code);
    return dept?.name ?? code;
  }

  getFacilityNameByDepartmentCode(code: string | undefined): string {
    if (!code) return '-';
    const dept = this.departments.find(d => d.code === code);
    if (!dept?.facilityCode) return '-';
    const facility = this.facilities.find(f => f.code === dept.facilityCode);
    return facility?.name ?? dept.facilityCode;
  }

  // Salva modifiche
  saveServiceEdit(): void {
    if (!this.selectedService) return;
    this.isLoading = true;
    this.servicesError = '';

    const payload = {
      amountCents: this.serviceEditForm.amountCents,
      patientName: this.serviceEditForm.patientName || null,
      patientEmail: this.serviceEditForm.patientEmail || null,
      notes: this.serviceEditForm.notes || null
    };

    this.api.request<ServicePerformedItem>('PATCH', `/api/admin/services/${this.selectedService.id}`, payload).subscribe({
      next: (updated) => {
        this.servicesPerformed = this.servicesPerformed.map(s => s.id === updated.id ? this.mapServiceFromBackend(updated) : s);
        this.updateFilteredServices();
        this.servicesSuccess = `Prestazione #${this.selectedService!.id} aggiornata.`;
        this.closeServiceEditModal();
        this.isLoading = false;
      },
      error: () => {
        this.servicesError = `Impossibile aggiornare la prestazione #${this.selectedService!.id}.`;
        this.isLoading = false;
      }
    });
  }

  // Elimina prestazione
  deleteService(service: ServicePerformedItem): void {
    if (!confirm(`Sei sicuro di voler eliminare la prestazione #${service.id}?`)) return;
    this.isLoading = true;
    this.servicesError = '';
    this.api.request<void>('DELETE', `/api/admin/services/${service.id}`).subscribe({
      next: () => {
        this.servicesPerformed = this.servicesPerformed.filter(s => s.id !== service.id);
        this.selectedServiceIds.delete(service.id);
        this.updateFilteredServices();
        this.servicesSuccess = `Prestazione #${service.id} eliminata.`;
        this.isLoading = false;
        this.loadServicesPerformed();
      },
      error: () => {
        this.servicesError = `Impossibile eliminare la prestazione #${service.id}.`;
        this.isLoading = false;
      }
    });
  }

  // Invia sollecito singolo
  sendServiceReminder(service: ServicePerformedItem): void {
    if (service.status !== 'PENDING') return;
    this.isLoading = true;
    this.servicesError = '';
    this.api.request<ServicePerformedItem>('POST', `/api/admin/services/${service.id}/reminder`).subscribe({
      next: (updated) => {
        this.servicesPerformed = this.servicesPerformed.map(s => s.id === updated.id ? this.mapServiceFromBackend(updated) : s);
        this.updateFilteredServices();
        this.servicesSuccess = `Sollecito inviato per prestazione #${service.id}.`;
        this.isLoading = false;
      },
      error: () => {
        this.servicesError = `Impossibile inviare sollecito per la prestazione #${service.id}. Verifica che l'email del paziente sia presente.`;
        this.isLoading = false;
      }
    });
  }

  // Invia solleciti multipli
  sendBulkServiceReminders(): void {
    if (this.selectedServiceIds.size === 0) return;
    this.isLoading = true;
    this.servicesError = '';
    const ids = Array.from(this.selectedServiceIds);
    this.api.request<void>('POST', '/api/admin/services/bulk-reminders', ids).subscribe({
      next: () => {
        this.servicesSuccess = `Solleciti inviati a ${ids.length} pazienti.`;
        this.selectedServiceIds.clear();
        this.isLoading = false;
        this.loadServicesPerformed();
      },
      error: () => {
        this.servicesError = 'Impossibile inviare i solleciti.';
        this.isLoading = false;
      }
    });
  }

  getServiceTypeLabel(type: string): string {
    switch (type) {
      case 'MEDICAL_VISIT': return 'Visita medica';
      case 'HOSPITALIZATION': return 'Ricovero';
      default: return type;
    }
  }

  getServiceStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'In attesa';
      case 'PAID': return 'Pagato';
      case 'FREE': return 'Gratuito';
      case 'CANCELLED': return 'Annullato';
      default: return status;
    }
  }

  getServiceDaysOverdue(service: ServicePerformedItem): number {
    if (!service.performedAt) return 0;
    const performedDate = new Date(service.performedAt);
    const today = new Date();
    const diffTime = today.getTime() - performedDate.getTime();
    return Math.floor(diffTime / (1000 * 60 * 60 * 24));
  }

  // ========== FINE GESTIONE PRESTAZIONI ==========

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
    if (!pendingPayment || pendingPayment.status !== 'CREATED') {
      this.paymentsError = 'Pagamento selezionato non valido.';
      return;
    }
    this.isLoading = true;
    this.paymentsError = '';
    this.api.request<PaymentItem>('POST', `/api/admin/payments/${this.paymentForm.paymentId}/capture`).subscribe({
      next: (payment) => {
        this.payments = this.payments.map((item) => (item.id === payment.id ? this.mapPaymentFromBackend(payment) : item));
        this.updatePaymentStats();
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

  submitNewPayment(): void {
    if (!this.paymentForm.doctorId) {
      this.paymentsError = 'Seleziona un medico.';
      return;
    }
    if (!this.paymentForm.patientId) {
      this.paymentsError = 'Seleziona un paziente.';
      return;
    }
    if (!this.paymentForm.service.trim()) {
      this.paymentsError = 'Inserisci la descrizione della prestazione.';
      return;
    }
    if (!this.paymentForm.amount || this.paymentForm.amount <= 0) {
      this.paymentsError = "Inserisci l'importo del pagamento.";
      return;
    }
    if (!this.paymentForm.performedAt) {
      this.paymentsError = 'Inserisci la data della prestazione.';
      return;
    }
    this.isLoading = true;
    this.paymentsError = '';
    this.paymentsSuccess = '';

    const payload = {
      doctorId: this.paymentForm.doctorId,
      patientId: this.paymentForm.patientId,
      serviceType: 'CUSTOM',
      description: this.paymentForm.service,
      amountCents: Math.round(this.paymentForm.amount * 100),
      performedAt: new Date(this.paymentForm.performedAt).toISOString()
    };

    this.api.request<ServicePerformedItem>('POST', '/api/admin/services', payload).subscribe({
      next: (service) => {
        const mappedService = this.mapServiceFromBackend(service);
        this.servicesPerformed = [mappedService, ...this.servicesPerformed];
        this.updateFilteredServices();
        this.paymentsSuccess = `Pagamento #${service.id} creato con successo.`;
        this.paymentForm.doctorId = null;
        this.paymentForm.patientId = null;
        this.paymentForm.service = '';
        this.paymentForm.amount = 0;
        this.paymentForm.performedAt = '';
        this.filteredPatientsForPayment = [];
        this.isLoading = false;
        setTimeout(() => {
          this.closeAdminPaymentModal();
          this.paymentsSuccess = '';
        }, 1500);
      },
      error: () => {
        this.paymentsError = 'Impossibile creare il pagamento.';
        this.isLoading = false;
      }
    });
  }

  // Getter: reparti filtrati per struttura selezionata
  get filteredDepartments(): DepartmentItem[] {
    if (!this.admissionForm.facilityCode) {
      return [];
    }
    return this.departments.filter(
      (dept) => dept.facilityCode === this.admissionForm.facilityCode
    );
  }

  // Getter: medici filtrati per reparto selezionato
  get filteredDoctors(): DoctorItem[] {
    if (!this.admissionForm.departmentCode) {
      return [];
    }
    return this.doctors.filter(
      (doctor) => doctor.departmentCode === this.admissionForm.departmentCode
    );
  }

  // Handler cambio struttura: reset reparto, medico e paziente
  onAdmissionFacilityChange(): void {
    this.admissionForm.departmentCode = '';
    this.admissionForm.attendingDoctorId = null;
    this.admissionForm.patientId = null;
    this.filteredAdmissionPatients = [];
  }

  // Handler cambio reparto: reset medico e paziente
  onAdmissionDepartmentChange(): void {
    this.admissionForm.attendingDoctorId = null;
    this.admissionForm.patientId = null;
    this.filteredAdmissionPatients = [];
  }

  // Getter per i posti disponibili nel reparto selezionato
  get selectedDepartmentAvailableBeds(): number | null {
    if (!this.admissionForm.departmentCode) return null;
    const capacity = this.departmentCapacities.find(
      c => c.departmentCode.toUpperCase() === this.admissionForm.departmentCode.toUpperCase()
    );
    return capacity?.availableBeds ?? null;
  }

  // Handler cambio medico: carica pazienti con consenso RECORDS per il medico
  onAdmissionDoctorChange(): void {
    this.admissionForm.patientId = null;
    this.filteredAdmissionPatients = [];

    if (!this.admissionForm.attendingDoctorId) {
      return;
    }

    // Carica i pazienti che hanno dato consenso RECORDS al medico selezionato
    this.api.request<number[]>('GET', `/api/consents/patients-with-consent?scope=RECORDS&doctorId=${this.admissionForm.attendingDoctorId}`).subscribe({
      next: (patientIds) => {
        // Filtra i pazienti già caricati per mostrare solo quelli con consenso
        this.filteredAdmissionPatients = this.patients.filter(p => patientIds.includes(p.id));
      },
      error: () => {
        this.filteredAdmissionPatients = [];
      }
    });
  }

  submitAdmission(): void {
    this.isLoading = true;
    this.paymentsError = '';

    // Validazione campi richiesti
    if (!this.admissionForm.patientId) {
      this.paymentsError = 'Seleziona un paziente.';
      this.isLoading = false;
      return;
    }
    if (!this.admissionForm.facilityCode?.trim()) {
      this.paymentsError = 'Seleziona una struttura.';
      this.isLoading = false;
      return;
    }
    if (!this.admissionForm.departmentCode?.trim()) {
      this.paymentsError = 'Seleziona un reparto.';
      this.isLoading = false;
      return;
    }
    if (!this.admissionForm.attendingDoctorId) {
      this.paymentsError = 'Seleziona un medico referente.';
      this.isLoading = false;
      return;
    }

    // Payload allineato a AdmissionCreateDto backend
    const payload = {
      patientId: this.admissionForm.patientId,
      departmentCode: this.admissionForm.departmentCode,
      admissionType: this.admissionForm.admissionType,
      notes: this.admissionForm.notes || null,
      attendingDoctorId: this.admissionForm.attendingDoctorId
    };

    this.api.request<AdmissionItem>('POST', '/api/admissions', payload).subscribe({
      next: (admission) => {
        // Mappa per compatibilità template
        const mappedAdmission: AdmissionItem = {
          ...admission,
          department: admission.departmentCode
        };
        this.admissions = [...this.admissions, mappedAdmission];
        // Reset form
        this.admissionForm = {
          patientId: null,
          facilityCode: '',
          departmentCode: '',
          admissionType: 'INPATIENT',
          notes: '',
          attendingDoctorId: null
        };
        this.filteredAdmissionPatients = [];
        if (this.showAdminAdmissionModal) {
          this.closeAdminAdmissionModal();
        }
        this.isLoading = false;
      },
      error: (err) => {
        // Gestione errore specifico per mancanza posti
        if (err?.status === 409 || err?.error?.message?.includes('bed')) {
          this.paymentsError = 'Nessun posto letto disponibile nel reparto selezionato.';
        } else {
          this.paymentsError = err?.error?.message || 'Impossibile registrare il ricovero.';
        }
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

  // ======================== Cambia Referente ========================

  openChangeReferentModal(admission: AdmissionItem): void {
    this.changeReferentTarget = admission;
    this.changeReferentSelectedDoctorId = admission.attendingDoctorId ?? null;
    this.changeReferentError = '';
    this.changeReferentDoctors = [];

    // Carica i medici con consenso dal paziente
    this.api.get<number[]>('/api/consents/doctors-with-consent', { patientId: admission.patientId }).subscribe({
      next: (doctorIds) => {
        // Filtra i medici dalla lista caricata
        this.changeReferentDoctors = this.doctors.filter(d => doctorIds.includes(d.id));
        this.showChangeReferentModal = true;
      },
      error: () => {
        // Fallback: mostra tutti i medici
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
    this.api.request<AdmissionItem>('PATCH', `/api/admissions/${this.changeReferentTarget.id}`, {
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

  // ======================== Dimissione ========================

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
    if (!this.dischargeTarget) {
      return;
    }
    this.isLoading = true;
    this.api.request<AdmissionItem>('POST', `/api/admissions/${this.dischargeTarget.id}/discharge`, {}).subscribe({
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

  // ======================== Note Ricovero ========================

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
    if (!this.admissionEditNoteTarget) {
      return;
    }
    this.isLoading = true;
    this.api.request<AdmissionItem>('PATCH', `/api/admissions/${this.admissionEditNoteTarget.id}`, {
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
    const departmentCode =
      this.getDoctorById(appointment.doctorId)?.departmentCode ||
      this.currentDoctorDepartment ||
      'CARD';
    const notes = `Proposta da visita ${appointment.id}: ${appointment.reason}. ${this.admissionProposalForm.reason}`;

    // Chiama API backend per creare proposta ricovero
    this.isLoading = true;
    this.api.request<AdmissionItem>('POST', '/api/admin/admissions', {
      patientId: appointment.patientId,
      departmentCode,
      admissionType: 'INPATIENT',
      admittedAt: this.admissionProposalForm.date,
      notes
    }).subscribe({
      next: (created) => {
        this.admissions = [...this.admissions, { ...created, department: departmentCode }];
        this.isLoading = false;
        this.closeAdmissionProposalModal();
      },
      error: () => {
        this.admissionProposalError = 'Impossibile creare la proposta di ricovero.';
        this.isLoading = false;
      }
    });
  }

  openAdminTelevisitModal(): void {
    this.televisitError = '';
    this.showAdminTelevisitModal = true;
  }

  closeAdminTelevisitModal(): void {
    this.showAdminTelevisitModal = false;
    this.televisitError = '';
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

    if (!this.televisitForm.doctorId) {
      return;
    }

    // Carica i pazienti che hanno dato consenso TELEVISIT a questo medico
    this.api.request<number[]>('GET', `/api/consents/patients-with-televisit-consent?doctorId=${this.televisitForm.doctorId}`).subscribe({
      next: (patientIds) => {
        this.televisitPatientsFiltered = this.patients.filter(p => patientIds.includes(p.id));
      },
      error: () => {
        this.televisitPatientsFiltered = [];
      }
    });
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
    const endpoint = this.isAdmin ? '/api/admin/notifications' : '/api/notifications';
    this.api.request<NotificationItem[] | NotificationPage>('GET', endpoint).subscribe({
      next: (notifications) => {
        const rawNotifications = Array.isArray(notifications) ? notifications : (notifications?.content ?? []);
        this.notifications = rawNotifications.map((n) => this.mapNotificationFromBackend(n));
        this.isLoading = false;
      },
      error: () => {
        this.notificationsError = 'Impossibile caricare le notifiche.';
        this.isLoading = false;
      }
    });
  }

  // Mappa NotificationDto backend -> NotificationItem frontend
  private mapNotificationFromBackend(n: NotificationItem): NotificationItem {
    return { ...n, recipient: n.toAddress || n.recipientId, message: n.body || n.message };
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
    // Validazione formato email se canale EMAIL
    if (this.notificationForm.channel === 'EMAIL') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.notificationForm.recipient.trim())) {
        this.notificationsError = 'Inserisci un indirizzo email valido.';
        return;
      }
    }
    this.isLoading = true;
    this.notificationsError = '';
    // Payload allineato a NotificationCreateDto backend
    const notifPayload = {
      recipientType: this.notificationForm.recipientType as 'PATIENT' | 'DOCTOR' | 'ADMIN',
      recipientId: this.notificationForm.recipient,
      channel: this.notificationForm.channel === 'EMAIL' ? 'EMAIL' : 'IN_APP',
      toAddress: this.notificationForm.channel === 'EMAIL' ? this.notificationForm.recipient : undefined,
      subject: this.notificationForm.subject,
      body: this.notificationForm.message
    };
    this.api.request<NotificationItem>('POST', '/api/admin/notifications', notifPayload).subscribe({
      next: (notification) => {
        // Aggiungi in cima alla lista (ordine cronologico inverso)
        this.notifications = [this.mapNotificationFromBackend(notification), ...this.notifications];
        this.resetNotificationForm();
        if (this.showAdminNotificationModal) {
          this.closeAdminNotificationModal();
        }
        this.isLoading = false;
      },
      error: (err) => {
        const errorMsg = err?.error?.message || err?.error?.error || 'Impossibile inviare la notifica.';
        this.notificationsError = errorMsg;
        this.isLoading = false;
      }
    });
  }

  private resetNotificationForm(): void {
    this.notificationForm = {
      recipient: '',
      channel: 'EMAIL',
      recipientType: 'PATIENT',
      subject: '',
      message: '',
      notes: ''
    };
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
    this.resetNotificationForm();
    this.showAdminNotificationModal = true;
  }

  closeAdminNotificationModal(): void {
    this.showAdminNotificationModal = false;
    this.notificationsError = '';
  }

  openNotificationDetailModal(notification: NotificationItem): void {
    this.selectedNotification = notification;
    this.showNotificationDetailModal = true;
  }

  closeNotificationDetailModal(): void {
    this.showNotificationDetailModal = false;
    this.selectedNotification = null;
  }

  openAdminPaymentModal(): void {
    this.paymentsError = '';
    this.paymentsSuccess = '';
    this.paymentForm.doctorId = null;
    this.paymentForm.patientId = null;
    this.paymentForm.service = '';
    this.paymentForm.amount = 0;
    this.paymentForm.performedAt = '';
    this.filteredPatientsForPayment = [];
    this.showAdminPaymentModal = true;
  }

  onPaymentDoctorChange(): void {
    this.paymentForm.patientId = null;
    this.filteredPatientsForPayment = [];
    if (!this.paymentForm.doctorId) {
      return;
    }
    this.api.request<number[]>('GET', `/api/consents/patients-with-consent?scope=RECORDS&doctorId=${this.paymentForm.doctorId}`).subscribe({
      next: (patientIds) => {
        this.filteredPatientsForPayment = this.patients.filter(p => patientIds.includes(p.id));
      },
      error: () => {
        this.paymentsError = 'Impossibile caricare i pazienti per questo medico.';
      }
    });
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
    // Chiama API per cancellare il ricovero
    this.isLoading = true;
    this.api.request<AdmissionItem>('PATCH', `/api/admissions/${this.rejectAdmissionTarget.id}`, {
      status: 'CANCELLED',
      notes: this.rejectAdmissionReason
    }).subscribe({
      next: (updated) => {
        this.admissions = this.admissions.map((item) =>
          item.id === this.rejectAdmissionTarget?.id
            ? { ...item, status: 'CANCELLED', notes: this.rejectAdmissionReason, department: updated.departmentCode }
            : item
        );
        this.isLoading = false;
        this.closeAdmissionRejectModal();
      },
      error: () => {
        this.rescheduleError = 'Impossibile annullare il ricovero.';
        this.isLoading = false;
      }
    });
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
    // Backend: aggiorna data ricovero tramite API (se disponibile) o localmente con status CANCELLED + nuovo ricovero
    const admissionId = this.rescheduleAdmission.id;
    this.isLoading = true;
    // Per ora cancelliamo il ricovero esistente (backend usa CANCELLED, non RESCHEDULED)
    this.api.request<void>('DELETE', `/api/admin/admissions/${admissionId}`).subscribe({
      next: () => {
        this.admissions = this.admissions.map((item) =>
          item.id === admissionId ? { ...item, status: 'CANCELLED', notes: `Ripianificato: ${this.rescheduleForm.reason}` } : item
        );
        this.rescheduleSuccess = 'Richiesta di ripianificazione inviata.';
        this.isLoading = false;
        this.closeAdmissionRescheduleModal();
      },
      error: () => {
        this.rescheduleError = 'Impossibile ripianificare il ricovero.';
        this.isLoading = false;
      }
    });
  }

  confirmAdmission(admission: AdmissionItem): void {
    // Backend usa ACTIVE per ricovero confermato/attivo
    if (admission.status === 'ACTIVE' || admission.status === 'DISCHARGED') {
      return;
    }
    this.isLoading = true;
    // Aggiorna status via API backend
    this.api.request<AdmissionItem>('PUT', `/api/admin/admissions/${admission.id}`, { ...admission, status: 'ACTIVE' }).subscribe({
      next: (updated) => {
        this.admissions = this.admissions.map((item) => item.id === updated.id ? updated : item);
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = 'Impossibile confermare il ricovero.';
        this.isLoading = false;
      }
    });
  }

  rejectAdmission(admission: AdmissionItem): void {
    // Backend usa CANCELLED per ricovero rifiutato
    if (admission.status === 'CANCELLED') {
      return;
    }
    this.openAdmissionRejectModal(admission);
  }

  markPaymentAsPaid(payment: PaymentItem): void {
    // Backend usa CREATED per pagamento in attesa, CAPTURED per pagato
    if (payment.status !== 'CREATED') {
      return;
    }
    this.confirmAdminPayment(payment); // Usa l'API backend già implementata
  }

  openPaymentReceiptUpload(payment: PaymentItem, input: HTMLInputElement): void {
    if (!this.canAttachPaymentReceipt(payment)) {
      return;
    }
    input.click();
  }

  // OBSOLETO: receiptName non esiste più nel backend PaymentOrderDto
  onPaymentReceiptSelected(_event: Event, _payment: PaymentItem): void {
    this.paymentsError = 'Funzionalità non più disponibile.';
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
    // Backend usa CREATED per pagamento da confermare, CAPTURED per confermato
    if (payment.status !== 'CREATED') {
      return;
    }
    this.confirmAdminPayment(payment); // Usa l'API backend
  }

  // Admin Payments - Filtri e gestione
  get filteredPayments(): PaymentItem[] {
    let result = this.payments;

    // Filtro per stato
    if (this.paymentStatusFilter !== 'ALL') {
      result = result.filter((p) => p.status === this.paymentStatusFilter);
    }

    // Filtro per giorni di ritardo (usa createdAt invece di appointmentDate)
    if (this.paymentDaysFilter) {
      const now = new Date();
      result = result.filter((p) => {
        if (!p.createdAt) return false;
        const createdDate = new Date(p.createdAt);
        const diffDays = Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24));
        return diffDays > this.paymentDaysFilter!;
      });
    }

    return result;
  }

  getPaymentDaysOverdue(payment: PaymentItem): number {
    // Usa createdAt invece di appointmentDate (non presente in PaymentOrderDto backend)
    if (!payment.createdAt) return 0;
    const now = new Date();
    const createdDate = new Date(payment.createdAt);
    return Math.max(0, Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24)));
  }

  togglePaymentSelection(paymentId: number): void {
    if (this.selectedPaymentIds.has(paymentId)) {
      this.selectedPaymentIds.delete(paymentId);
    } else {
      this.selectedPaymentIds.add(paymentId);
    }
  }

  toggleAllPaymentsSelection(): void {
    const pendingPayments = this.filteredPayments.filter((p) => p.status === 'CREATED');
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

  get hasSelectedPayments(): boolean {
    return this.selectedPaymentIds.size > 0;
  }

  get areAllPaymentsSelected(): boolean {
    const pendingPayments = this.payments.filter((p) => p.status === 'CREATED');
    return pendingPayments.length > 0 && this.selectedPaymentIds.size === pendingPayments.length;
  }

  get notificationsSentCount(): number {
    return this.notifications.filter((n) => n.status === 'SENT').length;
  }

  get notificationsPendingCount(): number {
    return this.notifications.filter((n) => n.status === 'PENDING').length;
  }

  get notificationsFailedCount(): number {
    return this.notifications.filter((n) => n.status === 'FAILED').length;
  }

  get paymentsCapturedCount(): number {
    return this.payments.filter((p) => p.status === 'CAPTURED').length;
  }

  get paymentsPendingCount(): number {
    return this.payments.filter((p) => p.status === 'CREATED').length;
  }

  get paymentsFailedCount(): number {
    return this.payments.filter((p) => p.status === 'FAILED' || p.status === 'CANCELLED').length;
  }

  getPaymentMethodLabel(method: string): string {
    const labels: Record<string, string> = {
      CARD: 'Carta',
      BANK_TRANSFER: 'Bonifico',
      CASH: 'Contanti'
    };
    return labels[method] ?? method;
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
    const paymentIds = Array.from(this.selectedPaymentIds);
    const count = paymentIds.length;
    if (count === 0) return;

    this.isLoading = true;
    this.paymentsError = '';
    let completed = 0;
    let errors = 0;

    // Invia solleciti via API backend per ogni pagamento selezionato
    paymentIds.forEach((id) => {
      this.api.request<void>('POST', `/api/admin/payments/${id}/reminder`).subscribe({
        next: () => {
          completed++;
          // Aggiorna tracciamento locale solleciti
          this.payments = this.payments.map((p) =>
            p.id === id ? { ...p, notificationAttempts: [...(p.notificationAttempts || []), { sentAt: new Date().toISOString() }] } : p
          );
          if (completed + errors === count) this.finalizeBulkReminders(completed, errors);
        },
        error: () => {
          errors++;
          if (completed + errors === count) this.finalizeBulkReminders(completed, errors);
        }
      });
    });
  }

  private finalizeBulkReminders(completed: number, errors: number): void {
    this.isLoading = false;
    if (errors === 0) {
      this.paymentsSuccess = `Solleciti inviati con successo a ${completed} pazienti.`;
    } else if (completed === 0) {
      this.paymentsError = `Impossibile inviare i solleciti. Errori: ${errors}.`;
    } else {
      this.paymentsSuccess = `Solleciti inviati: ${completed}. Errori: ${errors}.`;
    }
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

    const paymentId = this.alternativePaymentTarget.id;
    const methodLabel = this.alternativePaymentForm.method === 'BONIFICO' ? 'bonifico bancario' : 'pagamento in sede';

    this.isLoading = true;
    this.paymentsError = '';

    // Invia sollecito via API backend con metodo alternativo indicato nelle note
    this.api.request<void>('POST', `/api/admin/payments/${paymentId}/reminder`).subscribe({
      next: () => {
        // Aggiorna tracciamento locale solleciti
        this.payments = this.payments.map((p) =>
          p.id === paymentId ? { ...p, notificationAttempts: [...(p.notificationAttempts || []), { sentAt: new Date().toISOString() }] } : p
        );
        this.paymentsSuccess = `Sollecito con istruzioni per ${methodLabel} inviato al paziente.`;
        this.isLoading = false;
        this.closeAlternativePaymentModal();
      },
      error: () => {
        this.paymentsError = `Impossibile inviare il sollecito per il pagamento #${paymentId}.`;
        this.isLoading = false;
      }
    });
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
    const newScheduledAt = `${this.televisitRescheduleDate}T${this.televisitRescheduleTime}:00`;
    this.isLoading = true;
    this.api.request<TelevisitItem>('PATCH', `/api/televisits/${this.selectedTelevisit.id}`, {
      scheduledAt: newScheduledAt
    }).subscribe({
      next: (updated) => {
        this.televisits = this.televisits.map(tv =>
          tv.id === this.selectedTelevisit?.id
            ? { ...tv, ...updated, scheduledAt: updated.scheduledAt || newScheduledAt }
            : tv
        );
        this.isLoading = false;
        this.closeTelevisitRescheduleModal();
      },
      error: () => {
        this.televisitError = 'Impossibile ripianificare la televisita.';
        this.isLoading = false;
      }
    });
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

  // OBSOLETO: receiptName non esiste più nel backend PaymentOrderDto
  viewPaymentReceipt(_payment: PaymentItem): void {
    this.paymentsError = 'Funzionalità non più disponibile.';
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
    this.paymentForm.amount = pendingPayment.amount ?? 0;
    this.paymentForm.service = pendingPayment.service ?? '';
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
        // televisitForm non ha patientId - non assegnare
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

    // Validazione campi richiesti dal backend TelevisitCreateDto
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

    // Converti ID in nomi per il backend (TelevisitCreateDto usa subject come nome)
    const doctor = this.doctors.find(d => d.id === this.televisitForm.doctorId);
    const patient = this.televisitPatientsFiltered.find(p => p.id === this.televisitForm.patientId);

    if (!doctor || !patient) {
      this.televisitError = 'Medico o paziente non trovato.';
      this.isLoading = false;
      return;
    }

    // Payload allineato a TelevisitCreateDto backend
    const payload = {
      doctorSubject: `${doctor.firstName} ${doctor.lastName}`,
      patientSubject: `${patient.firstName} ${patient.lastName}`,
      department: this.televisitForm.department,
      scheduledAt: new Date(this.televisitForm.scheduledAt).toISOString()
    };

    this.api.request<TelevisitItem>('POST', '/api/admin/televisits', payload).subscribe({
      next: (televisit) => {
        // Mappa il risultato per compatibilità template
        const mappedTelevisit: TelevisitItem = {
          ...televisit,
          provider: 'LIVEKIT',
          token: televisit.roomName
        };
        this.televisits = [...this.televisits, mappedTelevisit];
        // Reset form
        this.televisitForm = {
          doctorId: null,
          patientId: null,
          facilityCode: '',
          department: '',
          scheduledAt: ''
        };
        this.televisitPatientsFiltered = [];
        if (this.showAdminTelevisitModal) {
          this.closeAdminTelevisitModal();
        } else {
          this.closeTelevisitModal();
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.televisitError = err?.error?.message || 'Impossibile creare la sessione.';
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
    this.auditDetailedEvents = [];
    this.auditFilteredEvents = [];
    this.auditSearchText = '';
    this.auditSelectedEvent = null;
    this.auditError = '';
    // Carica automaticamente tutti gli eventi
    this.loadAllAuditEvents();
  }

  /**
   * Carica tutti gli eventi di audit.
   */
  loadAllAuditEvents(): void {
    this.isLoading = true;
    this.auditError = '';

    this.api.get<SpringPage<AuditEventResponse>>('/api/audit/events', { size: '500' }).subscribe({
      next: (response) => {
        this.auditDetailedEvents = this.mapAuditEventsToDetailSimple(response.content);
        this.filterAuditByText();
        this.isLoading = false;
      },
      error: (err) => {
        if (err.status === 403) {
          this.auditError = 'Non hai i permessi per accedere ai dati di audit.';
        } else {
          this.auditError = 'Errore durante il caricamento degli eventi audit.';
        }
        this.isLoading = false;
      }
    });
  }

  /**
   * Filtra gli eventi audit in base al testo di ricerca full-text.
   */
  filterAuditByText(): void {
    if (!this.auditSearchText.trim()) {
      this.auditFilteredEvents = [...this.auditDetailedEvents];
    } else {
      const searchLower = this.auditSearchText.toLowerCase();
      this.auditFilteredEvents = this.auditDetailedEvents.filter(e =>
        e.action.toLowerCase().includes(searchLower) ||
        e.actorId.toLowerCase().includes(searchLower) ||
        e.actorName.toLowerCase().includes(searchLower) ||
        (e.actorType?.toLowerCase().includes(searchLower)) ||
        (e.resourceType?.toLowerCase().includes(searchLower)) ||
        (e.resourceId?.toLowerCase().includes(searchLower)) ||
        e.outcome.toLowerCase().includes(searchLower) ||
        (e.serviceName?.toLowerCase().includes(searchLower))
      );
    }
    // Reset pagination to page 1 when filter changes
    this.setPage('auditFiltered', 1, this.auditFilteredEvents.length);
  }

  private mapAuditEventsToDetailSimple(events: AuditEventResponse[]): AuditEventDetail[] {
    return events.map((event) => ({
      id: event.id,
      eventType: this.mapActionToEventType(event.action),
      action: event.action,
      actorType: (event.actorType as 'USER' | 'SERVICE' | 'SYSTEM' | 'ANONYMOUS') || 'SYSTEM',
      actorId: event.actorId || 'system',
      actorName: event.actorId || 'system',
      subjectType: this.mapResourceToSubjectType(event.resourceType),
      subjectId: event.resourceId || `EVT-${event.id}`,
      resourceType: event.resourceType,
      resourceId: event.resourceId,
      resourceName: event.resourceType || '-',
      serviceName: event.source || 'unknown',
      ipAddress: event.ip,
      outcome: (event.outcome as 'SUCCESS' | 'DENIED' | 'FAILURE') || 'SUCCESS',
      occurredAt: event.occurredAt,
      integrityHash: event.traceId || this.generateHash()
    }));
  }

  private generateHash(): string { const chars = 'abcdef0123456789'; let hash = ''; for (let i = 0; i < 64; i++) { hash += chars.charAt(Math.floor(Math.random() * chars.length)); } return hash; }

  private mapActionToEventType(action: string): AuditEventDetail['eventType'] {
    const a = action.toLowerCase();
    if (a.includes('consent') && a.includes('grant')) return 'CONSENT_GRANTED';
    if (a.includes('consent') && a.includes('revok')) return 'CONSENT_REVOKED';
    if (a.includes('document') || a.includes('view') || a.includes('read')) return 'DOCUMENT_ACCESS';
    if (a.includes('prescription')) return 'PRESCRIPTION_VIEW';
    if (a.includes('appointment') && a.includes('book')) return 'APPOINTMENT_BOOKED';
    if (a.includes('appointment') && a.includes('cancel')) return 'APPOINTMENT_CANCELLED';
    return 'PROFILE_UPDATE';
  }

  private mapResourceToSubjectType(resourceType?: string): AuditEventDetail['subjectType'] {
    if (!resourceType) return 'DOCUMENT';
    const rt = resourceType.toUpperCase();
    if (rt.includes('PATIENT')) return 'PATIENT';
    if (rt.includes('DOCTOR')) return 'DOCTOR';
    if (rt.includes('DOCUMENT')) return 'DOCUMENT';
    if (rt.includes('PRESCRIPTION')) return 'PRESCRIPTION';
    if (rt.includes('APPOINTMENT')) return 'APPOINTMENT';
    if (rt.includes('CONSENT')) return 'CONSENT';
    return 'DOCUMENT';
  }

  selectAuditEvent(event: AuditEventDetail): void {
    this.auditSelectedEvent = event;
    // Focus the modal after it renders for ESC key support
    setTimeout(() => {
      const modal = document.querySelector('.modal[tabindex="-1"]') as HTMLElement;
      if (modal) modal.focus();
    }, 50);
  }
  closeAuditEventDetail(): void { this.auditSelectedEvent = null; }
  getOutcomeLabel(outcome: string): string { return this.auditOutcomeOptions.find(o => o.value === outcome)?.label ?? outcome; }
  getOutcomeBadgeClass(outcome: string): string { switch (outcome) { case 'SUCCESS': return 'bg-success'; case 'DENIED': return 'bg-danger'; case 'FAILURE': return 'bg-warning text-dark'; default: return 'bg-secondary'; } }
  formatAuditDate(isoDate: string): string { return new Date(isoDate).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' }); }

  loadAdminTelevisit(): void {
    this.televisitError = '';
    this.isLoading = true;
    // Carica dati di supporto
    this.loadPatients();
    this.loadDoctors();
    this.loadFacilities();
    this.loadDepartments();
    // Carica televisite da API
    this.api.request<PagedResponse<TelevisitItem>>('GET', '/api/televisits').subscribe({
      next: (response) => {
        this.televisits = (response.content ?? []).map(tv => ({
          ...tv,
          provider: 'LIVEKIT',
          token: tv.roomName
        }));
        this.isLoading = false;
      },
      error: () => {
        this.televisitError = 'Impossibile caricare le televisite.';
        this.isLoading = false;
      }
    });
  }

  loadAdminAdmissions(): void {
    this.paymentsError = '';
    this.isLoading = true;
    // Carica dati di supporto
    this.loadPatients();
    this.loadDoctors();
    this.loadFacilities();
    this.loadDepartments();
    // Carica ricoveri da API
    this.api.request<PagedResponse<AdmissionItem>>('GET', '/api/admissions').subscribe({
      next: (response) => {
        this.admissions = (response.content ?? []).map(adm => ({
          ...adm,
          department: adm.departmentCode // mapping per compatibilità template
        }));
        this.isLoading = false;
      },
      error: () => {
        this.paymentsError = 'Impossibile caricare i ricoveri.';
        this.isLoading = false;
      }
    });
    // Carica anche capacità reparti
    this.loadDepartmentCapacity();
  }

  private loadDepartmentCapacity(): void {
    this.api.request<CapacityItem[]>('GET', '/api/departments/capacity').subscribe({
      next: (capacities) => {
        this.departmentCapacities = capacities;
      },
      error: () => {
        // Silently fail - non critico
      }
    });
  }

  // ===== STATISTICHE TELEVISITE =====

  /** Televisite programmate per oggi */
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

  /** Televisite concluse (status ENDED) */
  get televisiteConcluse(): number {
    return this.televisits.filter(tv => tv.status === 'ENDED').length;
  }

  /** Televisite in attesa paziente (status ACTIVE - il medico è entrato ma il paziente no) */
  get televisiteInAttesaPaziente(): number {
    return this.televisits.filter(tv => tv.status === 'ACTIVE').length;
  }

  // ===== STATISTICHE RICOVERI =====

  /** Ricoveri attivi (status ACTIVE) */
  get ricoveriAttivi(): number {
    return this.admissions.filter(adm => adm.status === 'ACTIVE').length;
  }

  /** Ricoveri in Pneumologia (reparto PNEUMO, status ACTIVE) */
  get ricoveriPneumologia(): number {
    return this.admissions.filter(adm => adm.departmentCode === 'PNEUMO' && adm.status === 'ACTIVE').length;
  }

  /** Ricoveri confermati (INPATIENT attivi) */
  get ricoveriConfermati(): number {
    return this.admissions.filter(adm => adm.admissionType === 'INPATIENT' && adm.status === 'ACTIVE').length;
  }

  /** Dimessi questo mese */
  get dimessiQuestoMese(): number {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

    return this.admissions.filter(adm => {
      if (adm.status !== 'DISCHARGED' || !adm.dischargedAt) return false;
      const discharged = new Date(adm.dischargedAt);
      return discharged >= startOfMonth;
    }).length;
  }

  private loadFacilities(): void {
    this.api.request<FacilityItem[]>('GET', '/api/facilities').subscribe({
      next: (facilities) => {
        if (facilities.length > 0) {
          this.facilities = facilities;
        } else {
          this.facilities = this.getMockFacilities();
        }
      },
      error: () => {
        this.facilities = this.getMockFacilities();
      }
    });
  }

  private getMockFacilities(): FacilityItem[] {
    return [
      { code: 'HQ', name: 'Sede Centrale' },
      { code: 'NORD', name: 'Presidio Nord' },
      { code: 'SUD', name: 'Presidio Sud' }
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

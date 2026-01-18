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
  uploadedAt: string;
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
  status: string;
  paidAt: string;
}

interface AdmissionItem {
  id: number;
  patientId: number;
  department: string;
  bedId: number;
  status: string;
  admittedAt: string;
}

interface NotificationItem {
  id: number;
  recipient: string;
  channel: string;
  message: string;
  status: string;
}

interface PrescriptionItem {
  id: number;
  patientId: number;
  drug: string;
  dosage: string;
  status: string;
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
  mode:
    | 'api'
    | 'scheduling'
    | 'docs'
    | 'payments'
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
    reason: ''
  };
  slotForm = {
    date: '',
    time: ''
  };
  documents: DocumentItem[] = [];
  consents: ConsentItem[] = [];
  docsError = '';
  docForm = {
    patientId: null as number | null,
    type: 'REFERT',
    name: ''
  };
  consentForm = {
    consentType: 'GDPR',
    accepted: true
  };
  payments: PaymentItem[] = [];
  admissions: AdmissionItem[] = [];
  paymentsError = '';
  paymentForm = {
    patientId: 1,
    amount: 120,
    currency: 'EUR'
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
    message: ''
  };
  notificationPreferences = {
    email: 'anna.conti@sanitech.example',
    phone: '+39 347 123 4567',
    channels: {
      email: true,
      sms: false,
      app: true
    },
    types: {
      appointments: true,
      documents: true,
      payments: false
    }
  };
  prescriptions: PrescriptionItem[] = [];
  prescribingError = '';
  prescriptionForm = {
    patientId: 1,
    drug: '',
    dosage: ''
  };
  televisits: TelevisitItem[] = [];
  televisitError = '';
  televisitForm = {
    appointmentId: 1,
    patientId: null as number | null,
    provider: 'LIVEKIT'
  };
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
    }
    if (this.mode === 'notifications') {
      this.loadNotifications();
    }
    if (this.mode === 'prescribing') {
      this.loadPrescriptions();
      this.loadPatients();
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
  }

  submitBooking(): void {
    if (!this.bookingForm.slotId) {
      this.schedulingError = 'Seleziona uno slot disponibile.';
      return;
    }
    if (!this.bookingForm.reason.trim()) {
      this.schedulingError = 'Inserisci il motivo della visita.';
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
        this.bookingForm.reason = '';
        this.bookingForm.slotId = null;
        this.isLoading = false;
      },
      error: () => {
        this.schedulingError = 'Impossibile registrare la prenotazione.';
        this.isLoading = false;
      }
    });
  }

  getDoctorLabel(doctorId: number): string {
    const doctor = this.doctors.find((item) => item.id === doctorId);
    return doctor ? `${doctor.firstName} ${doctor.lastName}` : `Medico ${doctorId}`;
  }

  getSlotById(slotId: number): SchedulingSlot | undefined {
    return this.slots.find((slot) => slot.id === slotId);
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

  getDocumentTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      REFERT: 'Referto',
      REPORT: 'Referto',
      CERTIFICATE: 'Certificato'
    };
    return labels[type] ?? type;
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
      ACTIVE: 'Attivo'
    };
    return labels[status] ?? status;
  }

  get availableSlots(): SchedulingSlot[] {
    return this.slots.filter((slot) => slot.status === 'AVAILABLE');
  }

  get visibleSlots(): SchedulingSlot[] {
    if (this.isDoctor) {
      return this.slots.filter((slot) => slot.doctorId === this.currentDoctorId);
    }
    return this.slots;
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
      ACTIVE: 'Attiva',
      SUSPENDED: 'Sospesa',
      COMPLETED: 'Conclusa',
      PENDING: 'In attesa'
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

  getSlotLabel(slot: SchedulingSlot): string {
    return `${this.formatDate(slot.date)} • ${slot.time} • ${this.getDoctorLabel(slot.doctorId)}`;
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
      status: 'AVAILABLE'
    }).subscribe({
      next: (slot) => {
        this.slots = [...this.slots, slot];
        this.slotForm.date = '';
        this.slotForm.time = '';
        this.isLoading = false;
      },
      error: () => {
        this.schedulingError = 'Impossibile creare lo slot.';
        this.isLoading = false;
      }
    });
  }

  confirmAppointment(appointment: SchedulingAppointment): void {
    if (appointment.status === 'CONFIRMED') {
      return;
    }
    this.appointments = this.appointments.map((item) =>
      item.id === appointment.id ? { ...item, status: 'CONFIRMED' } : item
    );
  }

  loadDocs(): void {
    this.isLoading = true;
    this.docsError = '';
    this.api.request<DocumentItem[]>('GET', '/api/docs').subscribe({
      next: (docs) => {
        this.documents = docs;
        this.isLoading = false;
      },
      error: () => {
        this.docsError = 'Impossibile caricare i documenti.';
        this.isLoading = false;
      }
    });
    this.api.request<ConsentItem[]>('GET', '/api/consents').subscribe({
      next: (consents) => {
        this.consents = consents;
      },
      error: () => {
        this.docsError = 'Impossibile caricare i consensi.';
      }
    });
  }

  submitDocument(): void {
    if (!this.docForm.name.trim()) {
      this.docsError = 'Inserisci il nome del documento.';
      return;
    }
    this.isLoading = true;
    this.docsError = '';
    const payload: Record<string, unknown> = {
      type: this.docForm.type,
      name: this.docForm.name
    };
    if (this.isDoctor) {
      payload['patientId'] = this.docForm.patientId ?? this.patients[0]?.id ?? 1;
    }
    this.api.request<DocumentItem>('POST', '/api/docs', payload).subscribe({
      next: (doc) => {
        this.documents = [...this.documents, doc];
        this.docForm.name = '';
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
    this.api.request<ConsentItem>('POST', '/api/consents', {
      consentType: this.consentForm.consentType,
      accepted: this.consentForm.accepted
    }).subscribe({
      next: (consent) => {
        this.consents = [...this.consents, consent];
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
    if (!this.paymentForm.amount) {
      this.paymentsError = 'Inserisci l’importo del pagamento.';
      return;
    }
    this.isLoading = true;
    this.paymentsError = '';
    this.api.request<PaymentItem>('POST', '/api/payments', {
      patientId: this.paymentForm.patientId,
      amount: this.paymentForm.amount,
      currency: this.paymentForm.currency
    }).subscribe({
      next: (payment) => {
        this.payments = [...this.payments, payment];
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
    if (!this.notificationForm.recipient.trim() || !this.notificationForm.message.trim()) {
      this.notificationsError = 'Inserisci destinatario e messaggio.';
      return;
    }
    this.isLoading = true;
    this.notificationsError = '';
    this.api.request<NotificationItem>('POST', '/api/notifications', {
      recipient: this.notificationForm.recipient,
      channel: this.notificationForm.channel,
      message: this.notificationForm.message
    }).subscribe({
      next: (notification) => {
        this.notifications = [...this.notifications, notification];
        this.notificationForm.message = '';
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
    if (!this.notificationPreferences.email.trim()) {
      this.notificationsError = 'Inserisci una email valida per ricevere le notifiche.';
      return;
    }
    if (!this.notificationPreferences.phone.trim()) {
      this.notificationsError = 'Inserisci un numero di telefono valido per ricevere le notifiche.';
      return;
    }
    const hasChannel = Object.values(this.notificationPreferences.channels).some((value) => value);
    if (!hasChannel) {
      this.notificationsError = 'Seleziona almeno un canale di notifica.';
      return;
    }
    const hasType = Object.values(this.notificationPreferences.types).some((value) => value);
    if (!hasType) {
      this.notificationsError = 'Seleziona almeno un tipo di notifica.';
      return;
    }
    this.notificationsSuccess = 'Preferenze notifiche aggiornate.';
  }

  confirmAdmission(admission: AdmissionItem): void {
    if (admission.status === 'CONFIRMED') {
      return;
    }
    this.admissions = this.admissions.map((item) =>
      item.id === admission.id ? { ...item, status: 'CONFIRMED' } : item
    );
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
    this.isLoading = true;
    this.prescribingError = '';
    this.api.request<PrescriptionItem>('POST', '/api/prescribing/prescriptions', {
      patientId: this.prescriptionForm.patientId,
      drug: this.prescriptionForm.drug,
      dosage: this.prescriptionForm.dosage
    }).subscribe({
      next: (prescription) => {
        this.prescriptions = [...this.prescriptions, prescription];
        this.prescriptionForm.drug = '';
        this.prescriptionForm.dosage = '';
        this.isLoading = false;
      },
      error: () => {
        this.prescribingError = 'Impossibile registrare la prescrizione.';
        this.isLoading = false;
      }
    });
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

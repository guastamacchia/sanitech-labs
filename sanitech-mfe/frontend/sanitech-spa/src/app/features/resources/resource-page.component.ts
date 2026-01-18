import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

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
  mode: 'api' | 'scheduling' | 'docs' | 'payments' | 'notifications' = 'api';
  slots: SchedulingSlot[] = [];
  appointments: SchedulingAppointment[] = [];
  schedulingError = '';
  bookingForm = {
    patientId: 1,
    doctorId: 2,
    slotId: 1,
    reason: ''
  };
  documents: DocumentItem[] = [];
  consents: ConsentItem[] = [];
  docsError = '';
  docForm = {
    patientId: 1,
    type: 'REFERT',
    name: ''
  };
  consentForm = {
    patientId: 1,
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
  notificationForm = {
    recipient: '',
    channel: 'EMAIL',
    message: ''
  };

  constructor(private route: ActivatedRoute, private api: ApiService) {
    const data = this.route.snapshot.data;
    this.title = data['title'] as string;
    this.description = data['description'] as string;
    this.mode = (data['view'] as 'api' | 'scheduling' | 'docs' | 'payments' | 'notifications') ?? 'api';
    this.endpoints = (data['endpoints'] as ResourceEndpoint[]) ?? [];
    this.selectedEndpoint = this.endpoints[0];
    this.payload = this.selectedEndpoint?.payload ?? '';
    if (this.mode === 'scheduling') {
      this.loadScheduling();
    }
    if (this.mode === 'docs') {
      this.loadDocs();
    }
    if (this.mode === 'payments') {
      this.loadPayments();
    }
    if (this.mode === 'notifications') {
      this.loadNotifications();
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
  }

  submitBooking(): void {
    if (!this.bookingForm.reason.trim()) {
      this.schedulingError = 'Inserisci il motivo della visita.';
      return;
    }
    this.isLoading = true;
    this.schedulingError = '';
    this.api.request<SchedulingAppointment>('POST', '/api/appointments', {
      patientId: this.bookingForm.patientId,
      doctorId: this.bookingForm.doctorId,
      slotId: this.bookingForm.slotId,
      reason: this.bookingForm.reason,
      status: 'PENDING'
    }).subscribe({
      next: (appointment) => {
        this.appointments = [...this.appointments, appointment];
        this.bookingForm.reason = '';
        this.isLoading = false;
      },
      error: () => {
        this.schedulingError = 'Impossibile registrare la prenotazione.';
        this.isLoading = false;
      }
    });
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
    this.api.request<DocumentItem>('POST', '/api/docs', {
      patientId: this.docForm.patientId,
      type: this.docForm.type,
      name: this.docForm.name
    }).subscribe({
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
      patientId: this.consentForm.patientId,
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
}

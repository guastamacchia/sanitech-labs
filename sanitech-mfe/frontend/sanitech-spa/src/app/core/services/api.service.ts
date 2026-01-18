import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, of, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private mockStore = {
    slots: [
      {
        id: 1,
        doctorId: 2,
        date: '2024-05-01',
        time: '09:30',
        status: 'AVAILABLE',
        modality: 'IN_PERSON',
        notes: 'Visite cardiologiche di controllo.'
      },
      {
        id: 2,
        doctorId: 2,
        date: '2024-05-01',
        time: '10:30',
        status: 'AVAILABLE',
        modality: 'REMOTE',
        notes: 'Disponibile anche per consulto da remoto.'
      },
      {
        id: 3,
        doctorId: 3,
        date: '2024-05-02',
        time: '11:00',
        status: 'AVAILABLE',
        modality: 'IN_PERSON',
        notes: 'Slot riservato visite neurologiche.'
      }
    ],
    appointments: [
      { id: 10, patientId: 1, doctorId: 2, slotId: 3, reason: 'Controllo cardiologico', status: 'CONFIRMED' }
    ],
    docs: [
      { id: 15, patientId: 1, type: 'REFERT', name: 'Referto cardiologia 2024', uploadedAt: '2024-04-01' }
    ],
    consents: [
      { id: 21, patientId: 1, consentType: 'GDPR', accepted: true, signedAt: '2024-03-20' }
    ],
    notifications: [
      {
        id: 30,
        recipient: 'anna.conti@sanitech.example',
        channel: 'EMAIL',
        message: 'Promemoria visita cardiologica del 12/05/2024 alle 09:30.',
        status: 'DELIVERED'
      },
      {
        id: 31,
        recipient: 'anna.conti@sanitech.example',
        channel: 'SMS',
        message: 'È disponibile un nuovo referto nella tua area documenti.',
        status: 'SENT'
      },
      {
        id: 32,
        recipient: 'anna.conti@sanitech.example',
        channel: 'APP',
        message: 'Pagamento registrato con successo. Grazie!',
        status: 'FAILED'
      }
    ],
    payments: [
      { id: 40, patientId: 1, amount: 120, currency: 'EUR', status: 'PAID', paidAt: '2024-03-15' }
    ],
    admissions: [
      { id: 50, patientId: 1, department: 'CARD', bedId: 4, status: 'ACTIVE', admittedAt: '2024-03-10' }
    ],
    beds: [
      { id: 4, department: 'CARD', status: 'OCCUPIED' },
      { id: 7, department: 'NEURO', status: 'AVAILABLE' }
    ],
    prescriptions: [
      { id: 60, patientId: 1, drug: 'Atorvastatina', dosage: '10mg', status: 'ACTIVE' }
    ],
    televisits: [
      { id: 70, appointmentId: 22, provider: 'LIVEKIT', status: 'READY', token: 'tv-abc-123' }
    ],
    doctors: [
      { id: 2, firstName: 'Marco', lastName: 'Bianchi', speciality: 'CARD' },
      { id: 3, firstName: 'Laura', lastName: 'Gatti', speciality: 'NEURO' }
    ],
    patients: [
      { id: 1, firstName: 'Anna', lastName: 'Conti', email: 'anna.conti@sanitech.example' }
    ],
    departments: [
      { code: 'CARD', name: 'Cardiologia' },
      { code: 'NEURO', name: 'Neurologia' }
    ],
    specialities: [
      { code: 'CARD', name: 'Cardiologia' },
      { code: 'NEURO', name: 'Neurologia' }
    ],
    audit: [
      { id: 80, action: 'LOGIN', actor: 'anna.conti@sanitech.example', timestamp: '2024-03-18 09:10' },
      { id: 81, action: 'CREATE_APPOINTMENT', actor: 'Dr. Marco Bianchi', timestamp: '2024-03-18 09:30' }
    ]
  };

  constructor(private http: HttpClient) {}

  request<T>(method: string, path: string, body?: unknown): Observable<T> {
    if (environment.mockApi) {
      return this.handleMockRequest<T>(method, path, body);
    }
    const url = `${environment.gatewayUrl}${path}`;
    return this.http.request<T>(method, url, {
      body
    });
  }

  private handleMockRequest<T>(method: string, path: string, body?: unknown): Observable<T> {
    const normalizedMethod = method.toUpperCase();
    switch (path) {
      case '/api/slots':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.slots as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newSlot = {
            id: this.nextId(this.mockStore.slots),
            doctorId: this.getNumber(payload.doctorId, 1),
            date: this.getString(payload.date, this.today()),
            time: this.getString(payload.time, '09:00'),
            status: this.getString(payload.status, 'AVAILABLE'),
            modality: this.getString(payload.modality, 'IN_PERSON'),
            notes: this.getString(payload.notes, '')
          };
          this.mockStore.slots.push(newSlot);
          return of(newSlot as T);
        }
        break;
      case '/api/appointments':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.appointments as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newAppointment = {
            id: this.nextId(this.mockStore.appointments),
            patientId: this.getNumber(payload.patientId, 1),
            doctorId: this.getNumber(payload.doctorId, 2),
            slotId: this.getNumber(payload.slotId, 1),
            reason: this.getString(payload.reason, 'Visita di controllo'),
            status: this.getString(payload.status, 'PENDING')
          };
          this.mockStore.appointments.push(newAppointment);
          return of(newAppointment as T);
        }
        break;
      case '/api/scheduling':
        if (normalizedMethod === 'GET') {
          return of({
            slots: this.mockStore.slots,
            appointments: this.mockStore.appointments,
            summary: {
              availableSlots: this.mockStore.slots.filter((slot) => slot.status === 'AVAILABLE').length,
              bookedAppointments: this.mockStore.appointments.length
            }
          } as T);
        }
        break;
      case '/api/docs':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.docs as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newDoc = {
            id: this.nextId(this.mockStore.docs),
            patientId: this.getNumber(payload.patientId, 1),
            type: this.getString(payload.type, 'REFERT'),
            name: this.getString(payload.name, 'Documento clinico'),
            uploadedAt: this.today()
          };
          this.mockStore.docs.push(newDoc);
          return of(newDoc as T);
        }
        break;
      case '/api/consents':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.consents as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newConsent = {
            id: this.nextId(this.mockStore.consents),
            patientId: this.getNumber(payload.patientId, 1),
            consentType: this.getString(payload.consentType, 'GDPR'),
            accepted: this.getBoolean(payload.accepted, true),
            signedAt: this.today()
          };
          this.mockStore.consents.push(newConsent);
          return of(newConsent as T);
        }
        break;
      case '/api/notifications':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.notifications as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newNotification = {
            id: this.nextId(this.mockStore.notifications),
            recipient: this.getString(payload.recipient, 'utente@example.com'),
            channel: this.getString(payload.channel, 'EMAIL'),
            message: this.getString(payload.message, 'Notifica di sistema'),
            status: this.getString(payload.status, 'SENT')
          };
          this.mockStore.notifications.push(newNotification);
          return of(newNotification as T);
        }
        break;
      case '/api/payments':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.payments as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newPayment = {
            id: this.nextId(this.mockStore.payments),
            patientId: this.getNumber(payload.patientId, 1),
            amount: this.getNumber(payload.amount, 100),
            currency: this.getString(payload.currency, 'EUR'),
            status: this.getString(payload.status, 'PAID'),
            paidAt: this.today()
          };
          this.mockStore.payments.push(newPayment);
          return of(newPayment as T);
        }
        break;
      case '/api/admissions':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.admissions as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newAdmission = {
            id: this.nextId(this.mockStore.admissions),
            patientId: this.getNumber(payload.patientId, 1),
            department: this.getString(payload.department, 'CARD'),
            bedId: this.getNumber(payload.bedId, 1),
            status: this.getString(payload.status, 'ACTIVE'),
            admittedAt: this.today()
          };
          this.mockStore.admissions.push(newAdmission);
          return of(newAdmission as T);
        }
        break;
      case '/api/beds':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.beds as T);
        }
        break;
      case '/api/prescriptions':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.prescriptions as T);
        }
        break;
      case '/api/prescribing/prescriptions':
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newPrescription = {
            id: this.nextId(this.mockStore.prescriptions),
            patientId: this.getNumber(payload.patientId, 1),
            drug: this.getString(payload.drug, 'Farmaco prescritto'),
            dosage: this.getString(payload.dosage, '10mg'),
            status: this.getString(payload.status, 'ACTIVE')
          };
          this.mockStore.prescriptions.push(newPrescription);
          return of(newPrescription as T);
        }
        break;
      case '/api/televisit':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.televisits as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newTelevisit = {
            id: this.nextId(this.mockStore.televisits),
            appointmentId: this.getNumber(payload.appointmentId, 1),
            provider: this.getString(payload.provider, 'LIVEKIT'),
            status: 'READY',
            token: `tv-${Math.random().toString(36).slice(2, 8)}`,
          };
          this.mockStore.televisits.push(newTelevisit);
          return of(newTelevisit as T);
        }
        break;
      case '/api/doctors':
      case '/api/admin/doctors':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.doctors as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newDoctor = {
            id: this.nextId(this.mockStore.doctors),
            firstName: this.getString(payload.firstName, 'Mario'),
            lastName: this.getString(payload.lastName, 'Rossi'),
            speciality: this.getString(payload.speciality, 'CARD')
          };
          this.mockStore.doctors.push(newDoctor);
          return of(newDoctor as T);
        }
        break;
      case '/api/patients':
      case '/api/admin/patients':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.patients as T);
        }
        if (normalizedMethod === 'POST') {
          const payload = this.ensureBody(body);
          const newPatient = {
            id: this.nextId(this.mockStore.patients),
            firstName: this.getString(payload.firstName, 'Anna'),
            lastName: this.getString(payload.lastName, 'Conti'),
            email: this.getString(payload.email, 'anna.conti@sanitech.example')
          };
          this.mockStore.patients.push(newPatient);
          return of(newPatient as T);
        }
        break;
      case '/api/departments':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.departments as T);
        }
        break;
      case '/api/specialities':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.specialities as T);
        }
        break;
      case '/api/audit':
        if (normalizedMethod === 'GET') {
          return of(this.mockStore.audit as T);
        }
        break;
      default:
        break;
    }
    return throwError(() => new Error(`Mock API: operazione non supportata per ${method} ${path}`));
  }

  private ensureBody(body: unknown): Record<string, unknown> {
    if (!body || typeof body !== 'object') {
      return {};
    }
    return body as Record<string, unknown>;
  }

  private getString(value: unknown, fallback: string): string {
    return typeof value === 'string' && value.trim() ? value : fallback;
  }

  private getNumber(value: unknown, fallback: number): number {
    return typeof value === 'number' && !Number.isNaN(value) ? value : fallback;
  }

  private getBoolean(value: unknown, fallback: boolean): boolean {
    return typeof value === 'boolean' ? value : fallback;
  }

  private nextId<T extends { id: number }>(collection: T[]): number {
    return collection.reduce((max, item) => Math.max(max, item.id), 0) + 1;
  }

  private today(): string {
    return new Date().toISOString().split('T')[0];
  }
}

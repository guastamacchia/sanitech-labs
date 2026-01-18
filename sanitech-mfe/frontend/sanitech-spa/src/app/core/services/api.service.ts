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
      { id: 1, doctorId: 2, date: '2024-05-01', time: '09:30', status: 'AVAILABLE' },
      { id: 2, doctorId: 2, date: '2024-05-01', time: '10:30', status: 'AVAILABLE' },
      { id: 3, doctorId: 3, date: '2024-05-02', time: '11:00', status: 'BOOKED' }
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
      { id: 30, recipient: 'anna.conti@sanitech.example', channel: 'EMAIL', message: 'Promemoria visita', status: 'SENT' }
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
          const newSlot = { id: this.nextId(this.mockStore.slots), status: 'AVAILABLE', ...payload };
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
          const newAppointment = { id: this.nextId(this.mockStore.appointments), status: 'PENDING', ...payload };
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
          const newDoc = { id: this.nextId(this.mockStore.docs), uploadedAt: this.today(), ...payload };
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
          const newConsent = { id: this.nextId(this.mockStore.consents), signedAt: this.today(), ...payload };
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
          const newNotification = { id: this.nextId(this.mockStore.notifications), status: 'SENT', ...payload };
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
          const newPayment = { id: this.nextId(this.mockStore.payments), status: 'PAID', paidAt: this.today(), ...payload };
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
          const newAdmission = { id: this.nextId(this.mockStore.admissions), status: 'ACTIVE', admittedAt: this.today(), ...payload };
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
          const newPrescription = { id: this.nextId(this.mockStore.prescriptions), status: 'ACTIVE', ...payload };
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
            status: 'READY',
            token: `tv-${Math.random().toString(36).slice(2, 8)}`,
            ...payload
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
          const newDoctor = { id: this.nextId(this.mockStore.doctors), ...payload };
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
          const newPatient = { id: this.nextId(this.mockStore.patients), ...payload };
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

  private nextId<T extends { id: number }>(collection: T[]): number {
    return collection.reduce((max, item) => Math.max(max, item.id), 0) + 1;
  }

  private today(): string {
    return new Date().toISOString().split('T')[0];
  }
}

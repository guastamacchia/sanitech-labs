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
      },
      {
        id: 4,
        doctorId: 4,
        date: '2024-05-03',
        time: '14:00',
        status: 'AVAILABLE',
        modality: 'IN_PERSON',
        notes: 'Prima visita dermatologica.'
      },
      {
        id: 5,
        doctorId: 5,
        date: '2024-05-03',
        time: '15:30',
        status: 'AVAILABLE',
        modality: 'REMOTE',
        notes: 'Follow-up ortopedico da remoto.'
      },
      {
        id: 6,
        doctorId: 2,
        date: '2024-05-04',
        time: '09:00',
        status: 'AVAILABLE',
        modality: 'IN_PERSON',
        notes: 'Controllo pressione.'
      },
      {
        id: 7,
        doctorId: 3,
        date: '2024-05-04',
        time: '12:00',
        status: 'AVAILABLE',
        modality: 'REMOTE',
        notes: 'Teleconsulto neurologico.'
      },
      {
        id: 8,
        doctorId: 6,
        date: '2024-05-05',
        time: '10:00',
        status: 'AVAILABLE',
        modality: 'IN_PERSON',
        notes: 'Visita pneumologica.'
      },
      {
        id: 9,
        doctorId: 4,
        date: '2024-05-05',
        time: '16:00',
        status: 'AVAILABLE',
        modality: 'IN_PERSON',
        notes: 'Controllo dermatologico.'
      },
      {
        id: 10,
        doctorId: 5,
        date: '2024-05-06',
        time: '11:30',
        status: 'AVAILABLE',
        modality: 'IN_PERSON',
        notes: 'Visita ortopedica pre-operatoria.'
      }
    ],
    appointments: [
      { id: 10, patientId: 1, doctorId: 2, slotId: 1, reason: 'Controllo cardiologico', status: 'CONFIRMED' },
      { id: 11, patientId: 2, doctorId: 3, slotId: 3, reason: 'Esito esami neurologici', status: 'PENDING' },
      { id: 12, patientId: 3, doctorId: 4, slotId: 4, reason: 'Visita dermatologica', status: 'PENDING' },
      { id: 13, patientId: 4, doctorId: 5, slotId: 5, reason: 'Dolore al ginocchio', status: 'CONFIRMED' }
    ],
    docs: [
      { id: 15, patientId: 1, type: 'REFERT', name: 'Referto cardiologia 2024', notes: 'Controllo annuale.', uploadedAt: '2024-04-01' },
      { id: 16, patientId: 2, type: 'CERTIFICATE', name: 'Certificato neurologico', notes: 'Valido fino al 2025.', uploadedAt: '2024-04-03' },
      { id: 17, patientId: 3, type: 'REFERT', name: 'Referto dermatologia', notes: 'Allergia lieve da monitorare.', uploadedAt: '2024-04-05' },
      { id: 18, patientId: 4, type: 'CERTIFICATE', name: 'Certificato ortopedico', notes: 'Richiesto riposo di 10 giorni.', uploadedAt: '2024-04-06' },
      { id: 19, patientId: 5, type: 'REFERT', name: 'Referto pneumologia', notes: 'Consigliata spirometria.', uploadedAt: '2024-04-08' }
    ],
    consents: [
      { id: 21, patientId: 1, consentType: 'GDPR', accepted: true, signedAt: '2024-03-20' },
      { id: 22, patientId: 2, consentType: 'PRIVACY', accepted: true, signedAt: '2024-03-22' },
      { id: 23, patientId: 3, consentType: 'THERAPY', accepted: false, signedAt: '2024-03-25' },
      { id: 24, patientId: 4, consentType: 'GDPR', accepted: true, signedAt: '2024-03-27' }
    ],
    notifications: [
      {
        id: 30,
        recipient: 'anna.conti@sanitech.example',
        channel: 'EMAIL',
        subject: 'Promemoria visita cardiologica',
        message: 'Promemoria visita cardiologica del 12/05/2024 alle 09:30.',
        notes: 'Presentarsi 10 minuti prima.',
        status: 'DELIVERED',
        sentAt: '2024-05-05T09:00:00'
      },
      {
        id: 31,
        recipient: 'anna.conti@sanitech.example',
        channel: 'SMS',
        subject: 'Nuovo referto disponibile',
        message: 'È disponibile un nuovo referto nella tua area documenti.',
        notes: '',
        status: 'SENT',
        sentAt: '2024-05-04T14:20:00'
      },
      {
        id: 32,
        recipient: 'anna.conti@sanitech.example',
        channel: 'APP',
        subject: 'Pagamento registrato',
        message: 'Pagamento registrato con successo. Grazie!',
        notes: 'Ricevuta disponibile in area pagamenti.',
        status: 'FAILED',
        sentAt: '2024-05-03T18:10:00'
      },
      {
        id: 33,
        recipient: 'giulia.rossi@sanitech.example',
        channel: 'EMAIL',
        subject: 'Referto pronto',
        message: 'Nuovo referto disponibile in cartella clinica.',
        notes: '',
        status: 'SENT',
        sentAt: '2024-05-02T10:35:00'
      },
      {
        id: 34,
        recipient: 'luca.rinaldi@sanitech.example',
        channel: 'SMS',
        subject: 'Follow-up programmato',
        message: 'Ricorda la visita di follow-up del 14/05/2024.',
        notes: 'Confermare la presenza.',
        status: 'DELIVERED',
        sentAt: '2024-05-01T08:45:00'
      }
    ],
    payments: [
      {
        id: 40,
        patientId: 1,
        amount: 120,
        currency: 'EUR',
        service: 'Visita medica con Dott. Marco Bianchi',
        status: 'PAID',
        paidAt: '2024-03-15',
        receiptName: 'ricevuta-visita-marco-bianchi.pdf'
      },
      {
        id: 41,
        patientId: 2,
        amount: 80,
        currency: 'EUR',
        service: 'Televisita neurologica con Dott.ssa Laura Gatti',
        status: 'PAID',
        paidAt: '2024-03-18',
        receiptName: 'ricevuta-televisita-laura-gatti.pdf'
      },
      {
        id: 42,
        patientId: 3,
        amount: 95,
        currency: 'EUR',
        service: 'Visita dermatologica con Dott.ssa Elisa Mori',
        status: 'PENDING',
        paidAt: '2024-03-20'
      },
      {
        id: 43,
        patientId: 4,
        amount: 110,
        currency: 'EUR',
        service: 'Visita ortopedica con Dott. Paolo Serra',
        status: 'PAID',
        paidAt: '2024-03-22',
        receiptName: 'ricevuta-visita-paolo-serra.pdf'
      }
    ],
    admissions: [
      { id: 50, patientId: 1, department: 'CARD', bedId: 4, status: 'ACTIVE', notes: 'Monitoraggio ECG notturno.', admittedAt: '2024-03-10' },
      { id: 51, patientId: 2, department: 'NEURO', bedId: 7, status: 'ACTIVE', notes: 'Osservazione neurologica.', admittedAt: '2024-03-12' },
      { id: 52, patientId: 3, department: 'DERM', bedId: 9, status: 'CONFIRMED', notes: 'Terapia topica in corso.', admittedAt: '2024-03-14' },
      { id: 53, patientId: 4, department: 'ORTHO', bedId: 11, status: 'ACTIVE', notes: 'Controllo post-operatorio.', admittedAt: '2024-03-16' },
      { id: 54, patientId: 5, department: 'PNEUMO', bedId: 12, status: 'ACTIVE', notes: 'Saturazione da monitorare.', admittedAt: '2024-03-18' }
    ],
    beds: [
      { id: 4, department: 'CARD', status: 'OCCUPIED' },
      { id: 7, department: 'NEURO', status: 'AVAILABLE' },
      { id: 9, department: 'DERM', status: 'OCCUPIED' },
      { id: 11, department: 'ORTHO', status: 'OCCUPIED' },
      { id: 12, department: 'PNEUMO', status: 'AVAILABLE' }
    ],
    prescriptions: [
      { id: 60, patientId: 1, doctorId: 2, drug: 'Atorvastatina', dosage: '10mg', durationDays: 30, status: 'ACTIVE', notes: 'Assumere la sera.' },
      { id: 61, patientId: 2, doctorId: 3, drug: 'Levetiracetam', dosage: '500mg', durationDays: 14, status: 'ACTIVE', notes: 'Monitorare eventuali effetti collaterali.' },
      { id: 62, patientId: 3, doctorId: 4, drug: 'Clobetasolo', dosage: 'Crema 2 volte al giorno', durationDays: 10, status: 'PENDING', notes: 'Applicare solo sulle zone interessate.' },
      { id: 63, patientId: 4, doctorId: 5, drug: 'Ibuprofene', dosage: '400mg al bisogno', durationDays: 7, status: 'ACTIVE', notes: 'Non superare 3 dosi al giorno.' },
      { id: 64, patientId: 5, doctorId: 6, drug: 'Salbutamolo', dosage: '2 puff al bisogno', durationDays: 15, status: 'ACTIVE', notes: 'Tenere sempre con sé.' }
    ],
    televisits: [
      { id: 70, appointmentId: 22, provider: 'LIVEKIT', status: 'READY', token: 'tv-abc-123' },
      { id: 71, appointmentId: 23, provider: 'LIVEKIT', status: 'ACTIVE', token: 'tv-def-456' },
      { id: 72, appointmentId: 24, provider: 'LIVEKIT', status: 'COMPLETED', token: 'tv-ghi-789' }
    ],
    doctors: [
      { id: 2, firstName: 'Dott. Marco', lastName: 'Bianchi', speciality: 'CARD' },
      { id: 3, firstName: 'Dott.ssa Laura', lastName: 'Gatti', speciality: 'NEURO' },
      { id: 4, firstName: 'Dott.ssa Elisa', lastName: 'Mori', speciality: 'DERM' },
      { id: 5, firstName: 'Dott. Paolo', lastName: 'Serra', speciality: 'ORTHO' },
      { id: 6, firstName: 'Dott.ssa Giulia', lastName: 'Marini', speciality: 'PNEUMO' }
    ],
    patients: [
      { id: 1, firstName: 'Anna', lastName: 'Conti', email: 'anna.conti@sanitech.example' },
      { id: 2, firstName: 'Luca', lastName: 'Rinaldi', email: 'luca.rinaldi@sanitech.example' },
      { id: 3, firstName: 'Giulia', lastName: 'Rossi', email: 'giulia.rossi@sanitech.example' },
      { id: 4, firstName: 'Elena', lastName: 'Greco', email: 'elena.greco@sanitech.example' },
      { id: 5, firstName: 'Matteo', lastName: 'Ferri', email: 'matteo.ferri@sanitech.example' },
      { id: 6, firstName: 'Sara', lastName: 'Vitale', email: 'sara.vitale@sanitech.example' }
    ],
    departments: [
      { code: 'CARD', name: 'Cardiologia' },
      { code: 'NEURO', name: 'Neurologia' },
      { code: 'DERM', name: 'Dermatologia' },
      { code: 'ORTHO', name: 'Ortopedia' },
      { code: 'PNEUMO', name: 'Pneumologia' }
    ],
    specialities: [
      { code: 'CARD', name: 'Cardiologia' },
      { code: 'NEURO', name: 'Neurologia' },
      { code: 'DERM', name: 'Dermatologia' },
      { code: 'ORTHO', name: 'Ortopedia' },
      { code: 'PNEUMO', name: 'Pneumologia' }
    ],
    audit: [
      { id: 80, action: 'LOGIN', actor: 'anna.conti@sanitech.example', timestamp: '2024-03-18 09:10' },
      { id: 81, action: 'CREATE_APPOINTMENT', actor: 'Dr. Marco Bianchi', timestamp: '2024-03-18 09:30' },
      { id: 82, action: 'UPLOAD_DOC', actor: 'giulia.rossi@sanitech.example', timestamp: '2024-03-19 11:15' },
      { id: 83, action: 'CONFIRM_ADMISSION', actor: 'Dr.ssa Laura Gatti', timestamp: '2024-03-20 15:45' }
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
          const slot = this.mockStore.slots.find((item) => item.id === newAppointment.slotId);
          if (slot) {
            slot.status = 'BOOKED';
          }
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
            notes: this.getString(payload.notes, ''),
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
            subject: this.getString(payload.subject, 'Aggiornamento importante'),
            message: this.getString(payload.message, 'Notifica di sistema'),
            notes: this.getString(payload.notes, ''),
            status: this.getString(payload.status, 'SENT'),
            sentAt: new Date().toISOString()
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
          const paymentId = this.getNumber(payload.paymentId ?? payload.id, 0);
          const existingPayment = this.mockStore.payments.find((payment) => payment.id === paymentId);
          if (existingPayment) {
            existingPayment.status = this.getString(payload.status, 'PAID');
            existingPayment.paidAt = this.today();
            existingPayment.receiptName = this.getString(payload.receiptName, existingPayment.receiptName ?? '');
            return of(existingPayment as T);
          }
          const newPayment = {
            id: this.nextId(this.mockStore.payments),
            patientId: this.getNumber(payload.patientId, 1),
            amount: this.getNumber(payload.amount, 100),
            currency: this.getString(payload.currency, 'EUR'),
            service: this.getString(payload.service, 'Prestazione sanitaria'),
            status: this.getString(payload.status, 'PAID'),
            paidAt: this.today(),
            receiptName: this.getString(payload.receiptName, '')
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
            notes: this.getString(payload.notes, ''),
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
            doctorId: this.getNumber(payload.doctorId, 2),
            drug: this.getString(payload.drug, 'Farmaco prescritto'),
            dosage: this.getString(payload.dosage, '10mg'),
            durationDays: this.getNumber(payload.durationDays, 10),
            status: this.getString(payload.status, 'ACTIVE'),
            notes: this.getString(payload.notes, '')
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

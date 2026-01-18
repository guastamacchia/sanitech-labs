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
  mode: 'api' | 'scheduling' = 'api';
  slots: SchedulingSlot[] = [];
  appointments: SchedulingAppointment[] = [];
  schedulingError = '';
  bookingForm = {
    patientId: 1,
    doctorId: 2,
    slotId: 1,
    reason: ''
  };

  constructor(private route: ActivatedRoute, private api: ApiService) {
    const data = this.route.snapshot.data;
    this.title = data['title'] as string;
    this.description = data['description'] as string;
    this.mode = (data['view'] as 'api' | 'scheduling') ?? 'api';
    this.endpoints = (data['endpoints'] as ResourceEndpoint[]) ?? [];
    this.selectedEndpoint = this.endpoints[0];
    this.payload = this.selectedEndpoint?.payload ?? '';
    if (this.mode === 'scheduling') {
      this.loadScheduling();
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
}

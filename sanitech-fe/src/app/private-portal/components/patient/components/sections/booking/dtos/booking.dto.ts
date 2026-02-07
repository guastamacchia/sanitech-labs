// ============================================================
// DTOs per Appointment Booking (tipi interni per la UI)
// Estratti da appointment-booking.component.ts
// ============================================================

import { VisitMode, AppointmentStatus } from '../../../../dtos/patient-shared.dto';

export interface TimeSlot {
  id: number;
  doctorId: number;
  date: string;
  startTime: string;
  endTime: string;
  mode: VisitMode;
  status: 'AVAILABLE' | 'BOOKED';
}

export interface DisplayAppointment {
  id: number;
  slotId: number;
  patientId: number;
  doctorId: number;
  doctorName: string;
  departmentName: string;
  facilityName: string;
  date: string;
  startTime: string;
  endTime: string;
  mode: VisitMode;
  status: AppointmentStatus;
  reason?: string;
}

// ============================================================================
// DTOs locali per il componente Agenda
// ============================================================================

export type SlotStatus = 'AVAILABLE' | 'BOOKED' | 'BLOCKED';
export type SlotModality = 'IN_PERSON' | 'TELEVISIT' | 'BOTH';

export interface TimeSlot {
  id: number;
  date: string;
  time: string;
  duration: number;
  modality: SlotModality;
  status: SlotStatus;
  patientId?: number;
  patientName?: string;
  visitReason?: string;
  appointmentId?: number;
}

export interface SlotCreationForm {
  date: string;
  startTime: string;
  endTime: string;
  duration: number;
  modality: SlotModality;
  repeatWeekly: boolean;
  repeatWeeks: number;
}

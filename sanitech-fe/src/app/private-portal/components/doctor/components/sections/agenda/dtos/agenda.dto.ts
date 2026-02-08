// ============================================================================
// DTOs locali per il componente Agenda
// ============================================================================

// BUG-015: Allineato con backend SlotStatus (AVAILABLE, BOOKED, CANCELLED)
export type SlotStatus = 'AVAILABLE' | 'BOOKED' | 'CANCELLED';
// Manteniamo BOTH nel frontend per la creazione (crea slot IN_PERSON + TELEVISIT)
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

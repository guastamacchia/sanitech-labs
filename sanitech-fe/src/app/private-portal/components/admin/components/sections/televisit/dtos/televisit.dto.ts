// DTO per la sezione televisite admin
import { DoctorItem, DoctorApiItem, PatientItem, FacilityItem, DepartmentItem, PagedResponse } from '../../../../dtos/admin-shared.dto';

// Re-export per comodità
export { DoctorItem, DoctorApiItem, PatientItem, FacilityItem, DepartmentItem, PagedResponse };

// Allineato a TelevisitDto backend
export interface TelevisitItem {
  id: number;
  roomName: string;
  department: string;
  doctorSubject: string;
  patientSubject: string;
  scheduledAt: string;
  status: 'CREATED' | 'SCHEDULED' | 'ACTIVE' | 'ENDED' | 'CANCELED';
  provider?: string;
  token?: string;
}

export interface LiveKitTokenResponse {
  roomName: string;
  livekitUrl: string;
  token: string;
  expiresInSeconds: number;
}

// Payload per creazione televisita (allineato a TelevisitCreateDto backend)
export interface TelevisitCreatePayload {
  doctorSubject: string;
  patientSubject: string;
  department: string;
  scheduledAt: string;
}

// Configurazione room televisita per overlay
export interface TelevisitRoomConfig {
  id: string;
  room: string;
  url: string;
  token: string;
}

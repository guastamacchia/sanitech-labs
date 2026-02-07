// ============================================================
// DTOs per Patient Televisits (tipi interni per la UI)
// Estratti da patient-televisits.component.ts
// ============================================================

import { TelevisitDto } from '../../../../dtos/patient-shared.dto';

export interface TelevisitWithDetails extends TelevisitDto {
  doctorName?: string;
  departmentName?: string;
}

export interface DeviceCheck {
  camera: 'pending' | 'ok' | 'error';
  microphone: 'pending' | 'ok' | 'error';
  connection: 'pending' | 'ok' | 'error';
}

import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';

export interface DepartmentDto {
  id: number;
  code: string;
  name: string;
  facilityId?: number;
}

export interface PatientDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  departments: DepartmentDto[];
}

export interface PatientPage {
  content: PatientDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface DocumentDto {
  id: string;
  patientId: number;
  departmentCode: string;
  documentType: string;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  checksumSha256: string;
  description?: string;
  createdAt: string;
}

export interface DocumentPage {
  content: DocumentDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ConsentCheckResponse {
  patientId: number;
  doctorId: number;
  scope: 'DOCS' | 'PRESCRIPTIONS' | 'TELEVISIT';
  allowed: boolean;
  status: 'GRANTED' | 'REVOKED';
  expiresAt?: string;
}

export interface ProblemDetails {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
}

export class ConsentDeniedException extends Error {
  constructor(public problem: ProblemDetails) {
    super(problem.detail);
    this.name = 'ConsentDeniedException';
  }
}

@Injectable({
  providedIn: 'root'
})
export class DoctorDocsService {

  constructor(private api: ApiService) {}

  searchPatients(query: string, page = 0, size = 20): Observable<PatientPage> {
    return this.api.get<PatientPage>('/api/patients', {
      q: query,
      page,
      size
    });
  }

  getPatient(id: number): Observable<PatientDto> {
    return this.api.get<PatientDto>(`/api/patients/${id}`);
  }

  checkConsent(patientId: number, scope: 'DOCS' | 'PRESCRIPTIONS' | 'TELEVISIT' = 'DOCS'): Observable<ConsentCheckResponse> {
    return this.api.get<ConsentCheckResponse>('/api/consents/check', {
      patientId,
      scope
    });
  }

  getPatientDocuments(patientId: number, page = 0, size = 20): Observable<DocumentPage> {
    return this.api.get<DocumentPage>('/api/docs', {
      patientId,
      page,
      size,
      sort: 'createdAt,desc'
    }).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 403 && error.error?.type) {
          return throwError(() => new ConsentDeniedException(error.error as ProblemDetails));
        }
        return throwError(() => error);
      })
    );
  }

  downloadDocument(documentId: string): string {
    return `/api/docs/${documentId}/download`;
  }
}

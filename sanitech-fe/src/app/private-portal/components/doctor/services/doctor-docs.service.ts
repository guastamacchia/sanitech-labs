import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiService } from '@core/services/api.service';

// ============================================================================
// RE-EXPORT DTOs per backward compatibility
// Le definizioni sono ora centralizzate in ../dtos/doctor-shared.dto.ts
// ============================================================================
export {
  DepartmentDto,
  PatientDto,
  DocumentDto,
  ConsentCheckResponse,
  ProblemDetails
} from '../dtos/doctor-shared.dto';

import type {
  Page,
  PatientDto,
  DocumentDto,
  ConsentCheckResponse,
  ProblemDetails
} from '../dtos/doctor-shared.dto';

// Alias di tipo per backward compatibility (PatientPage e DocumentPage
// erano definiti come interfacce dedicate, ora sono alias di Page<T>)
export type PatientPage = Page<PatientDto>;
export type DocumentPage = Page<DocumentDto>;

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

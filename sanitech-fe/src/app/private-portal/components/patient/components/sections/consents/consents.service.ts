import { Injectable } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import { ConsentDto, DoctorDto, PageResponse } from './dtos/consents.dto';

@Injectable({
  providedIn: 'root'
})
export class ConsentsService {

  constructor(private api: ApiService) {}

  /**
   * Carica medici e consensi in parallelo
   */
  loadData(): Observable<{
    doctors: PageResponse<DoctorDto>;
    consents: ConsentDto[];
  }> {
    return forkJoin({
      doctors: this.api.request<PageResponse<DoctorDto>>('GET', '/api/doctors?size=100'),
      consents: this.api.request<ConsentDto[]>('GET', '/api/consents/me/doctors')
    });
  }

  /**
   * Concede consensi multipli a un medico (bulk)
   */
  grantBulkConsents(body: {
    doctorId: number;
    scopes: string[];
    expiresAt: string | null;
  }): Observable<ConsentDto[]> {
    return this.api.request<ConsentDto[]>('POST', '/api/consents/me/doctors/bulk', body);
  }

  /**
   * Revoca un consenso specifico
   */
  revokeConsent(doctorId: number, scope: string): Observable<void> {
    return this.api.request<void>('DELETE', `/api/consents/me/doctors/${doctorId}/${scope}`);
  }

  /**
   * Modifica un consenso (es. scadenza)
   */
  updateConsent(doctorId: number, scope: string, body: {
    doctorId: number;
    scope: string;
    expiresAt: string | null;
  }): Observable<ConsentDto> {
    return this.api.request<ConsentDto>(
      'PATCH',
      `/api/consents/me/doctors/${doctorId}/${scope}`,
      body
    );
  }
}

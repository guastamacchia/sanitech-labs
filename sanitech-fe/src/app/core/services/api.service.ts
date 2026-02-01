import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) {}

  request<T>(method: string, path: string, body?: unknown): Observable<T> {
    const url = `${environment.gatewayUrl}${path}`;
    return this.http.request<T>(method, url, {
      body
    });
  }

  get<T>(path: string, params?: Record<string, string | number | boolean | undefined>): Observable<T> {
    const url = `${environment.gatewayUrl}${path}`;
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          httpParams = httpParams.set(key, String(value));
        }
      });
    }
    return this.http.get<T>(url, { params: httpParams });
  }

  post<T>(path: string, body?: unknown): Observable<T> {
    const url = `${environment.gatewayUrl}${path}`;
    return this.http.post<T>(url, body);
  }

  delete<T>(path: string): Observable<T> {
    const url = `${environment.gatewayUrl}${path}`;
    return this.http.delete<T>(url);
  }
}

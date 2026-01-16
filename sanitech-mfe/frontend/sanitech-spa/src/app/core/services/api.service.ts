import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
}

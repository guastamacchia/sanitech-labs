import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

interface ResourceEndpoint {
  label: string;
  method: string;
  path: string;
  payload?: string;
}

@Component({
  selector: 'app-resource-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './resource-page.component.html'
})
export class ResourcePageComponent {
  title = '';
  description = '';
  endpoints: ResourceEndpoint[] = [];
  selectedEndpoint?: ResourceEndpoint;
  payload = '';
  responseBody = '';
  isLoading = false;

  constructor(private route: ActivatedRoute, private api: ApiService) {
    const data = this.route.snapshot.data;
    this.title = data['title'] as string;
    this.description = data['description'] as string;
    this.endpoints = data['endpoints'] as ResourceEndpoint[];
    this.selectedEndpoint = this.endpoints[0];
    this.payload = this.selectedEndpoint?.payload ?? '';
  }

  selectEndpoint(endpoint: ResourceEndpoint): void {
    this.selectedEndpoint = endpoint;
    this.payload = endpoint.payload ?? '';
    this.responseBody = '';
  }

  execute(): void {
    if (!this.selectedEndpoint) {
      return;
    }
    let body: unknown;
    if (this.payload.trim()) {
      try {
        body = JSON.parse(this.payload);
      } catch {
        this.responseBody = 'Payload JSON non valido.';
        return;
      }
    }
    this.isLoading = true;
    this.responseBody = '';
    this.api.request(this.selectedEndpoint.method, this.selectedEndpoint.path, body).subscribe({
      next: (response) => {
        this.responseBody = JSON.stringify(response, null, 2);
        this.isLoading = false;
      },
      error: (error) => {
        this.responseBody = JSON.stringify(error, null, 2);
        this.isLoading = false;
      }
    });
  }
}

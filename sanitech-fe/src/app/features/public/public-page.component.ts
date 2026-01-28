import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-public-page',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './public-page.component.html'
})
export class PublicPageComponent {
  registrationForm = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    notes: ''
  };
  contactForm = {
    fullName: '',
    email: '',
    subject: '',
    message: ''
  };
  registrationSuccess = '';
  registrationError = '';
  contactSuccess = '';
  activeAccessTab: 'login' | 'register' = 'login';

  constructor(public auth: AuthService, private api: ApiService) {}

  login(): void {
    this.auth.login();
  }

  setActiveAccessTab(tab: 'login' | 'register'): void {
    this.activeAccessTab = tab;
    this.registrationSuccess = '';
    this.registrationError = '';
  }

  submitRegistration(): void {
    this.registrationSuccess = '';
    this.registrationError = '';
    const payload = { ...this.registrationForm };

    this.api.request<void>('POST', '/api/public/patients', payload).subscribe({
      next: () => {
        this.registrationSuccess = `Grazie ${this.registrationForm.firstName}, la tua richiesta è stata registrata. Ti contatteremo per completare l’accesso.`;
        this.registrationForm = {
          firstName: '',
          lastName: '',
          email: '',
          phone: '',
          notes: ''
        };
      },
      error: (error: { error?: { message?: string } }) => {
        this.registrationError =
          error?.error?.message ??
          'Non è stato possibile completare la registrazione. Verifica i dati inseriti e riprova.';
      }
    });
  }

  submitContact(): void {
    this.contactSuccess = `Grazie ${this.contactForm.fullName}, abbiamo ricevuto la tua richiesta. Un nostro referente ti risponderà al più presto.`;
    this.contactForm = {
      fullName: '',
      email: '',
      subject: '',
      message: ''
    };
  }
}

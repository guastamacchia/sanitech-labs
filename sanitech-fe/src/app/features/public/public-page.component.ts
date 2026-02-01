import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { ApiService } from '../../core/services/api.service';
import { RecaptchaV3Module, ReCaptchaV3Service } from 'ng-recaptcha';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-public-page',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, RecaptchaV3Module],
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
  isSubmitting = false;
  recaptchaEnabled = !!environment.recaptchaSiteKey;

  constructor(
    public auth: AuthService,
    private api: ApiService,
    private recaptchaV3Service: ReCaptchaV3Service
  ) {}

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
    this.isSubmitting = true;

    // Ottieni il token reCAPTCHA prima di inviare la richiesta
    if (this.recaptchaEnabled) {
      this.recaptchaV3Service.execute('patient_registration').subscribe({
        next: (captchaToken) => {
          this.sendRegistrationRequest(captchaToken);
        },
        error: () => {
          this.registrationError = 'Errore durante la verifica di sicurezza. Riprova.';
          this.isSubmitting = false;
        }
      });
    } else {
      // Se reCAPTCHA non è configurato, invia senza token
      this.sendRegistrationRequest('');
    }
  }

  private sendRegistrationRequest(captchaToken: string): void {
    const payload = { ...this.registrationForm, captchaToken };

    this.api.request<void>('POST', '/api/public/patients', payload).subscribe({
      next: () => {
        this.registrationSuccess = `Grazie ${this.registrationForm.firstName}, la tua richiesta è stata registrata. Ti contatteremo per completare l'accesso.`;
        this.registrationForm = {
          firstName: '',
          lastName: '',
          email: '',
          phone: '',
          notes: ''
        };
        this.isSubmitting = false;
      },
      error: (error: { error?: { message?: string; detail?: string } }) => {
        this.registrationError =
          error?.error?.detail ??
          error?.error?.message ??
          'Non è stato possibile completare la registrazione. Verifica i dati inseriti e riprova.';
        this.isSubmitting = false;
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

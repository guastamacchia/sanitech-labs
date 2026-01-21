import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-public-page',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './public-page.component.html'
})
export class PublicPageComponent {
  credentials = {
    email: '',
    password: '',
    role: 'ROLE_PATIENT'
  };
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
  loginError = '';
  registrationSuccess = '';
  contactSuccess = '';
  activeAccessTab: 'login' | 'register' = 'login';
  profiles = this.auth.mockProfiles;

  constructor(public auth: AuthService, private router: Router) {}

  submitLogin(): void {
    this.loginError = '';
    const success = this.auth.signInWithCredentials(
      this.credentials.email,
      this.credentials.password,
      this.credentials.role
    );
    if (!success) {
      this.loginError = 'Inserisci credenziali valide per completare l’accesso.';
      return;
    }
    this.router.navigate(['/portal']);
  }

  setActiveAccessTab(tab: 'login' | 'register'): void {
    this.activeAccessTab = tab;
    this.loginError = '';
    this.registrationSuccess = '';
  }

  submitRegistration(): void {
    this.registrationSuccess = `Grazie ${this.registrationForm.firstName}, la tua richiesta è stata registrata. Ti contatteremo per completare l’accesso.`;
    this.registrationForm = {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      notes: ''
    };
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

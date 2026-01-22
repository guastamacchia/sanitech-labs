import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';

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
  contactSuccess = '';
  activeAccessTab: 'login' | 'register' = 'login';

  constructor(public auth: AuthService) {}

  login(): void {
    this.auth.login();
  }

  setActiveAccessTab(tab: 'login' | 'register'): void {
    this.activeAccessTab = tab;
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

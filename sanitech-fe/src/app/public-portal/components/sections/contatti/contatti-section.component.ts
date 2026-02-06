import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

/** Sezione "Contatti" della landing page pubblica. Gestisce il form di contatto. */
@Component({
  selector: 'app-contatti-section',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contatti-section.component.html'
})
export class ContattiSectionComponent {
  contactForm = {
    fullName: '',
    email: '',
    subject: '',
    message: ''
  };
  contactSuccess = '';

  /** Invia il modulo di contatto (simulazione frontend) */
  submitContact(): void {
    this.contactSuccess =
      `Grazie ${this.contactForm.fullName}, abbiamo ricevuto la tua richiesta. Un nostro referente ti risponderà al più presto.`;
    this.contactForm = { fullName: '', email: '', subject: '', message: '' };
  }
}

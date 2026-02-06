import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '@core/services/api.service';
import { RecaptchaV3Module, ReCaptchaV3Service } from 'ng-recaptcha';
import { environment } from '@env/environment';

/** Contenuto del tab "Registrazione pazienti" — form + reCAPTCHA + POST API. */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RecaptchaV3Module],
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnChanges {
  /** Indica se il tab registrazione è attivo (per reset messaggi al cambio) */
  @Input() isActive = false;

  /** Step corrente del form di registrazione (1 = anagrafica, 2 = contatti) */
  currentStep: 1 | 2 = 1;

  registrationForm = {
    firstName: '',
    lastName: '',
    fiscalCode: '',
    birthDate: '',
    email: '',
    phone: '',
    address: ''
  };
  registrationSuccess = '';
  registrationError = '';
  isSubmitting = false;
  recaptchaEnabled = !!environment.recaptchaSiteKey;

  constructor(
    private api: ApiService,
    private recaptchaV3Service: ReCaptchaV3Service
  ) {}

  /** Resetta messaggi successo/errore e step al cambio tab */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isActive']) {
      this.registrationSuccess = '';
      this.registrationError = '';
      this.currentStep = 1;
    }
  }

  /** Passa allo step successivo */
  nextStep(): void {
    this.currentStep = 2;
  }

  /** Torna allo step precedente */
  prevStep(): void {
    this.currentStep = 1;
  }

  /** Verifica se i campi dello step 1 sono compilati */
  get isStep1Valid(): boolean {
    return !!(this.registrationForm.firstName && this.registrationForm.lastName && this.registrationForm.fiscalCode);
  }

  /** Invia la richiesta di registrazione con verifica reCAPTCHA */
  submitRegistration(): void {
    this.registrationSuccess = '';
    this.registrationError = '';
    this.isSubmitting = true;

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
      this.sendRegistrationRequest('');
    }
  }

  private sendRegistrationRequest(captchaToken: string): void {
    const payload = { ...this.registrationForm, captchaToken };

    this.api.request<void>('POST', '/api/public/patients', payload).subscribe({
      next: () => {
        this.registrationSuccess =
          `Grazie ${this.registrationForm.firstName}, la tua richiesta è stata registrata. Ti contatteremo per completare l'accesso.`;
        this.registrationForm = {
          firstName: '',
          lastName: '',
          fiscalCode: '',
          birthDate: '',
          email: '',
          phone: '',
          address: ''
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
}

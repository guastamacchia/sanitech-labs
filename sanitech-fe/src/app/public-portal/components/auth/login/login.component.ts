import { Component } from '@angular/core';
import { AuthService } from '@core/auth/auth.service';

/** Contenuto del tab "Accesso riservato" — delega login a OAuth2/Keycloak. */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  constructor(private auth: AuthService) {}

  /** Avvia il flusso di login OAuth2 tramite Keycloak */
  login(): void {
    this.auth.login();
  }
}

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';
import { LoginComponent } from '../../auth/login/login.component';
import { RegisterComponent } from '../../auth/register/register.component';

/** Sezione hero (#accesso) con card di autenticazione (tab login/registrazione). */
@Component({
  selector: 'app-hero-section',
  standalone: true,
  imports: [CommonModule, RouterLink, LoginComponent, RegisterComponent],
  templateUrl: './hero-section.component.html'
})
export class HeroSectionComponent {
  /** Tab attivo nella card di autenticazione */
  @Input() activeAccessTab: 'login' | 'register' = 'login';
  /** Emette il cambio di tab verso il componente padre */
  @Output() tabChange = new EventEmitter<'login' | 'register'>();

  constructor(public auth: AuthService) {}

  /** Cambia il tab attivo e notifica il padre */
  setActiveTab(tab: 'login' | 'register'): void {
    this.tabChange.emit(tab);
  }
}

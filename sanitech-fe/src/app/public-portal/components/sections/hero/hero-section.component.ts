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
  templateUrl: './hero-section.component.html',
  styles: [`
    .hero-nav-card {
      background: rgba(248, 249, 250, 0.6);
      border: 1px solid rgba(0, 0, 0, 0.08);
      transition: all 0.25s ease;
    }
    .hero-nav-card:hover {
      background: #fff;
      border-color: rgba(13, 110, 253, 0.2);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }
    .hero-nav-circle {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
      transition: transform 0.25s ease;
    }
    .hero-nav-card:hover .hero-nav-circle { transform: scale(1.1); }
    .hero-nav-label {
      font-size: 0.8rem;
      font-weight: 600;
      color: #343a40;
      letter-spacing: 0.02em;
    }
    .hero-nav-desc {
      display: block;
      font-size: 0.68rem;
      color: #6c757d;
      margin-top: 2px;
      line-height: 1.3;
    }
  `]
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

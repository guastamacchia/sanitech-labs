import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  constructor(public auth: AuthService, private router: Router) {}

  async ngOnInit(): Promise<void> {
    // La discovery OAuth viene eseguita in APP_INITIALIZER (main.ts)
    // per garantire che il token sia disponibile prima dei route guard.
    // Qui gestiamo solo il redirect degli utenti autenticati su rotte pubbliche.
    // Usiamo window.location.pathname per ottenere la rotta reale del browser
    // perché this.router.url può essere '/' durante l'inizializzazione.
    if (this.auth.isAuthenticated) {
      const browserPath = window.location.pathname;
      const isPublicRoute = browserPath === '/' || !browserPath.startsWith('/portal');
      if (isPublicRoute) {
        await this.router.navigate(['/portal']);
      }
    }
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  get isInReservedArea(): boolean {
    return this.router.url.startsWith('/portal');
  }
}
